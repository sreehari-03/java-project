package com.srms.model;

public class ClassRoom {
    private int id;
    private String className;
    private String section;
    private String academicYear;

    public ClassRoom() {}

    public ClassRoom(int id, String className, String section, String academicYear) {
        this.id = id;
        this.className = className;
        this.section = section;
        this.academicYear = academicYear;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }

    public String getSection() { return section; }
    public void setSection(String section) { this.section = section; }

    public String getAcademicYear() { return academicYear; }
    public void setAcademicYear(String academicYear) { this.academicYear = academicYear; }

    @Override
    public String toString() {
        return className + (section != null && !section.isEmpty() ? " (" + section + ")" : "");
    }
}
