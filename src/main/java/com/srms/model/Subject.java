package com.srms.model;

public class Subject {
    private int id;
    private String subjectCode;
    private String subjectName;
    private int maxMarks;
    private int passMarks;
    private String description;

    public Subject() {
        this.maxMarks = 100;
        this.passMarks = 35;
    }

    public Subject(int id, String subjectCode, String subjectName, int maxMarks, int passMarks, String description) {
        this.id = id;
        this.subjectCode = subjectCode;
        this.subjectName = subjectName;
        this.maxMarks = maxMarks;
        this.passMarks = passMarks;
        this.description = description;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getSubjectCode() { return subjectCode; }
    public void setSubjectCode(String subjectCode) { this.subjectCode = subjectCode; }

    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }

    public int getMaxMarks() { return maxMarks; }
    public void setMaxMarks(int maxMarks) { this.maxMarks = maxMarks; }

    public int getPassMarks() { return passMarks; }
    public void setPassMarks(int passMarks) { this.passMarks = passMarks; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    @Override
    public String toString() {
        return subjectName;
    }
}
