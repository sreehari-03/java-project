package com.srms.dao;

import com.srms.config.DatabaseConnection;
import com.srms.exception.DatabaseException;
import com.srms.model.Student;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.*;

/**
 * DAO for Student CRUD operations.
 */
public class StudentDAO {
    private static final Logger log = LoggerFactory.getLogger(StudentDAO.class);
    private final DatabaseConnection db = DatabaseConnection.getInstance();

    /**
     * Returns all students joined with their class name.
     */
    public List<Student> findAll() {
        if (!db.isAvailable()) return Collections.emptyList();
        final String sql = """
            SELECT s.id, s.user_id, s.reg_no, s.full_name, s.class_id,
                   CONCAT(c.class_name, ' (', c.section, ')') AS class_display,
                   s.email, s.phone, s.school_name, s.status, s.created_at, s.updated_at
            FROM students s
            JOIN classes c ON s.class_id = c.id
            ORDER BY s.full_name
            """;
        List<Student> list = new ArrayList<>();
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapStudent(rs));
        } catch (SQLException e) {
            log.error("findAll students error: {}", e.getMessage());
        }
        return list;
    }

    public Optional<Student> findById(int id) {
        if (!db.isAvailable()) return Optional.empty();
        final String sql = """
            SELECT s.id, s.user_id, s.reg_no, s.full_name, s.class_id,
                   CONCAT(c.class_name, ' (', c.section, ')') AS class_display,
                   s.email, s.phone, s.school_name, s.status, s.created_at, s.updated_at
            FROM students s
            JOIN classes c ON s.class_id = c.id
            WHERE s.id = ?
            """;
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapStudent(rs));
            }
        } catch (SQLException e) {
            log.error("findById student error: {}", e.getMessage());
        }
        return Optional.empty();
    }

    public Optional<Student> findByRegNo(String regNo) {
        if (!db.isAvailable()) return Optional.empty();
        final String sql = """
            SELECT s.id, s.user_id, s.reg_no, s.full_name, s.class_id,
                   CONCAT(c.class_name, ' (', c.section, ')') AS class_display,
                   s.email, s.phone, s.school_name, s.status, s.created_at, s.updated_at
            FROM students s
            JOIN classes c ON s.class_id = c.id
            WHERE s.reg_no = ?
            """;
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, regNo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapStudent(rs));
            }
        } catch (SQLException e) {
            log.error("findByRegNo error: {}", e.getMessage());
        }
        return Optional.empty();
    }

    public Optional<Student> findByUserId(int userId) {
        if (!db.isAvailable()) return Optional.empty();
        final String sql = """
            SELECT s.id, s.user_id, s.reg_no, s.full_name, s.class_id,
                   CONCAT(c.class_name, ' (', c.section, ')') AS class_display,
                   s.email, s.phone, s.school_name, s.status, s.created_at, s.updated_at
            FROM students s
            JOIN classes c ON s.class_id = c.id
            WHERE s.user_id = ?
            """;
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapStudent(rs));
            }
        } catch (SQLException e) {
            log.error("findByUserId error: {}", e.getMessage());
        }
        return Optional.empty();
    }

    public boolean existsByRegNo(String regNo) {
        if (!db.isAvailable()) return false;
        final String sql = "SELECT COUNT(*) FROM students WHERE reg_no = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, regNo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            log.error("existsByRegNo error: {}", e.getMessage());
        }
        return false;
    }

    public boolean existsByEmail(String email) {
        if (!db.isAvailable()) return false;
        final String sql = "SELECT COUNT(*) FROM students WHERE email = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            log.error("existsByEmail student error: {}", e.getMessage());
        }
        return false;
    }

    /**
     * Inserts student and returns its generated id.
     */
    public int insert(Student s) {
        if (!db.isAvailable()) throw new DatabaseException("Database not connected");
        final String sql = "INSERT INTO students (user_id, reg_no, full_name, class_id, email, phone, school_name, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            if (s.getUserId() != null) ps.setInt(1, s.getUserId()); else ps.setNull(1, Types.INTEGER);
            ps.setString(2, s.getReg());
            ps.setString(3, s.getName());
            ps.setInt(4, s.getClassId());
            ps.setString(5, s.getEmail());
            ps.setString(6, s.getPhone());
            ps.setString(7, s.getSchool() != null ? s.getSchool() : "Sunrise Public School");
            ps.setString(8, s.getStatus() != null ? s.getStatus() : "Active");
            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) return keys.getInt(1);
                }
            }
        } catch (SQLException e) {
            log.error("Student insert failed (SQLState={}, errorCode={}): {}",
                    e.getSQLState(), e.getErrorCode(), e.getMessage(), e);
            throw new DatabaseException("Failed to insert student: " + e.getMessage(), e);
        }
        throw new DatabaseException("Student insert returned no ID");
    }

    public boolean update(Student s) {
        if (!db.isAvailable()) throw new DatabaseException("Database not connected");
        final String sql = "UPDATE students SET full_name = ?, class_id = ?, email = ?, phone = ?, school_name = ?, status = ? WHERE id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, s.getName());
            ps.setInt(2, s.getClassId());
            ps.setString(3, s.getEmail());
            ps.setString(4, s.getPhone());
            ps.setString(5, s.getSchool());
            ps.setString(6, s.getStatus());
            ps.setInt(7, s.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update student: " + e.getMessage(), e);
        }
    }

    public boolean updateStatus(int studentId, String status) {
        if (!db.isAvailable()) throw new DatabaseException("Database not connected");
        final String sql = "UPDATE students SET status = ? WHERE id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, studentId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update student status: " + e.getMessage(), e);
        }
    }

    public boolean delete(int studentId) {
        if (!db.isAvailable()) throw new DatabaseException("Database not connected");
        final String sql = "DELETE FROM students WHERE id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to delete student: " + e.getMessage(), e);
        }
    }

    public int countAll() {
        if (!db.isAvailable()) return 0;
        try (Connection conn = db.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM students WHERE status = 'Active'")) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            log.error("countAll students error: {}", e.getMessage());
            return 0;
        }
    }

    /**
     * Returns list of [class_display, student_count] for bar chart.
     */
    public List<String[]> countByClass() {
        if (!db.isAvailable()) return Collections.emptyList();
        final String sql = """
            SELECT CONCAT(c.class_name, ' (', c.section, ')') AS class_display, COUNT(s.id) AS cnt
            FROM students s
            JOIN classes c ON s.class_id = c.id
            WHERE s.status = 'Active'
            GROUP BY c.id, class_display
            ORDER BY class_display
            """;
        List<String[]> rows = new ArrayList<>();
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                rows.add(new String[]{rs.getString("class_display"), rs.getString("cnt")});
            }
        } catch (SQLException e) {
            log.error("countByClass error: {}", e.getMessage());
        }
        return rows;
    }

    private Student mapStudent(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        int userId = rs.getInt("user_id");
        Integer userIdBox = rs.wasNull() ? null : userId;
        String regNo = rs.getString("reg_no");
        String fullName = rs.getString("full_name");
        int classId = rs.getInt("class_id");
        String classDisplay = rs.getString("class_display");
        String email = rs.getString("email");
        String phone = rs.getString("phone");
        String school = rs.getString("school_name");
        String status = rs.getString("status");
        Student s = new Student(id, userIdBox, regNo, fullName, classId, classDisplay, email, phone, school, status);
        Timestamp ca = rs.getTimestamp("created_at");
        if (ca != null) s.setCreatedAt(ca.toLocalDateTime());
        Timestamp ua = rs.getTimestamp("updated_at");
        if (ua != null) s.setUpdatedAt(ua.toLocalDateTime());
        return s;
    }
}
