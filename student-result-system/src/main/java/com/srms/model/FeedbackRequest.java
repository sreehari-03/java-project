package com.srms.model;

import java.time.LocalDateTime;

/** A correction request or topic assistance request, shared by both portals. */
public class FeedbackRequest {
    private int id;
    private int studentId;
    private int subjectId;
    private String topic; // null/empty for correction requests
    private String requestText;
    private String status; // 'Pending', 'In Progress', 'Resolved', 'Rejected'
    private String teacherResponse;
    private Integer reviewedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // View fields
    private String studentName;
    private String regNo;
    private String subjectName;
    private Integer termId;
    private String termName;
    private String type; // 'correction' or 'assistance'

    // Kept for legacy compatibility
    public final int legacyId;
    public final String reg;
    public final String subject;
    public String status_legacy;
    public final String message;
    public final String date;

    public FeedbackRequest() {
        this.legacyId = 0;
        this.reg = "";
        this.subject = "";
        this.message = "";
        this.date = "";
        this.status_legacy = "Pending";
        this.status = "Pending";
    }

    public FeedbackRequest(int id, int studentId, int subjectId, String topic, String requestText,
                           String status, String teacherResponse, Integer reviewedBy,
                           LocalDateTime createdAt, String studentName, String regNo,
                           String subjectName, String type) {
        this.id = id;
        this.studentId = studentId;
        this.subjectId = subjectId;
        this.topic = topic;
        this.requestText = requestText;
        this.status = status;
        this.status_legacy = status;
        this.teacherResponse = teacherResponse;
        this.reviewedBy = reviewedBy;
        this.createdAt = createdAt;
        this.studentName = studentName;
        this.regNo = regNo;
        this.subjectName = subjectName;
        this.type = type;
        this.message = requestText;
        this.date = createdAt != null ? createdAt.toLocalDate().toString() : "";
        this.legacyId = id;
        this.reg = regNo != null ? regNo : "";
        this.subject = subjectName != null ? subjectName : "";
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public int getSubjectId() { return subjectId; }
    public void setSubjectId(int subjectId) { this.subjectId = subjectId; }

    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }

    public String getRequestText() { return requestText; }
    public void setRequestText(String requestText) { this.requestText = requestText; }

    public String getStatus() { return status; }
    public void setStatus(String status) {
        this.status = status;
        this.status_legacy = status;
    }

    public String getTeacherResponse() { return teacherResponse; }
    public void setTeacherResponse(String teacherResponse) { this.teacherResponse = teacherResponse; }

    public Integer getReviewedBy() { return reviewedBy; }
    public void setReviewedBy(Integer reviewedBy) { this.reviewedBy = reviewedBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getRegNo() { return regNo; }
    public void setRegNo(String regNo) {}

    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }

    public Integer getTermId() { return termId; }
    public void setTermId(Integer termId) { this.termId = termId; }

    public String getTermName() { return termName; }
    public void setTermName(String termName) { this.termName = termName; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getCreatedAtString() {
        return createdAt != null ? createdAt.toLocalDate().toString() : "";
    }
}
