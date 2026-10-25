package com.srms.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class MarkRow {
    private final String subject;
    private final StringProperty marks = new SimpleStringProperty("");

    public MarkRow(String subject, String marks) { this.subject = subject; this.marks.set(marks); }

    public String getSubject() { return subject; }
    public StringProperty marksProperty() { return marks; }
    public String getMarks() { return marks.get(); }
    public void setMarks(String m) { marks.set(m); }

    public static Integer parse(String s) {
        try { return s == null ? null : Integer.valueOf(s.trim()); } catch (NumberFormatException e) { return null; }
    }
}
