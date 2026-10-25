package com.srms.model;

import java.time.LocalDateTime;

public class StudentResult {
    private int id;
    private int studentId;
    private int subjectId;
    private int termId;
    private int marksObtained;
    private int maxMarks;
    private boolean isPass;
    private String teacherRemarks;
    private Integer enteredBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Display fields for views
    private String studentName;
    private String regNo;
    private String subjectName;
    private String termName;

    public StudentResult() {
        this.maxMarks = 100;
        this.isPass = true;
    }

    public StudentResult(int id, int studentId, int subjectId, int termId, int marksObtained, int maxMarks, boolean isPass, String teacherRemarks) {
        this.id = id;
        this.studentId = studentId;
        this.subjectId = subjectId;
        this.termId = termId;
        this.marksObtained = marksObtained;
        this.maxMarks = maxMarks;
        this.isPass = isPass;
        this.teacherRemarks = teacherRemarks;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public int getSubjectId() { return subjectId; }
    public void setSubjectId(int subjectId) { this.subjectId = subjectId; }

    public int getTermId() { return termId; }
    public void setTermId(int termId) { this.termId = termId; }

    public int getMarksObtained() { return marksObtained; }
    public void setMarksObtained(int marksObtained) { this.marksObtained = marksObtained; }

    public int getMaxMarks() { return maxMarks; }
    public void setMaxMarks(int maxMarks) { this.maxMarks = maxMarks; }

    public boolean isPass() { return isPass; }
    public void setPass(boolean pass) { isPass = pass; }

    public String getTeacherRemarks() { return teacherRemarks; }
    public void setTeacherRemarks(String teacherRemarks) { this.teacherRemarks = teacherRemarks; }

    public Integer getEnteredBy() { return enteredBy; }
    public void setEnteredBy(Integer enteredBy) { this.enteredBy = enteredBy; }

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

    public String getStatus() {
        return isPass ? "Pass" : "Fail";
    }

    public double getPercentage() {
        return maxMarks > 0 ? (marksObtained * 100.0) / maxMarks : 0.0;
    }
}
