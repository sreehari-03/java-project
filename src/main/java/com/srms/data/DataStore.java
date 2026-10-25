package com.srms.data;

import com.srms.model.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.LocalDate;
import java.util.*;

/**
 * In-memory data layer so the UI runs stand-alone.
 * TODO: replace the methods below with JDBC/MySQL DAO calls (the controllers only talk to this class).
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
        accounts.add(new Account("student", "student123", "student", "Aarav Nair", "STU001"));
        String sc = "St. Mary’s High School";
        students.addAll(
                new Student("STU001", "Aarav Nair", "10-A", "aarav@school.edu", sc),
                new Student("STU002", "Diya Thomas", "10-A", "diya@school.edu", sc),
                new Student("STU003", "Rohan Varma", "10-B", "rohan@school.edu", sc),
                new Student("STU004", "Meera Das", "9-A", "meera@school.edu", sc),
                new Student("STU005", "Kabir Singh", "9-B", "kabir@school.edu", sc),
                new Student("STU006", "Anjali Raj", "10-B", "anjali@school.edu", sc));
        results.put(key("STU001", "Term 1"), new int[]{78, 66, 82, 71, 90});
        results.put(key("STU002", "Term 1"), new int[]{88, 92, 79, 85, 94});
        results.put(key("STU003", "Term 1"), new int[]{40, 33, 58, 49, 62});
        results.put(key("STU004", "Term 1"), new int[]{67, 71, 75, 60, 80});
        results.put(key("STU005", "Term 1"), new int[]{55, 48, 60, 52, 70});
        remarks.put(key("STU001", "Term 1"), "Strong in computing. Science needs revision of electricity and chemical reactions.");
        remarks.put(key("STU003", "Term 1"), "Needs daily practice in algebra and science concepts. Recommend extra tutorials.");
        corrections.add(new Request(seq++, "STU003", "Science", "", "Question 5 seems marked wrongly.", "2026-10-12"));
        topics.add(new Request(seq++, "STU001", "Science", "Electricity", "Need help with circuits.", "2026-10-15"));
        activity.addAll("Marks updated for Rohan Varma", "New student registered: Meera Das", "Correction request received");
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
    public String nameOf(String reg) { return find(reg).map(s -> s.name).orElse(reg); }
    public void deleteStudent(Student s) {
        students.remove(s);
        results.keySet().removeIf(k -> k.startsWith(s.reg + "|"));
        remarks.keySet().removeIf(k -> k.startsWith(s.reg + "|"));
    }

    // ---- results & remarks
    public int[] getResult(String reg, String term) { return results.get(key(reg, term)); }
    public String getRemark(String reg, String term) { return remarks.getOrDefault(key(reg, term), ""); }
    public void saveResult(String reg, String term, int[] marks, String remark) {
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
