package com.srms.dao;

import com.srms.config.DatabaseConnection;
import com.srms.exception.DatabaseException;
import com.srms.model.ActivityLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.*;

public class ActivityLogDAO {
    private static final Logger log = LoggerFactory.getLogger(ActivityLogDAO.class);
    private final DatabaseConnection db = DatabaseConnection.getInstance();

    public List<ActivityLog> findRecent(int limit) {
        if (!db.isAvailable()) return Collections.emptyList();
        final String sql = "SELECT id, user_id, action_type, description, created_at FROM activity_logs ORDER BY created_at DESC LIMIT ?";
        List<ActivityLog> list = new ArrayList<>();
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ActivityLog al = new ActivityLog();
                    al.setId(rs.getInt("id"));
                    int uid = rs.getInt("user_id");
                    al.setUserId(rs.wasNull() ? null : uid);
                    al.setActionType(rs.getString("action_type"));
                    al.setDescription(rs.getString("description"));
                    Timestamp ca = rs.getTimestamp("created_at");
                    if (ca != null) al.setCreatedAt(ca.toLocalDateTime());
                    list.add(al);
                }
            }
        } catch (SQLException e) {
            log.error("findRecent activityLogs error: {}", e.getMessage());
        }
        return list;
    }

    public void log(Integer userId, String actionType, String description) {
        if (!db.isAvailable()) return;
        final String sql = "INSERT INTO activity_logs (user_id, action_type, description) VALUES (?, ?, ?)";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (userId != null) ps.setInt(1, userId); else ps.setNull(1, Types.INTEGER);
            ps.setString(2, actionType);
            ps.setString(3, description);
            ps.executeUpdate();
        } catch (SQLException e) {
            log.warn("ActivityLog insert failed: {}", e.getMessage());
        }
    }
}
