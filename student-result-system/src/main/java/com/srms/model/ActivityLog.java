package com.srms.model;

import java.time.LocalDateTime;

public class ActivityLog {
    private int id;
    private Integer userId;
    private String actionType;
    private String description;
    private LocalDateTime createdAt;

    public ActivityLog() {}

    public ActivityLog(int id, Integer userId, String actionType, String description, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.actionType = actionType;
        this.description = description;
        this.createdAt = createdAt;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public String getActionType() { return actionType; }
    public void setActionType(String actionType) { this.actionType = actionType; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getTimeAgo() {
        if (createdAt == null) return "";
        long minutesAgo = java.time.temporal.ChronoUnit.MINUTES.between(createdAt, LocalDateTime.now());
        if (minutesAgo < 1) return "just now";
        if (minutesAgo < 60) return minutesAgo + " min ago";
        long hoursAgo = minutesAgo / 60;
        if (hoursAgo < 24) return hoursAgo + " hour" + (hoursAgo > 1 ? "s" : "") + " ago";
        return createdAt.toLocalDate().toString();
    }

    @Override
    public String toString() {
        return description + "   " + getTimeAgo();
    }
}
