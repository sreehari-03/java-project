package com.srms.dao;

import com.srms.config.DatabaseConnection;
import com.srms.exception.DatabaseException;
import com.srms.model.StudentResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.*;

/**
 * DAO for student result CRUD operations.
 */
public class ResultDAO {
    private static final Logger log = LoggerFactory.getLogger(ResultDAO.class);
    private final DatabaseConnection db = DatabaseConnection.getInstance();

    /**
     * Returns all results for a given student and term (joined with subject name).
     */
    public List<StudentResult> findByStudentAndTerm(int studentId, int termId) {
        if (!db.isAvailable()) return Collections.emptyList();
        final String sql = """
            SELECT sr.id, sr.student_id, sr.subject_id, sr.term_id, sr.marks_obtained,
                   sr.max_marks, sr.is_pass, sr.teacher_remarks, sr.entered_by,
                   sr.created_at, sr.updated_at,
                   s.full_name AS student_name, s.reg_no,
                   sub.subject_name, t.term_name
            FROM student_results sr
            JOIN students s ON sr.student_id = s.id
            JOIN subjects sub ON sr.subject_id = sub.id
            JOIN academic_terms t ON sr.term_id = t.id
            WHERE sr.student_id = ? AND sr.term_id = ?
            ORDER BY sub.subject_name
            """;
        List<StudentResult> list = new ArrayList<>();
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, termId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapResult(rs));
            }
        } catch (SQLException e) {
            log.error("findByStudentAndTerm error: {}", e.getMessage());
        }
        return list;
    }

    public Optional<StudentResult> findByStudentSubjectTerm(int studentId, int subjectId, int termId) {
        if (!db.isAvailable()) return Optional.empty();
        final String sql = """
            SELECT sr.id, sr.student_id, sr.subject_id, sr.term_id, sr.marks_obtained,
                   sr.max_marks, sr.is_pass, sr.teacher_remarks, sr.entered_by,
                   sr.created_at, sr.updated_at,
                   s.full_name AS student_name, s.reg_no,
                   sub.subject_name, t.term_name
            FROM student_results sr
            JOIN students s ON sr.student_id = s.id
            JOIN subjects sub ON sr.subject_id = sub.id
            JOIN academic_terms t ON sr.term_id = t.id
            WHERE sr.student_id = ? AND sr.subject_id = ? AND sr.term_id = ?
            """;
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, subjectId);
            ps.setInt(3, termId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapResult(rs));
            }
        } catch (SQLException e) {
            log.error("findByStudentSubjectTerm error: {}", e.getMessage());
        }
        return Optional.empty();
    }

    /**
     * Upsert: inserts or updates a result.
     */
    public boolean upsert(StudentResult result) {
        if (!db.isAvailable()) throw new DatabaseException("Database not connected");
        final String sql = """
            INSERT INTO student_results (student_id, subject_id, term_id, marks_obtained, max_marks, is_pass, teacher_remarks, entered_by)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE
                marks_obtained = VALUES(marks_obtained),
                max_marks = VALUES(max_marks),
                is_pass = VALUES(is_pass),
                teacher_remarks = VALUES(teacher_remarks),
                entered_by = VALUES(entered_by),
                updated_at = CURRENT_TIMESTAMP
            """;
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, result.getStudentId());
            ps.setInt(2, result.getSubjectId());
            ps.setInt(3, result.getTermId());
            ps.setInt(4, result.getMarksObtained());
            ps.setInt(5, result.getMaxMarks());
            ps.setBoolean(6, result.isPass());
            ps.setString(7, result.getTeacherRemarks());
            if (result.getEnteredBy() != null) ps.setInt(8, result.getEnteredBy());
            else ps.setNull(8, Types.INTEGER);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to upsert result: " + e.getMessage(), e);
        }
    }

    /**
     * Returns total count of distinct students with at least one result.
     */
    public int countStudentsWithResults() {
        if (!db.isAvailable()) return 0;
        try (Connection conn = db.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(DISTINCT student_id) FROM student_results")) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            log.error("countStudentsWithResults error: {}", e.getMessage());
            return 0;
        }
    }

    /**
     * Returns [subject_name, average_marks] across all students for a term.
     */
    public List<String[]> averageBySubjectForTerm(int termId) {
        if (!db.isAvailable()) return Collections.emptyList();
        final String sql = """
            SELECT sub.subject_name, ROUND(AVG(sr.marks_obtained), 1) AS avg_marks
            FROM student_results sr
            JOIN subjects sub ON sr.subject_id = sub.id
            WHERE sr.term_id = ?
            GROUP BY sub.id, sub.subject_name
            ORDER BY sub.subject_name
            """;
        List<String[]> rows = new ArrayList<>();
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, termId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(new String[]{rs.getString("subject_name"), rs.getString("avg_marks")});
                }
            }
        } catch (SQLException e) {
            log.error("averageBySubjectForTerm error: {}", e.getMessage());
        }
        return rows;
    }

    /**
     * Returns overall average percentage for a student across all subjects in a term.
     */
    public double averageForStudent(int studentId, int termId) {
        if (!db.isAvailable()) return -1;
        final String sql = """
            SELECT AVG(CAST(marks_obtained AS DECIMAL(10,2)) / max_marks * 100) AS avg_pct
            FROM student_results
            WHERE student_id = ? AND term_id = ?
            """;
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, termId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    double v = rs.getDouble("avg_pct");
                    return rs.wasNull() ? -1 : v;
                }
            }
        } catch (SQLException e) {
            log.error("averageForStudent error: {}", e.getMessage());
        }
        return -1;
    }

    private StudentResult mapResult(ResultSet rs) throws SQLException {
        StudentResult r = new StudentResult();
        r.setId(rs.getInt("id"));
        r.setStudentId(rs.getInt("student_id"));
        r.setSubjectId(rs.getInt("subject_id"));
        r.setTermId(rs.getInt("term_id"));
        r.setMarksObtained(rs.getInt("marks_obtained"));
        r.setMaxMarks(rs.getInt("max_marks"));
        r.setPass(rs.getBoolean("is_pass"));
        r.setTeacherRemarks(rs.getString("teacher_remarks"));
        int eb = rs.getInt("entered_by");
        r.setEnteredBy(rs.wasNull() ? null : eb);
        Timestamp ca = rs.getTimestamp("created_at");
        if (ca != null) r.setCreatedAt(ca.toLocalDateTime());
        Timestamp ua = rs.getTimestamp("updated_at");
        if (ua != null) r.setUpdatedAt(ua.toLocalDateTime());
        r.setStudentName(rs.getString("student_name"));
        r.setRegNo(rs.getString("reg_no"));
        r.setSubjectName(rs.getString("subject_name"));
        r.setTermName(rs.getString("term_name"));
        return r;
    }
}
