package com.srms.dao;

import com.srms.config.DatabaseConnection;
import com.srms.exception.DatabaseException;
import com.srms.model.FeedbackRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.*;

/**
 * DAO for correction requests and topic assistance requests.
 */
public class FeedbackDAO {
    private static final Logger log = LoggerFactory.getLogger(FeedbackDAO.class);
    private final DatabaseConnection db = DatabaseConnection.getInstance();

    // ---- Correction Requests ----

    public List<FeedbackRequest> findAllCorrections() {
        if (!db.isAvailable()) return Collections.emptyList();
        final String sql = """
            SELECT cr.id, cr.student_id, cr.subject_id, NULL AS topic, cr.request_text, cr.status,
                   cr.teacher_response, cr.reviewed_by, cr.created_at, cr.updated_at,
                   s.full_name AS student_name, s.reg_no, sub.subject_name, 'correction' AS type
            FROM correction_requests cr
            JOIN students s ON cr.student_id = s.id
            JOIN subjects sub ON cr.subject_id = sub.id
            ORDER BY cr.created_at DESC
            """;
        return queryRequests(sql, null);
    }

    public List<FeedbackRequest> findCorrectionsByStudent(int studentId) {
        if (!db.isAvailable()) return Collections.emptyList();
        final String sql = """
            SELECT cr.id, cr.student_id, cr.subject_id, NULL AS topic, cr.request_text, cr.status,
                   cr.teacher_response, cr.reviewed_by, cr.created_at, cr.updated_at,
                   s.full_name AS student_name, s.reg_no, sub.subject_name, 'correction' AS type
            FROM correction_requests cr
            JOIN students s ON cr.student_id = s.id
            JOIN subjects sub ON cr.subject_id = sub.id
            WHERE cr.student_id = ?
            ORDER BY cr.created_at DESC
            """;
        return queryRequests(sql, studentId);
    }

    public int insertCorrection(int studentId, int subjectId, Integer termId, String requestText) {
        if (!db.isAvailable()) throw new DatabaseException("Database not connected");
        final String sql = "INSERT INTO correction_requests (student_id, subject_id, term_id, request_text, status) VALUES (?, ?, ?, ?, 'Pending')";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, studentId);
            ps.setInt(2, subjectId);
            if (termId != null) ps.setInt(3, termId); else ps.setNull(3, Types.INTEGER);
            ps.setString(4, requestText);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to insert correction request: " + e.getMessage(), e);
        }
        return -1;
    }

    public boolean updateCorrectionStatus(int id, String status, String response, Integer reviewedBy) {
        if (!db.isAvailable()) throw new DatabaseException("Database not connected");
        final String sql = "UPDATE correction_requests SET status = ?, teacher_response = ?, reviewed_by = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setString(2, response);
            if (reviewedBy != null) ps.setInt(3, reviewedBy); else ps.setNull(3, Types.INTEGER);
            ps.setInt(4, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update correction status: " + e.getMessage(), e);
        }
    }

    public int countPendingCorrections() {
        if (!db.isAvailable()) return 0;
        try (Connection conn = db.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM correction_requests WHERE status = 'Pending'")) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            log.error("countPendingCorrections: {}", e.getMessage());
            return 0;
        }
    }

    // ---- Topic Assistance Requests ----

    public List<FeedbackRequest> findAllAssistance() {
        if (!db.isAvailable()) return Collections.emptyList();
        final String sql = """
            SELECT ar.id, ar.student_id, ar.subject_id, ar.topic, ar.comments AS request_text, ar.status,
                   ar.teacher_response, ar.reviewed_by, ar.created_at, ar.updated_at,
                   s.full_name AS student_name, s.reg_no, sub.subject_name, 'assistance' AS type
            FROM assistance_requests ar
            JOIN students s ON ar.student_id = s.id
            JOIN subjects sub ON ar.subject_id = sub.id
            ORDER BY ar.created_at DESC
            """;
        return queryRequests(sql, null);
    }

    public List<FeedbackRequest> findAssistanceByStudent(int studentId) {
        if (!db.isAvailable()) return Collections.emptyList();
        final String sql = """
            SELECT ar.id, ar.student_id, ar.subject_id, ar.topic, ar.comments AS request_text, ar.status,
                   ar.teacher_response, ar.reviewed_by, ar.created_at, ar.updated_at,
                   s.full_name AS student_name, s.reg_no, sub.subject_name, 'assistance' AS type
            FROM assistance_requests ar
            JOIN students s ON ar.student_id = s.id
            JOIN subjects sub ON ar.subject_id = sub.id
            WHERE ar.student_id = ?
            ORDER BY ar.created_at DESC
            """;
        return queryRequests(sql, studentId);
    }

    public int insertAssistance(int studentId, int subjectId, String topic, String comments) {
        if (!db.isAvailable()) throw new DatabaseException("Database not connected");
        final String sql = "INSERT INTO assistance_requests (student_id, subject_id, topic, comments, status) VALUES (?, ?, ?, ?, 'Pending')";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, studentId);
            ps.setInt(2, subjectId);
            ps.setString(3, topic);
            ps.setString(4, comments);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to insert assistance request: " + e.getMessage(), e);
        }
        return -1;
    }

    public boolean updateAssistanceStatus(int id, String status, String response, Integer reviewedBy) {
        if (!db.isAvailable()) throw new DatabaseException("Database not connected");
        final String sql = "UPDATE assistance_requests SET status = ?, teacher_response = ?, reviewed_by = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setString(2, response);
            if (reviewedBy != null) ps.setInt(3, reviewedBy); else ps.setNull(3, Types.INTEGER);
            ps.setInt(4, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update assistance status: " + e.getMessage(), e);
        }
    }

    public int countPendingAssistance() {
        if (!db.isAvailable()) return 0;
        try (Connection conn = db.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM assistance_requests WHERE status = 'Pending'")) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            log.error("countPendingAssistance: {}", e.getMessage());
            return 0;
        }
    }

    private List<FeedbackRequest> queryRequests(String sql, Integer paramStudentId) {
        List<FeedbackRequest> list = new ArrayList<>();
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (paramStudentId != null) ps.setInt(1, paramStudentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Timestamp ca = rs.getTimestamp("created_at");
                    list.add(new FeedbackRequest(
                            rs.getInt("id"),
                            rs.getInt("student_id"),
                            rs.getInt("subject_id"),
                            rs.getString("topic"),
                            rs.getString("request_text"),
                            rs.getString("status"),
                            rs.getString("teacher_response"),
                            rs.getObject("reviewed_by", Integer.class),
                            ca != null ? ca.toLocalDateTime() : null,
                            rs.getString("student_name"),
                            rs.getString("reg_no"),
                            rs.getString("subject_name"),
                            rs.getString("type")
                    ));
                }
            }
        } catch (SQLException e) {
            log.error("queryRequests error: {}", e.getMessage());
        }
        return list;
    }
}
