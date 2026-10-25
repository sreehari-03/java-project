package com.srms.model;

public class AcademicTerm {
    private int id;
    private String termName;
    private String academicYear;
    private boolean isActive;

    public AcademicTerm() {}

    public AcademicTerm(int id, String termName, String academicYear, boolean isActive) {
        this.id = id;
        this.termName = termName;
        this.academicYear = academicYear;
        this.isActive = isActive;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTermName() { return termName; }
    public void setTermName(String termName) { this.termName = termName; }

    public String getAcademicYear() { return academicYear; }
    public void setAcademicYear(String academicYear) { this.academicYear = academicYear; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    @Override
    public String toString() {
        return termName;
    }
}
