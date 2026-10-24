package com.srms.controller;

import com.srms.Nav;
import com.srms.data.DataStore;
import com.srms.model.MarkRow;
import com.srms.model.Request;
import com.srms.model.Student;
import com.srms.util.Ui;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

import java.util.*;

public class TeacherPortalController {
    private final DataStore ds = DataStore.I;
    private Map<ToggleButton, Node> pages;
    private FilteredList<Student> filtered;

    // navigation
    @FXML private ToggleGroup navGroup;
    @FXML private ToggleButton navDash, navStudents, navResults, navRemarks, navFeedback, navSettings;
    @FXML private Label teacherName;
    @FXML private Node dashPage, studentsPage, resultPage, remarksPage, feedbackPage, settingsPage;
    // dashboard
    @FXML private Label kpiStudents, kpiResults, kpiPending, kpiCorrections;
    @FXML private BarChart<String, Number> classChart;
    @FXML private ListView<String> activityList;
    // students
    @FXML private TextField searchField;
    @FXML private ComboBox<String> classFilter;
    @FXML private TableView<Student> studentTable;
    // results
    @FXML private ComboBox<Student> studentBox;
    @FXML private ComboBox<String> termBox;
    @FXML private TableView<MarkRow> marksTable;
    @FXML private TextArea remarksArea;
    // remarks + feedback
    @FXML private TableView<String[]> remarksTable;
    @FXML private TableView<Request> corrTable, topicTable;

    @FXML
    private void initialize() {
        teacherName.setText(ds.current.name() + " · Teacher / Admin");
        pages = new LinkedHashMap<>();
        pages.put(navDash, dashPage); pages.put(navStudents, studentsPage); pages.put(navResults, resultPage);
        pages.put(navRemarks, remarksPage); pages.put(navFeedback, feedbackPage); pages.put(navSettings, settingsPage);

        setupStudentTable();
        setupMarksTable();
        setupRemarksTable();
        setupRequestTable(corrTable, ds.corrections, false);
        setupRequestTable(topicTable, ds.topics, true);

        activityList.setItems(ds.activity);
        studentBox.setItems(ds.students);
        termBox.setItems(FXCollections.observableArrayList(DataStore.TERMS));
        studentBox.getSelectionModel().selectFirst();
        termBox.getSelectionModel().selectFirst();
        studentBox.valueProperty().addListener((o, a, b) -> loadMarks());
        termBox.valueProperty().addListener((o, a, b) -> loadMarks());

        navGroup.selectedToggleProperty().addListener((o, a, n) -> { if (n == null) a.setSelected(true); else show(n); });
        navDash.setSelected(true);
    }

    private void show(Toggle t) {
        pages.forEach((b, p) -> { boolean on = b == t; p.setVisible(on); p.setManaged(on); });
        if (t == navDash) refreshDashboard();
        else if (t == navStudents) refreshClassFilter();
        else if (t == navResults) loadMarks();
        else if (t == navRemarks) refreshRemarks();
        else if (t == navFeedback) { corrTable.refresh(); topicTable.refresh(); }
    }

    // ------------------------------------------------------------------ dashboard
    private void refreshDashboard() {
        long pending = ds.corrections.stream().filter(r -> r.status.equals("Pending")).count()
                + ds.topics.stream().filter(r -> r.status.equals("Pending")).count();
        kpiStudents.setText(String.valueOf(ds.students.size()));
        kpiResults.setText(String.valueOf(ds.resultsEntered()));
        kpiPending.setText(String.valueOf(pending));
        kpiCorrections.setText(String.valueOf(ds.corrections.size()));

        XYChart.Series<String, Number> s = new XYChart.Series<>();
        s.setName("Average %");
        ds.students.stream().map(x -> x.cls).distinct().sorted().forEach(c -> {
            double avg = ds.students.stream().filter(x -> x.cls.equals(c))
                    .mapToDouble(x -> ds.average(x.reg, "Term 1")).filter(v -> v >= 0).average().orElse(0);
            s.getData().add(new XYChart.Data<>(c, Math.round(avg)));
        });
        classChart.getData().setAll(s);
    }

    @FXML private void goResults() { navResults.setSelected(true); }
    @FXML private void goStudents() { navStudents.setSelected(true); }
    @FXML private void goFeedback() { navFeedback.setSelected(true); }

