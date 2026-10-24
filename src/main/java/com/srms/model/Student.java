package com.srms.model;

public class Student {
    public final String reg;
    public String name, cls, email, school, status = "Active";

    public Student(String reg, String name, String cls, String email, String school) {
        this.reg = reg; this.name = name; this.cls = cls; this.email = email; this.school = school;
    }

    @Override public String toString() { return reg + " · " + name; }
}
