package com.srms.dao;

import com.srms.config.DatabaseConnection;
import com.srms.exception.DatabaseException;
import com.srms.model.TeacherRemark;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.*;

/**
 * DAO for Teacher Remarks on student performance.
 */
public class RemarkDAO {
    private static final Logger log = LoggerFactory.getLogger(RemarkDAO.class);
    private final DatabaseConnection db = DatabaseConnection.getInstance();

    public List<TeacherRemark> findAll() {
        if (!db.isAvailable()) return Collections.emptyList();
        final String sql = """
            SELECT tr.id, tr.student_id, tr.subject_id, tr.term_id, tr.teacher_id, tr.remark_text,
                   tr.created_at, tr.updated_at,
                   s.full_name AS student_name, s.reg_no,
                   sub.subject_name, t.term_name, u.full_name AS teacher_name
            FROM teacher_remarks tr
            JOIN students s ON tr.student_id = s.id
            LEFT JOIN subjects sub ON tr.subject_id = sub.id
            JOIN academic_terms t ON tr.term_id = t.id
            LEFT JOIN users u ON tr.teacher_id = u.id
            ORDER BY tr.updated_at DESC
            """;
        List<TeacherRemark> list = new ArrayList<>();
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRemark(rs));
        } catch (SQLException e) {
            log.error("findAll remarks error: {}", e.getMessage());
        }
        return list;
    }

    public List<TeacherRemark> findByStudentAndTerm(int studentId, int termId) {
        if (!db.isAvailable()) return Collections.emptyList();
        final String sql = """
            SELECT tr.id, tr.student_id, tr.subject_id, tr.term_id, tr.teacher_id, tr.remark_text,
                   tr.created_at, tr.updated_at,
                   s.full_name AS student_name, s.reg_no,
                   sub.subject_name, t.term_name, u.full_name AS teacher_name
            FROM teacher_remarks tr
            JOIN students s ON tr.student_id = s.id
            LEFT JOIN subjects sub ON tr.subject_id = sub.id
            JOIN academic_terms t ON tr.term_id = t.id
            LEFT JOIN users u ON tr.teacher_id = u.id
            WHERE tr.student_id = ? AND tr.term_id = ?
            ORDER BY sub.subject_name
            """;
        List<TeacherRemark> list = new ArrayList<>();
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, termId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRemark(rs));
            }
        } catch (SQLException e) {
            log.error("findByStudentAndTerm remarks error: {}", e.getMessage());
        }
        return list;
    }

    /**
     * Returns a single overall remark for student+term (no subject filter).
     */
    public Optional<String> getOverallRemark(int studentId, int termId) {
        if (!db.isAvailable()) return Optional.empty();
        final String sql = """
            SELECT remark_text FROM teacher_remarks
            WHERE student_id = ? AND term_id = ? AND subject_id IS NULL
            ORDER BY updated_at DESC LIMIT 1
            """;
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, termId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(rs.getString("remark_text"));
            }
        } catch (SQLException e) {
            log.error("getOverallRemark error: {}", e.getMessage());
        }
        return Optional.empty();
    }

    /**
     * Upsert overall remark for a student+term (no subject).
     */
    public boolean upsertOverallRemark(int studentId, int termId, Integer teacherId, String remarkText) {
        if (!db.isAvailable()) throw new DatabaseException("Database not connected");
        // Try update first
        final String updateSql = """
            UPDATE teacher_remarks SET remark_text = ?, teacher_id = ?, updated_at = CURRENT_TIMESTAMP
            WHERE student_id = ? AND term_id = ? AND subject_id IS NULL
            """;
        try (Connection conn = db.getConnection();
             PreparedStatement upd = conn.prepareStatement(updateSql)) {
            upd.setString(1, remarkText);
            if (teacherId != null) upd.setInt(2, teacherId); else upd.setNull(2, Types.INTEGER);
            upd.setInt(3, studentId);
            upd.setInt(4, termId);
            int rows = upd.executeUpdate();
            if (rows > 0) return true;
        } catch (SQLException e) {
            log.warn("upsertOverallRemark update phase: {}", e.getMessage());
        }
        // Insert
        final String insertSql = "INSERT INTO teacher_remarks (student_id, subject_id, term_id, teacher_id, remark_text) VALUES (?, NULL, ?, ?, ?)";
        try (Connection conn = db.getConnection();
             PreparedStatement ins = conn.prepareStatement(insertSql)) {
            ins.setInt(1, studentId);
            ins.setInt(2, termId);
            if (teacherId != null) ins.setInt(3, teacherId); else ins.setNull(3, Types.INTEGER);
            ins.setString(4, remarkText);
            return ins.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to upsert remark: " + e.getMessage(), e);
        }
    }

    public boolean updateById(int id, String newText) {
        if (!db.isAvailable()) throw new DatabaseException("Database not connected");
        final String sql = "UPDATE teacher_remarks SET remark_text = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newText);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update remark: " + e.getMessage(), e);
        }
    }

    private TeacherRemark mapRemark(ResultSet rs) throws SQLException {
        TeacherRemark r = new TeacherRemark();
        r.setId(rs.getInt("id"));
        r.setStudentId(rs.getInt("student_id"));
        int subId = rs.getInt("subject_id");
        r.setSubjectId(rs.wasNull() ? null : subId);
        r.setTermId(rs.getInt("term_id"));
        int tid = rs.getInt("teacher_id");
        r.setTeacherId(rs.wasNull() ? null : tid);
        r.setRemarkText(rs.getString("remark_text"));
        Timestamp ca = rs.getTimestamp("created_at");
        if (ca != null) r.setCreatedAt(ca.toLocalDateTime());
        Timestamp ua = rs.getTimestamp("updated_at");
        if (ua != null) r.setUpdatedAt(ua.toLocalDateTime());
        r.setStudentName(rs.getString("student_name"));
        r.setRegNo(rs.getString("reg_no"));
        r.setSubjectName(rs.getString("subject_name"));
        r.setTermName(rs.getString("term_name"));
        r.setTeacherName(rs.getString("teacher_name"));
        return r;
    }
}