    // ------------------------------------------------------------------ student management
    private void setupStudentTable() {
        Ui.fit(studentTable);
        filtered = new FilteredList<>(ds.students, s -> true);
        studentTable.setItems(filtered);
        studentTable.getColumns().addAll(
                Ui.<Student>col("REG NO", s -> s.reg),
                Ui.<Student>col("NAME", s -> s.name),
                Ui.<Student>col("CLASS", s -> s.cls),
                Ui.<Student>col("EMAIL", s -> s.email),
                Ui.<Student>badgeCol("STATUS", s -> s.status),
                Ui.<Student>actionCol("ACTIONS", s -> new HBox(6,
                        Ui.btn("View", "btn-outline", () -> viewStudent(s)),
                        Ui.btn("Edit", "btn-secondary", () -> editStudent(s)),
                        Ui.btn("Delete", "btn-coral", () -> deleteStudent(s)))));
        searchField.textProperty().addListener((o, a, b) -> applyFilter());
        classFilter.valueProperty().addListener((o, a, b) -> applyFilter());
        refreshClassFilter();
    }

    private void applyFilter() {
        String q = searchField.getText() == null ? "" : searchField.getText().toLowerCase().trim();
        String c = classFilter.getValue();
        filtered.setPredicate(s -> (c == null || c.equals("All") || s.cls.equals(c))
                && (s.name + " " + s.reg).toLowerCase().contains(q));
    }

    private void refreshClassFilter() {
        String sel = classFilter.getValue();
        List<String> l = new ArrayList<>(List.of("All"));
        ds.students.stream().map(s -> s.cls).distinct().sorted().forEach(l::add);
        classFilter.setItems(FXCollections.observableArrayList(l));
        classFilter.setValue(sel != null && l.contains(sel) ? sel : "All");
    }

    private void viewStudent(Student s) {
        double avg = ds.average(s.reg, "Term 1");
        Ui.info(s.name + "\nReg No: " + s.reg + "\nClass: " + s.cls + "\nEmail: " + s.email + "\nSchool: " + s.school
                + "\nTerm 1 average: " + (avg < 0 ? "no results yet" : Math.round(avg) + "%"));
    }

    private void editStudent(Student s) {
        TextInputDialog d = new TextInputDialog(s.name);
        d.setHeaderText("Edit student name");
        d.showAndWait().ifPresent(n -> { if (!n.isBlank()) s.name = n.trim(); });
        TextInputDialog c = new TextInputDialog(s.cls);
        c.setHeaderText("Edit class");
        c.showAndWait().ifPresent(v -> { if (!v.isBlank()) s.cls = v.trim(); });
        studentTable.refresh();
        refreshClassFilter();
    }

    private void deleteStudent(Student s) {
        if (Ui.confirm("Delete " + s.name + " (" + s.reg + ")? Their results will be removed too.")) {
            ds.deleteStudent(s);
            ds.addActivity("Student removed: " + s.name);
            refreshClassFilter();
        }
    }

