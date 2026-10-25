package com.srms.dao;

import com.srms.config.DatabaseConnection;
import com.srms.exception.DatabaseException;
import com.srms.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * DAO for user authentication and management.
 */
public class UserDAO {
    private static final Logger log = LoggerFactory.getLogger(UserDAO.class);
    private final DatabaseConnection db = DatabaseConnection.getInstance();

    public Optional<User> findByUsername(String username) {
        if (!db.isAvailable()) return Optional.empty();
        final String sql = "SELECT id, username, password_hash, role, full_name, email, created_at, updated_at FROM users WHERE username = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapUser(rs));
                }
            }
        } catch (SQLException e) {
            log.error("findByUsername error for '{}': {}", username, e.getMessage());
        }
        return Optional.empty();
    }

    public Optional<User> findById(int id) {
        if (!db.isAvailable()) return Optional.empty();
        final String sql = "SELECT id, username, password_hash, role, full_name, email, created_at, updated_at FROM users WHERE id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapUser(rs));
                }
            }
        } catch (SQLException e) {
            log.error("findById error for id={}: {}", id, e.getMessage());
        }
        return Optional.empty();
    }

    public boolean existsByUsername(String username) {
        if (!db.isAvailable()) return false;
        final String sql = "SELECT COUNT(*) FROM users WHERE username = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            log.error("existsByUsername error: {}", e.getMessage());
        }
        return false;
    }

    public boolean existsByEmail(String email) {
        if (!db.isAvailable()) return false;
        final String sql = "SELECT COUNT(*) FROM users WHERE email = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            log.error("existsByEmail error: {}", e.getMessage());
        }
        return false;
    }

    /**
     * Inserts a new user record and returns the generated id.
     */
    public int insert(User user) {
        if (!db.isAvailable()) throw new DatabaseException("Database not connected");
        final String sql = "INSERT INTO users (username, password_hash, role, full_name, email) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPasswordHash());
            ps.setString(3, user.getRole());
            ps.setString(4, user.getFullName());
            ps.setString(5, user.getEmail());
            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) return keys.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to create user: " + e.getMessage(), e);
        }
        throw new DatabaseException("User creation returned no ID");
    }

    public void updatePasswordHash(int userId, String newHash) {
        if (!db.isAvailable()) throw new DatabaseException("Database not connected");
        final String sql = "UPDATE users SET password_hash = ? WHERE id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newHash);
            ps.setInt(2, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update password: " + e.getMessage(), e);
        }
    }

    private User mapUser(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getInt("id"));
        u.setUsername(rs.getString("username"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setRole(rs.getString("role"));
        u.setFullName(rs.getString("full_name"));
        u.setEmail(rs.getString("email"));
        Timestamp ca = rs.getTimestamp("created_at");
        if (ca != null) u.setCreatedAt(ca.toLocalDateTime());
        Timestamp ua = rs.getTimestamp("updated_at");
        if (ua != null) u.setUpdatedAt(ua.toLocalDateTime());
        return u;
    }
}
