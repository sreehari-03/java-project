package com.srms.service;

import com.srms.config.DatabaseConnection;
import com.srms.dao.*;
import com.srms.exception.ValidationException;
import com.srms.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Service for result entry, updating, and retrieval.
 */
public class ResultService {
    private static final Logger log = LoggerFactory.getLogger(ResultService.class);

    private final ResultDAO resultDAO = new ResultDAO();
    private final RemarkDAO remarkDAO = new RemarkDAO();
    private final ReferenceDataDAO refDAO = new ReferenceDataDAO();
    private final ActivityLogDAO activityDAO = new ActivityLogDAO();

    // In-memory fallback store: key = studentId+"|"+termId+"|"+subjectId → marks
    private static final Map<String, Integer> inMemResults = new HashMap<>();
    private static final Map<String, String> inMemRemarks = new HashMap<>();

    static {
        // Seed some demo results for offline mode
        seedInMemory("1", "1", "1", 85); seedInMemory("1", "1", "2", 78);
        seedInMemory("1", "1", "3", 76); seedInMemory("1", "1", "4", 68); seedInMemory("1", "1", "5", 92);
    }

    private static void seedInMemory(String sid, String tid, String subid, int marks) {
        inMemResults.put(sid + "|" + tid + "|" + subid, marks);
    }

    /**
     * Returns all results for a student in a term.
     */
    public List<StudentResult> getResultsForStudentTerm(Student student, AcademicTerm term) {
        if (!DatabaseConnection.getInstance().isAvailable()) {
            return buildOfflineResults(student, term);
        }
        return resultDAO.findByStudentAndTerm(student.getId(), term.getId());
    }

    /**
     * Saves all results + overall remark for a student+term.
     * Validates marks and creates pass/fail flags.
     */
    public void saveResults(Student student, AcademicTerm term, List<MarkEntry> entries, String overallRemark) {
        for (MarkEntry entry : entries) {
            if (entry.marks < 0 || entry.marks > entry.subject.getMaxMarks()) {
                throw new ValidationException("Marks for " + entry.subject.getSubjectName()
                        + " must be between 0 and " + entry.subject.getMaxMarks() + ".");
            }
        }

        if (!DatabaseConnection.getInstance().isAvailable()) {
            for (MarkEntry e : entries) {
                inMemResults.put(student.getId() + "|" + term.getId() + "|" + e.subject.getId(), e.marks);
            }
            if (overallRemark != null) inMemRemarks.put(student.getId() + "|" + term.getId(), overallRemark);
            return;
        }

        Integer teacherId = AuthService.getCurrentUser() != null ? AuthService.getCurrentUser().getId() : null;

        for (MarkEntry e : entries) {
            boolean isPass = e.marks >= e.subject.getPassMarks();
            StudentResult sr = new StudentResult();
            sr.setStudentId(student.getId());
            sr.setSubjectId(e.subject.getId());
            sr.setTermId(term.getId());
            sr.setMarksObtained(e.marks);
            sr.setMaxMarks(e.subject.getMaxMarks());
            sr.setPass(isPass);
            sr.setTeacherRemarks(e.subjectRemark);
            sr.setEnteredBy(teacherId);
            resultDAO.upsert(sr);
        }

        // Save overall remark
        if (overallRemark != null && !overallRemark.isBlank()) {
            remarkDAO.upsertOverallRemark(student.getId(), term.getId(), teacherId, overallRemark);
        }

        activityDAO.log(teacherId, "RESULT_UPDATE",
                "Results saved for " + student.getName() + " - " + term.getTermName());
    }

    public int getStudentsWithResultsCount() {
        if (!DatabaseConnection.getInstance().isAvailable()) return 3; // demo
        return resultDAO.countStudentsWithResults();
    }

    public String getOverallRemark(int studentId, int termId) {
        if (!DatabaseConnection.getInstance().isAvailable()) {
            return inMemRemarks.getOrDefault(studentId + "|" + termId, "");
        }
        return remarkDAO.getOverallRemark(studentId, termId).orElse("");
    }

    public List<TeacherRemark> getRemarksForStudentTerm(int studentId, int termId) {
        if (!DatabaseConnection.getInstance().isAvailable()) return Collections.emptyList();
        return remarkDAO.findByStudentAndTerm(studentId, termId);
    }

    public List<TeacherRemark> getAllRemarks() {
        if (!DatabaseConnection.getInstance().isAvailable()) return Collections.emptyList();
        return remarkDAO.findAll();
    }

    /**
     * Returns average percentage for a student in a given term (0..100, or -1 if no results).
     */
    public double getAveragePercentage(Student student, AcademicTerm term) {
        if (!DatabaseConnection.getInstance().isAvailable()) {
            List<StudentResult> results = buildOfflineResults(student, term);
            if (results.isEmpty()) return -1;
            return results.stream().mapToDouble(StudentResult::getPercentage).average().orElse(-1);
        }
        return resultDAO.averageForStudent(student.getId(), term.getId());
    }

    public List<String[]> getAverageBySubjectForTerm(int termId) {
        if (!DatabaseConnection.getInstance().isAvailable()) return Collections.emptyList();
        return resultDAO.averageBySubjectForTerm(termId);
    }

    public List<Subject> getAllSubjects() {
        return refDAO.findAllSubjects();
    }

    public List<AcademicTerm> getAllTerms() {
        return refDAO.findAllTerms();
    }

    public AcademicTerm getFirstTerm() {
        List<AcademicTerm> terms = getAllTerms();
        return terms.isEmpty() ? null : terms.get(0);
    }

    private List<StudentResult> buildOfflineResults(Student student, AcademicTerm term) {
        List<Subject> subjects = refDAO.findAllSubjects();
        List<StudentResult> results = new ArrayList<>();
        for (Subject sub : subjects) {
            String key = student.getId() + "|" + term.getId() + "|" + sub.getId();
            if (inMemResults.containsKey(key)) {
                int marks = inMemResults.get(key);
                StudentResult sr = new StudentResult();
                sr.setStudentId(student.getId());
                sr.setSubjectId(sub.getId());
                sr.setTermId(term.getId());
                sr.setMarksObtained(marks);
                sr.setMaxMarks(sub.getMaxMarks());
                sr.setPass(marks >= sub.getPassMarks());
                sr.setSubjectName(sub.getSubjectName());
                sr.setTermName(term.getTermName());
                sr.setStudentName(student.getName());
                sr.setRegNo(student.getReg());
                results.add(sr);
            }
        }
        return results;
    }

    /**
     * Simple holder for a subject + its mark + optional subject remark.
     */
    public record MarkEntry(Subject subject, int marks, String subjectRemark) {}
}
