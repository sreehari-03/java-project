package com.srms.data;

import com.srms.model.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;

/**
 * Shared application state. Results are loaded from and saved to MySQL.
 */
public final class DataStore {
    public static final DataStore I = new DataStore();

    public static final List<String> SUBJECTS = List.of("Mathematics", "Science", "English", "Social Studies", "Computer Science");
    public static final List<String> TERMS = List.of("Term 1", "Term 2", "Final");
    public static final int PASS = 35;
    public static final Map<String, List<String>> TOPICS = Map.of(
            "Mathematics", List.of("Algebra", "Geometry", "Trigonometry"),
            "Science", List.of("Electricity", "Chemical Reactions", "Cell Biology"),
            "English", List.of("Grammar", "Essay Writing", "Comprehension"),
            "Social Studies", List.of("Civics", "Geography", "History"),
            "Computer Science", List.of("Programming", "Databases", "Networking"));

    public final ObservableList<Student> students = FXCollections.observableArrayList();
    public final ObservableList<Request> corrections = FXCollections.observableArrayList();
    public final ObservableList<Request> topics = FXCollections.observableArrayList();
    public final ObservableList<String> activity = FXCollections.observableArrayList();
    public final List<Account> accounts = new ArrayList<>();
    public Account current;

    private final Map<String, int[]> results = new HashMap<>();
    private final Map<String, String> remarks = new HashMap<>();
    private int seq = 1;

    private DataStore() {
        accounts.add(new Account("teacher", "teacher123", "teacher", "Ms. Priya Menon", "TCH001"));
    }

    private static String key(String reg, String term) { return reg + "|" + term; }

    // ---- accounts
    public Account authenticate(String user, String pass) {
        return accounts.stream().filter(a -> a.username().equals(user) && a.password().equals(pass)).findFirst().orElse(null);
    }
    public boolean usernameTaken(String u) { return accounts.stream().anyMatch(a -> a.username().equalsIgnoreCase(u)); }
    public boolean regTaken(String r) { return students.stream().anyMatch(s -> s.reg.equalsIgnoreCase(r)); }

    // ---- students
    public Optional<Student> find(String reg) { return students.stream().filter(s -> s.reg.equals(reg)).findFirst(); }
    public void saveStudent(Student student) {
        OptionalInt index = java.util.stream.IntStream.range(0, students.size())
                .filter(i -> students.get(i).reg.equals(student.reg))
                .findFirst();
        if (index.isPresent()) students.set(index.getAsInt(), student);
        else students.add(student);
    }
    public String nameOf(String reg) { return find(reg).map(s -> s.name).orElse(reg); }
    public void deleteStudent(Student s) {
        students.remove(s);
        results.keySet().removeIf(k -> k.startsWith(s.reg + "|"));
        remarks.keySet().removeIf(k -> k.startsWith(s.reg + "|"));
    }

    // ---- results & remarks
    public int[] getResult(String reg, String term) { return results.get(key(reg, term)); }
    public String getRemark(String reg, String term) { return remarks.getOrDefault(key(reg, term), ""); }
    public void loadResultsFromDatabase() throws SQLException {
        results.clear();
        remarks.clear();
        for (ResultService.SavedResult saved : ResultService.findAll()) {
            String resultKey = key(saved.admissionNo(), saved.term());
            if (saved.marks() != null) {
                results.put(resultKey, saved.marks());
            }
            if (saved.remark() != null && !saved.remark().isBlank()) {
                remarks.put(resultKey, saved.remark());
            }
        }
    }

    public void saveTeacherFeedback(String reg, String term, String feedback) throws SQLException {
        ResultService.saveTeacherFeedback(reg, term, feedback);
        remarks.put(key(reg, term), feedback);
        addActivity("Teacher feedback added for " + nameOf(reg));
    }
    public void saveResult(String reg, String term, int[] marks, String remark) throws SQLException {
        ResultService.save(reg, term, marks, remark);
        results.put(key(reg, term), marks);
        remarks.put(key(reg, term), remark);
    }
    public double average(String reg, String term) {
        int[] m = getResult(reg, term);
        return m == null ? -1 : Arrays.stream(m).average().orElse(-1);
    }
    public String latestTerm(String reg) {
        for (int i = TERMS.size() - 1; i >= 0; i--) if (getResult(reg, TERMS.get(i)) != null) return TERMS.get(i);
        return null;
    }
    public long resultsEntered() { return results.keySet().stream().map(k -> k.split("\\|")[0]).distinct().count(); }
    /** Rows of {reg, term, remark} for non-empty remarks. */
    public List<String[]> remarkRows() {
        List<String[]> l = new ArrayList<>();
        remarks.forEach((k, v) -> { if (!v.isBlank()) { String[] p = k.split("\\|"); l.add(new String[]{p[0], p[1], v}); } });
        l.sort(Comparator.comparing(r -> r[0]));
        return l;
    }

    // ---- requests
    public void addCorrection(String reg, String subject, String msg) {
        corrections.add(new Request(seq++, reg, subject, "", msg, LocalDate.now().toString()));
        addActivity("Correction request from " + nameOf(reg));
    }
    public void addTopic(String reg, String subject, String topic, String msg) {
        topics.add(new Request(seq++, reg, subject, topic, msg, LocalDate.now().toString()));
        addActivity("Topic request from " + nameOf(reg));
    }
    public void addActivity(String text) { activity.add(0, text); }
}
