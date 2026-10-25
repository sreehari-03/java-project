package com.srms.model;

import java.time.LocalDateTime;

public class TeacherRemark {
    private int id;
    private int studentId;
    private Integer subjectId;
    private int termId;
    private Integer teacherId;
    private String remarkText;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // View fields
    private String studentName;
    private String regNo;
    private String subjectName;
    private String termName;
    private String teacherName;

    public TeacherRemark() {}

    public TeacherRemark(int id, int studentId, Integer subjectId, int termId, Integer teacherId, String remarkText) {
        this.id = id;
        this.studentId = studentId;
        this.subjectId = subjectId;
        this.termId = termId;
        this.teacherId = teacherId;
        this.remarkText = remarkText;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public Integer getSubjectId() { return subjectId; }
    public void setSubjectId(Integer subjectId) { this.subjectId = subjectId; }

    public int getTermId() { return termId; }
    public void setTermId(int termId) { this.termId = termId; }

    public Integer getTeacherId() { return teacherId; }
    public void setTeacherId(Integer teacherId) { this.teacherId = teacherId; }

    public String getRemarkText() { return remarkText; }
    public void setRemarkText(String remarkText) { this.remarkText = remarkText; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getRegNo() { return regNo; }
    public void setRegNo(String regNo) { this.regNo = regNo; }

    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }

    public String getTermName() { return termName; }
    public void setTermName(String termName) { this.termName = termName; }

    public String getTeacherName() { return teacherName; }
    public void setTeacherName(String teacherName) { this.teacherName = teacherName; }
}