    // ------------------------------------------------------------------ result entry
    private void setupMarksTable() {
        Ui.fit(marksTable);
        TableColumn<MarkRow, String> subject = Ui.col("SUBJECT", MarkRow::getSubject);
        TableColumn<MarkRow, String> max = Ui.col("MAX MARKS", r -> "100");
        TableColumn<MarkRow, String> got = new TableColumn<>("MARKS OBTAINED");
        got.setCellValueFactory(d -> d.getValue().marksProperty());
        got.setCellFactory(c -> new MarkCell());
        got.setSortable(false);
        TableColumn<MarkRow, String> result = new TableColumn<>("RESULT");
        result.setCellValueFactory(d -> d.getValue().marksProperty());
        result.setSortable(false);
        result.setCellFactory(c -> new TableCell<MarkRow, String>() {
            @Override protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                setText(null);
                if (empty) { setGraphic(null); return; }
                Integer n = MarkRow.parse(v);
                setGraphic(n == null ? new Label("—") : Ui.badge(n >= DataStore.PASS ? "Pass" : "Fail")); // live pass/fail pill
            }
        });
        marksTable.getColumns().addAll(subject, max, got, result);
    }

    private void loadMarks() {
        Student s = studentBox.getValue();
        String t = termBox.getValue();
        if (s == null || t == null) { marksTable.setItems(FXCollections.observableArrayList()); remarksArea.clear(); return; }
        int[] m = ds.getResult(s.reg, t);
        ObservableList<MarkRow> rows = FXCollections.observableArrayList();
        for (int i = 0; i < DataStore.SUBJECTS.size(); i++)
            rows.add(new MarkRow(DataStore.SUBJECTS.get(i), m == null ? "" : String.valueOf(m[i])));
        marksTable.setItems(rows);
        remarksArea.setText(ds.getRemark(s.reg, t));
    }

    @FXML
    private void onSaveResult() {
        Student s = studentBox.getValue();
        String t = termBox.getValue();
        if (s == null || t == null) { Ui.info("Select a student and term first."); return; }
        int[] m = new int[marksTable.getItems().size()];
        for (int i = 0; i < m.length; i++) {
            Integer v = MarkRow.parse(marksTable.getItems().get(i).getMarks());
            if (v == null || v < 0 || v > 100) { Ui.info("Enter marks between 0 and 100 for every subject."); return; }
            m[i] = v;
        }
        ds.saveResult(s.reg, t, m, remarksArea.getText().trim());
        ds.addActivity("Marks saved for " + s.name + " (" + t + ")");
        Ui.info("Result and remarks saved for " + s.name + ".");
    }

    @FXML private void onCancel() { navDash.setSelected(true); }

    // ------------------------------------------------------------------ teacher remarks overview
    private void setupRemarksTable() {
        Ui.fit(remarksTable);
        remarksTable.getColumns().addAll(
                Ui.<String[]>col("STUDENT", r -> ds.nameOf(r[0])),
                Ui.<String[]>col("TERM", r -> r[1]),
                Ui.<String[]>col("REMARK", r -> r[2]),
                Ui.<String[]>actionCol("ACTION", r -> Ui.btn("Edit", "btn-secondary", () -> {
                    ds.find(r[0]).ifPresent(studentBox::setValue);
                    termBox.setValue(r[1]);
                    navResults.setSelected(true);
                })));
    }

    private void refreshRemarks() { remarksTable.setItems(FXCollections.observableArrayList(ds.remarkRows())); }

    // ------------------------------------------------------------------ feedback review
    private void setupRequestTable(TableView<Request> table, ObservableList<Request> source, boolean topic) {
        Ui.fit(table);
        table.setItems(source);
        table.getColumns().addAll(
                Ui.<Request>col("STUDENT", r -> ds.nameOf(r.reg)),
                Ui.<Request>col(topic ? "SUBJECT / TOPIC" : "SUBJECT", r -> topic ? r.subject + " · " + r.topic : r.subject),
                Ui.<Request>col("COMMENT", r -> r.message),
                Ui.<Request>col("SUBMITTED", r -> r.date),
                Ui.<Request>badgeCol("STATUS", r -> r.status),
                Ui.<Request>actionCol("ACTIONS", r -> {
                    HBox box = new HBox(6, Ui.btn("View", "btn-outline", () ->
                            Ui.info(ds.nameOf(r.reg) + " · " + r.subject + (topic ? " · " + r.topic : "") + "\n\n" + r.message)));
                    if (r.status.equals("Pending"))
                        box.getChildren().add(Ui.btn("Resolve", "btn-peach", () -> {
                            r.status = "Resolved";
                            ds.addActivity("Resolved request from " + ds.nameOf(r.reg));
                            table.refresh();
                        }));
                    return box;
                }));
    }

    @FXML
    private void onLogout() {
        ds.current = null;
        Nav.go("LoginView");
    }

    /** Table cell with a numeric TextField bound to the row's marks. */
    private static class MarkCell extends TableCell<MarkRow, String> {
        private final TextField tf = new TextField();

        MarkCell() {
            tf.getStyleClass().add("cell-input");
            tf.setMaxWidth(90);
            tf.setTextFormatter(new TextFormatter<String>(c -> c.getControlNewText().matches("\\d{0,3}") ? c : null));
            tf.textProperty().addListener((o, a, n) -> {
                MarkRow r = getTableRow() == null ? null : getTableRow().getItem();
                if (r != null) r.setMarks(n);
            });
        }

        @Override protected void updateItem(String v, boolean empty) {
            super.updateItem(v, empty);
            if (empty) { setGraphic(null); return; }
            String s = v == null ? "" : v;
            if (!tf.getText().equals(s)) tf.setText(s);
            setGraphic(tf);
        }
    }
}
