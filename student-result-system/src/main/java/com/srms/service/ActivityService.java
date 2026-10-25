package com.srms.service;

import com.srms.config.DatabaseConnection;
import com.srms.dao.ActivityLogDAO;
import com.srms.model.ActivityLog;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Service for fetching and adding dashboard activity log items.
 */
public class ActivityService {
    private final ActivityLogDAO activityDAO = new ActivityLogDAO();

    // In-memory fallback
    private static final List<String> inMemActivity = new ArrayList<>(Arrays.asList(
            "Result updated for Rohan Mehta (Class 10)       2 hours ago",
            "Teacher remark added for Sneha Iyer (Class 11)  3 hours ago",
            "Correction request from Arjun Das (Class 12)    5 hours ago",
            "Topic assistance request from Priya Nair         6 hours ago"
    ));

    public List<ActivityLog> getRecentActivity(int limit) {
        if (!DatabaseConnection.getInstance().isAvailable()) return Collections.emptyList();
        return activityDAO.findRecent(limit);
    }

    public List<String> getRecentActivityStrings(int limit) {
        if (!DatabaseConnection.getInstance().isAvailable()) {
            return inMemActivity.stream().limit(limit).toList();
        }
        List<ActivityLog> logs = activityDAO.findRecent(limit);
        List<String> result = new ArrayList<>();
        for (ActivityLog al : logs) {
            result.add(al.toString());
        }
        return result;
    }

    public void addActivity(String description, String actionType) {
        Integer uid = AuthService.getCurrentUser() != null ? AuthService.getCurrentUser().getId() : null;
        if (DatabaseConnection.getInstance().isAvailable()) {
            activityDAO.log(uid, actionType, description);
        } else {
            inMemActivity.add(0, description + "   just now");
        }
    }
}
