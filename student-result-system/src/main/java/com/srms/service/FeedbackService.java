package com.srms.service;

import com.srms.config.DatabaseConnection;
import com.srms.dao.*;
import com.srms.exception.ValidationException;
import com.srms.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Service for feedback/correction/assistance request management.
 */
public class FeedbackService {
    private static final Logger log = LoggerFactory.getLogger(FeedbackService.class);

    private final FeedbackDAO feedbackDAO = new FeedbackDAO();
    private final ReferenceDataDAO refDAO = new ReferenceDataDAO();
    private final ActivityLogDAO activityDAO = new ActivityLogDAO();

    // In-memory fallback stores
    private static final List<FeedbackRequest> inMemCorrections = new ArrayList<>();
    private static final List<FeedbackRequest> inMemAssistance = new ArrayList<>();

    public List<FeedbackRequest> getAllCorrectionRequests() {
        if (!DatabaseConnection.getInstance().isAvailable()) return inMemCorrections;
        return feedbackDAO.findAllCorrections();
    }

    public List<FeedbackRequest> getCorrectionsByStudent(int studentId) {
        if (!DatabaseConnection.getInstance().isAvailable()) {
            return inMemCorrections.stream().filter(r -> r.getStudentId() == studentId).toList();
        }
        return feedbackDAO.findCorrectionsByStudent(studentId);
    }

    public void submitCorrectionRequest(Student student, Subject subject, Integer termId, String requestText) {
        if (requestText == null || requestText.isBlank()) {
            throw new ValidationException("Please describe what correction is needed.");
        }
        Integer uid = AuthService.getCurrentUser() != null ? AuthService.getCurrentUser().getId() : null;

        if (!DatabaseConnection.getInstance().isAvailable()) {
            FeedbackRequest r = new FeedbackRequest(
                    inMemCorrections.size() + 1,
                    student.getId(), subject.getId(),
                    null, requestText, "Pending", null, null,
                    java.time.LocalDateTime.now(),
                    student.getName(), student.getReg(), subject.getSubjectName(), "correction"
            );
            inMemCorrections.add(r);
            return;
        }
        feedbackDAO.insertCorrection(student.getId(), subject.getId(), termId, requestText);
        activityDAO.log(uid, "CORRECTION_REQ",
                "Correction request from " + student.getName() + " - " + subject.getSubjectName());
    }

    public List<FeedbackRequest> getAllAssistanceRequests() {
        if (!DatabaseConnection.getInstance().isAvailable()) return inMemAssistance;
        return feedbackDAO.findAllAssistance();
    }

    public List<FeedbackRequest> getAssistanceByStudent(int studentId) {
        if (!DatabaseConnection.getInstance().isAvailable()) {
            return inMemAssistance.stream().filter(r -> r.getStudentId() == studentId).toList();
        }
        return feedbackDAO.findAssistanceByStudent(studentId);
    }

    public void submitAssistanceRequest(Student student, Subject subject, String topic, String comments) {
        if (topic == null || topic.isBlank()) {
            throw new ValidationException("Please select or enter a topic.");
        }
        Integer uid = AuthService.getCurrentUser() != null ? AuthService.getCurrentUser().getId() : null;

        if (!DatabaseConnection.getInstance().isAvailable()) {
            FeedbackRequest r = new FeedbackRequest(
                    inMemAssistance.size() + 1,
                    student.getId(), subject.getId(),
                    topic, comments, "Pending", null, null,
                    java.time.LocalDateTime.now(),
                    student.getName(), student.getReg(), subject.getSubjectName(), "assistance"
            );
            inMemAssistance.add(r);
            return;
        }
        feedbackDAO.insertAssistance(student.getId(), subject.getId(), topic, comments);
        activityDAO.log(uid, "ASSIST_REQ",
                "Topic assistance request from " + student.getName() + " - " + subject.getSubjectName());
    }

    public boolean updateCorrectionStatus(int requestId, String status, String response) {
        Integer uid = AuthService.getCurrentUser() != null ? AuthService.getCurrentUser().getId() : null;
        if (!DatabaseConnection.getInstance().isAvailable()) {
            inMemCorrections.stream().filter(r -> r.getId() == requestId).findFirst()
                    .ifPresent(r -> r.setStatus(status));
            return true;
        }
        boolean ok = feedbackDAO.updateCorrectionStatus(requestId, status, response, uid);
        if (ok) activityDAO.log(uid, "CORRECTION_REVIEW", "Correction request #" + requestId + " -> " + status);
        return ok;
    }

    public boolean updateAssistanceStatus(int requestId, String status, String response) {
        Integer uid = AuthService.getCurrentUser() != null ? AuthService.getCurrentUser().getId() : null;
        if (!DatabaseConnection.getInstance().isAvailable()) {
            inMemAssistance.stream().filter(r -> r.getId() == requestId).findFirst()
                    .ifPresent(r -> r.setStatus(status));
            return true;
        }
        boolean ok = feedbackDAO.updateAssistanceStatus(requestId, status, response, uid);
        if (ok) activityDAO.log(uid, "ASSIST_REVIEW", "Assistance request #" + requestId + " -> " + status);
        return ok;
    }

    public int getPendingCorrectionCount() {
        if (!DatabaseConnection.getInstance().isAvailable()) {
            return (int) inMemCorrections.stream().filter(r -> "Pending".equals(r.getStatus())).count();
        }
        return feedbackDAO.countPendingCorrections();
    }

    public int getPendingAssistanceCount() {
        if (!DatabaseConnection.getInstance().isAvailable()) {
            return (int) inMemAssistance.stream().filter(r -> "Pending".equals(r.getStatus())).count();
        }
        return feedbackDAO.countPendingAssistance();
    }

    /** Returns topic suggestions for a given subject name. */
    public List<String> getTopicsForSubject(String subjectName) {
        return switch (subjectName) {
            case "Mathematics" -> List.of("Algebra", "Geometry", "Trigonometry", "Statistics", "Calculus");
            case "Science" -> List.of("Electricity", "Chemical Reactions", "Cell Biology", "Optics", "Magnetism");
            case "English" -> List.of("Grammar", "Essay Writing", "Comprehension", "Poetry", "Debate");
            case "History" -> List.of("Civics", "Geography", "Modern History", "Ancient Civilizations", "Freedom Movement");
            case "Computer Science" -> List.of("Programming", "Databases", "Networking", "Web Development", "Algorithms");
            default -> List.of("General Topic", "Exam Preparation", "Assignment Help");
        };
    }
}
