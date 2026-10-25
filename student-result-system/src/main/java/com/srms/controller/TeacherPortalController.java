package com.srms.controller;

import com.srms.Nav;
import com.srms.exception.ValidationException;
import com.srms.model.*;
import com.srms.service.*;
import com.srms.util.Ui;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TeacherPortalController {
    // ── Services ──────────────────────────────────────────────────────────────
    private final StudentService studentService = new StudentService();
    private final ResultService resultService = new ResultService();
    private final FeedbackService feedbackService = new FeedbackService();
    private final ActivityService activityService = new ActivityService();
    private final AuthService authService = new AuthService();

    private final ExecutorService bgExec = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "teacher-bg");
        t.setDaemon(true);
        return t;
    });

    // Navigation
    @FXML private ToggleGroup navGroup;
    @FXML private ToggleButton navDash, navStudents, navResults, navRemarks, navFeedback, navSettings;
    @FXML private Label teacherName;
    @FXML private Node dashPage, studentsPage, resultPage, remarksPage, feedbackPage, settingsPage;
    // Dashboard
    @FXML private Label kpiStudents, kpiResults, kpiPending, kpiCorrections;
    @FXML private BarChart<String, Number> classChart;
    @FXML private ListView<String> activityList;

    // Students
    @FXML private TextField searchField;
    @FXML private ComboBox<String> classFilter;
    @FXML private TableView<Student> studentTable;

    // Results
    @FXML private ComboBox<Student> studentBox;
    @FXML private ComboBox<AcademicTerm> termBox;
    @FXML private TableView<ResultRow> marksTable;
    @FXML private TextArea remarksArea;

    // Remarks overview
    @FXML private TableView<TeacherRemark> remarksTable;

    // Feedback
    @FXML private TableView<FeedbackRequest> corrTable, topicTable;

    private Map<ToggleButton, Node> pages;
    private ObservableList<Student> allStudents;
    private FilteredList<Student> filtered;
    private List<Subject> allSubjects;
    private List<AcademicTerm> allTerms;

    @FXML
    private void initialize() {
        User current = AuthService.getCurrentUser();
        teacherName.setText(current != null ? current.getFullName() + " · Teacher" : "Teacher");

        pages = new LinkedHashMap<>();
        pages.put(navDash, dashPage);
        pages.put(navStudents, studentsPage);
        pages.put(navResults, resultPage);
        pages.put(navRemarks, remarksPage);
        pages.put(navFeedback, feedbackPage);
        pages.put(navSettings, settingsPage);

        // Load ref data
        allSubjects = resultService.getAllSubjects();
        allTerms = resultService.getAllTerms();

        // Setup tables
        setupStudentTable();
        setupMarksTable();
        setupRemarksTable();
        setupCorrectionTable();
        setupAssistanceTable();

        // Nav group listener
        navGroup.selectedToggleProperty().addListener((o, old, n) -> {
            if (n == null && old != null) old.setSelected(true);
            else if (n != null) show(n);
        });
        navDash.setSelected(true);
    }

    // ── Navigation ────────────────────────────────────────────────────────────
    private void show(Toggle t) {
        pages.forEach((b, p) -> { boolean on = b == t; p.setVisible(on); p.setManaged(on); });
        if (t == navDash) refreshDashboard();
        else if (t == navStudents) loadStudents();
        else if (t == navResults) loadResultForm();
        else if (t == navRemarks) loadRemarks();
        else if (t == navFeedback) loadFeedback();
    }

    // ── Dashboard ─────────────────────────────────────────────────────────────
    private void refreshDashboard() {
        bgExec.submit(() -> {
            int studentCount = studentService.getTotalStudentCount();
            int resultsCount = resultService.getStudentsWithResultsCount();
            int pendingCount = feedbackService.getPendingAssistanceCount();
            int corrCount = feedbackService.getPendingCorrectionCount();
            List<String[]> classCounts = studentService.getCountByClass();
            List<String> activities = activityService.getRecentActivityStrings(20);

            Platform.runLater(() -> {
                kpiStudents.setText(String.valueOf(studentCount));
                kpiResults.setText(String.valueOf(resultsCount));
                kpiPending.setText(String.valueOf(pendingCount));
                kpiCorrections.setText(String.valueOf(corrCount));

                // Bar chart
                XYChart.Series<String, Number> s = new XYChart.Series<>();
                s.setName("Students");
                for (String[] row : classCounts) {
                    try { s.getData().add(new XYChart.Data<>(row[0], Integer.parseInt(row[1]))); }
                    catch (NumberFormatException ignored) {}
                }
                classChart.getData().setAll(s);

                activityList.setItems(FXCollections.observableArrayList(activities));
            });
        });
    }

    @FXML private void goResults() { navResults.setSelected(true); }
    @FXML private void goStudents() { navStudents.setSelected(true); }
    @FXML private void goFeedback() { navFeedback.setSelected(true); }
    @FXML private void goRemarks() { navRemarks.setSelected(true); }

    // ── Student Management ────────────────────────────────────────────────────
    private void setupStudentTable() {
        Ui.fit(studentTable);
        allStudents = FXCollections.observableArrayList();
        filtered = new FilteredList<>(allStudents, s -> true);
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
                        Ui.btn("Delete", "btn-coral", () -> deleteStudent(s))))
        );
        searchField.textProperty().addListener((o, a, b) -> applyFilter());
        classFilter.valueProperty().addListener((o, a, b) -> applyFilter());
    }

    private void loadStudents() {
        bgExec.submit(() -> {
            List<Student> students = studentService.getAllStudents();
            Platform.runLater(() -> {
                allStudents.setAll(students);
                refreshClassFilter();
            });
        });
    }

    private void applyFilter() {
        String q = searchField.getText() == null ? "" : searchField.getText().toLowerCase().trim();
        String c = classFilter.getValue();
        filtered.setPredicate(s -> (c == null || c.equals("All") || s.cls.equalsIgnoreCase(c))
                && (s.name + " " + s.reg).toLowerCase().contains(q));
    }

    private void refreshClassFilter() {
        String sel = classFilter.getValue();
        List<String> l = new ArrayList<>(List.of("All"));
        allStudents.stream().map(s -> s.cls).distinct().sorted().forEach(l::add);
        classFilter.setItems(FXCollections.observableArrayList(l));
        classFilter.setValue(sel != null && l.contains(sel) ? sel : "All");
    }

    private void viewStudent(Student s) {
        Ui.info("Student Details\n\n" +
                "Name: " + s.name + "\n" +
                "Reg No: " + s.reg + "\n" +
                "Class: " + s.cls + "\n" +
                "Email: " + s.email + "\n" +
                "School: " + s.school + "\n" +
                "Status: " + s.status);
    }

    private void editStudent(Student s) {
        TextInputDialog nd = new TextInputDialog(s.name);
        nd.setTitle("Edit Student");
        nd.setHeaderText("Edit student name for " + s.reg);
        nd.showAndWait().ifPresent(newName -> {
            if (!newName.isBlank()) {
                s.name = newName.trim();
                bgExec.submit(() -> {
                    studentService.updateStudent(s);
                    Platform.runLater(() -> studentTable.refresh());
                });
            }
        });
    }

    private void deleteStudent(Student s) {
        ChoiceDialog<String> choice = new ChoiceDialog<>("Deactivate", "Deactivate", "Delete permanently");
        choice.setTitle("Remove Student");
        choice.setHeaderText("Remove student: " + s.name + " (" + s.reg + ")");
        choice.setContentText("Choose action:");
        choice.showAndWait().ifPresent(action -> {
            if ("Delete permanently".equals(action)) {
                if (Ui.confirm("This will permanently delete " + s.name + " and all their results. Continue?")) {
                    bgExec.submit(() -> {
                        studentService.deleteStudent(s.getId(), s.name);
                        Platform.runLater(() -> { allStudents.remove(s); refreshClassFilter(); });
                    });
                }
            } else {
                bgExec.submit(() -> {
                    studentService.deactivateStudent(s.getId(), s.name);
                    Platform.runLater(() -> { s.status = "Inactive"; studentTable.refresh(); });
                });
            }
        });
    }

    @FXML
    private void onAddStudent() {
        // Simple dialog for add student
        Dialog<Student> dialog = new Dialog<>();
        dialog.setTitle("Add New Student");
        dialog.setHeaderText("Enter student details");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(8);
        TextField regF = new TextField(); regF.setPromptText("e.g. REG010");
        TextField nameF = new TextField(); nameF.setPromptText("Full Name");
        TextField emailF = new TextField(); emailF.setPromptText("email@school.edu");
        ComboBox<String> clsF = new ComboBox<>(FXCollections.observableArrayList(
                allStudents.stream().map(s -> s.cls).distinct().sorted().toList()
        ));
        if (clsF.getItems().isEmpty()) clsF.getItems().addAll("Class 10 (A)", "Class 11 (A)", "Class 12 (A)");
        clsF.setValue(clsF.getItems().get(0));

        grid.add(new Label("Reg No:"), 0, 0); grid.add(regF, 1, 0);
        grid.add(new Label("Name:"), 0, 1); grid.add(nameF, 1, 1);
        grid.add(new Label("Email:"), 0, 2); grid.add(emailF, 1, 2);
        grid.add(new Label("Class:"), 0, 3); grid.add(clsF, 1, 3);

        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                Student s = new Student();
                s.setReg(regF.getText().trim());
                s.setName(nameF.getText().trim());
                s.setCls(clsF.getValue());
                s.setEmail(emailF.getText().trim());
                s.setSchool("Sunrise Public School");
                s.setStatus("Active");
                return s;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(s -> {
            if (s.getReg().isBlank() || s.getName().isBlank()) {
                Ui.info("Reg No and Name are required.");
                return;
            }
            bgExec.submit(() -> {
                try {
                    studentService.createStudent(s.getReg(), s.getName(), s.getCls(), s.getEmail(), null, s.getSchool(), null, null);
                    Platform.runLater(() -> {
                        loadStudents();
                        Ui.info("Student " + s.getName() + " added successfully.");
                    });
                } catch (ValidationException ve) {
                    Platform.runLater(() -> Ui.info("Error: " + ve.getMessage()));
                } catch (Exception e) {
                    Platform.runLater(() -> Ui.info("Could not add student: " + e.getMessage()));
                }
            });
        });
    }

    // ── Result Entry ──────────────────────────────────────────────────────────
    private void setupMarksTable() {
        Ui.fit(marksTable);
        // Subject column
        TableColumn<ResultRow, String> subjectCol = Ui.col("SUBJECT", r -> r.subject().getSubjectName());
        // Max marks
        TableColumn<ResultRow, String> maxCol = Ui.col("MAX MARKS", r -> String.valueOf(r.subject().getMaxMarks()));
        // Editable marks
        TableColumn<ResultRow, String> marksCol = new TableColumn<>("MARKS OBTAINED");
        marksCol.setCellValueFactory(d -> d.getValue().marksProperty());
        marksCol.setCellFactory(c -> new MarkCell());
        marksCol.setSortable(false);
        // Status
        TableColumn<ResultRow, String> statusCol = new TableColumn<>("RESULT");
        statusCol.setSortable(false);
        statusCol.setCellValueFactory(d -> d.getValue().marksProperty());
        statusCol.setCellFactory(c -> new TableCell<>() {
            @Override protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                setText(null);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) { setGraphic(null); return; }
                ResultRow row = getTableRow().getItem();
                try {
                    int m = Integer.parseInt(v);
                    setGraphic(Ui.badge(m >= row.subject().getPassMarks() ? "Pass" : "Fail"));
                } catch (NumberFormatException e) {
                    setGraphic(new Label("—"));
                }
            }
        });
        marksTable.getColumns().addAll(subjectCol, maxCol, marksCol, statusCol);

        // Student and term boxes
        List<Student> students = studentService.getAllStudents();
        studentBox.setItems(FXCollections.observableArrayList(students));
        termBox.setItems(FXCollections.observableArrayList(allTerms));
        if (!students.isEmpty()) studentBox.getSelectionModel().selectFirst();
        if (!allTerms.isEmpty()) termBox.getSelectionModel().selectFirst();
        studentBox.valueProperty().addListener((o, a, b) -> loadMarks());
        termBox.valueProperty().addListener((o, a, b) -> loadMarks());
        loadMarks();
    }

    private void loadResultForm() {
        // Refresh student list when navigating here
        bgExec.submit(() -> {
            List<Student> students = studentService.getAllStudents();
            Platform.runLater(() -> {
                Student currentSel = studentBox.getValue();
                studentBox.setItems(FXCollections.observableArrayList(students));
                if (currentSel != null) {
                    students.stream().filter(s -> s.getReg().equals(currentSel.getReg()))
                            .findFirst().ifPresent(s -> studentBox.setValue(s));
                } else if (!students.isEmpty()) {
                    studentBox.getSelectionModel().selectFirst();
                }
                loadMarks();
            });
        });
    }

    private void loadMarks() {
        Student s = studentBox.getValue();
        AcademicTerm t = termBox.getValue();
        if (s == null || t == null) { marksTable.setItems(FXCollections.emptyObservableList()); remarksArea.clear(); return; }
        bgExec.submit(() -> {
            List<StudentResult> existingResults = resultService.getResultsForStudentTerm(s, t);
            String overallRemark = resultService.getOverallRemark(s.getId(), t.getId());
            Map<Integer, StudentResult> bySubject = new HashMap<>();
            existingResults.forEach(r -> bySubject.put(r.getSubjectId(), r));
            List<ResultRow> rows = new ArrayList<>();
            for (Subject sub : allSubjects) {
                StudentResult existing = bySubject.get(sub.getId());
                String marks = existing != null ? String.valueOf(existing.getMarksObtained()) : "";
                rows.add(new ResultRow(sub, marks));
            }
            Platform.runLater(() -> {
                marksTable.setItems(FXCollections.observableArrayList(rows));
                remarksArea.setText(overallRemark);
            });
        });
    }

    @FXML
    private void onSaveResult() {
        Student s = studentBox.getValue();
        AcademicTerm t = termBox.getValue();
        if (s == null || t == null) { Ui.info("Please select a student and term."); return; }

        List<ResultService.MarkEntry> entries = new ArrayList<>();
        for (ResultRow row : marksTable.getItems()) {
            String marksStr = row.getMarks();
            if (marksStr.isBlank()) continue;
            try {
                int m = Integer.parseInt(marksStr.trim());
                entries.add(new ResultService.MarkEntry(row.subject(), m, null));
            } catch (NumberFormatException e) {
                Ui.info("Invalid marks for " + row.subject().getSubjectName() + ". Enter a whole number.");
                return;
            }
        }
        if (entries.isEmpty()) { Ui.info("Please enter marks for at least one subject."); return; }

        String remark = remarksArea.getText().trim();
        bgExec.submit(() -> {
            try {
                resultService.saveResults(s, t, entries, remark);
                Platform.runLater(() -> Ui.info("Results saved for " + s.getName() + " (" + t.getTermName() + ")"));
            } catch (ValidationException ve) {
                Platform.runLater(() -> Ui.info("Validation error: " + ve.getMessage()));
            } catch (Exception e) {
                Platform.runLater(() -> Ui.info("Error saving results: " + e.getMessage()));
            }
        });
    }

    @FXML private void onCancel() { navDash.setSelected(true); }

    // ── Teacher Remarks Overview ───────────────────────────────────────────────
    private void setupRemarksTable() {
        Ui.fit(remarksTable);
        remarksTable.getColumns().addAll(
                Ui.<TeacherRemark>col("STUDENT", r -> r.getStudentName() != null ? r.getStudentName() : ""),
                Ui.<TeacherRemark>col("SUBJECT", r -> r.getSubjectName() != null ? r.getSubjectName() : "Overall"),
                Ui.<TeacherRemark>col("TERM", r -> r.getTermName() != null ? r.getTermName() : ""),
                Ui.<TeacherRemark>col("REMARK", r -> r.getRemarkText()),
                Ui.<TeacherRemark>actionCol("ACTION", r -> Ui.btn("Edit", "btn-secondary", () -> {
                    TextInputDialog d = new TextInputDialog(r.getRemarkText());
                    d.setTitle("Edit Remark");
                    d.setHeaderText("Edit remark for " + r.getStudentName());
                    d.showAndWait().ifPresent(newText -> {
                        if (!newText.isBlank()) {
                            bgExec.submit(() -> {
                                new com.srms.dao.RemarkDAO().updateById(r.getId(), newText.trim());
                                Platform.runLater(() -> loadRemarks());
                            });
                        }
                    });
                }))
        );
    }

    private void loadRemarks() {
        bgExec.submit(() -> {
            List<TeacherRemark> list = resultService.getAllRemarks();
            Platform.runLater(() -> remarksTable.setItems(FXCollections.observableArrayList(list)));
        });
    }

    // ── Feedback Review ───────────────────────────────────────────────────────
    private void setupCorrectionTable() {
        Ui.fit(corrTable);
        corrTable.getColumns().addAll(
                Ui.<FeedbackRequest>col("STUDENT", r -> r.getStudentName() != null ? r.getStudentName() : r.getRegNo()),
                Ui.<FeedbackRequest>col("SUBJECT", r -> r.getSubjectName()),
                Ui.<FeedbackRequest>col("REQUEST", r -> r.getRequestText()),
                Ui.<FeedbackRequest>col("DATE", r -> r.getCreatedAtString()),
                Ui.<FeedbackRequest>badgeCol("STATUS", FeedbackRequest::getStatus),
                Ui.<FeedbackRequest>actionCol("ACTIONS", r -> buildFeedbackActions(r, true))
        );
    }

    private void setupAssistanceTable() {
        Ui.fit(topicTable);
        topicTable.getColumns().addAll(
                Ui.<FeedbackRequest>col("STUDENT", r -> r.getStudentName() != null ? r.getStudentName() : r.getRegNo()),
                Ui.<FeedbackRequest>col("SUBJECT", r -> r.getSubjectName()),
                Ui.<FeedbackRequest>col("TOPIC", r -> r.getTopic() != null ? r.getTopic() : ""),
                Ui.<FeedbackRequest>col("COMMENT", r -> r.getRequestText()),
                Ui.<FeedbackRequest>col("DATE", r -> r.getCreatedAtString()),
                Ui.<FeedbackRequest>badgeCol("STATUS", FeedbackRequest::getStatus),
                Ui.<FeedbackRequest>actionCol("ACTIONS", r -> buildFeedbackActions(r, false))
        );
    }

    private HBox buildFeedbackActions(FeedbackRequest r, boolean isCorrection) {
        HBox box = new HBox(6);
        box.getChildren().add(Ui.btn("View", "btn-outline", () -> showRequestDetail(r)));
        if ("Pending".equals(r.getStatus()) || "In Progress".equals(r.getStatus())) {
            box.getChildren().add(Ui.btn("Resolve", "btn-peach", () -> resolveRequest(r, isCorrection)));
        }
        return box;
    }

    private void showRequestDetail(FeedbackRequest r) {
        StringBuilder sb = new StringBuilder();
        sb.append("Student: ").append(r.getStudentName()).append(" (").append(r.getRegNo()).append(")\n");
        sb.append("Subject: ").append(r.getSubjectName()).append("\n");
        if (r.getTopic() != null && !r.getTopic().isBlank()) sb.append("Topic: ").append(r.getTopic()).append("\n");
        sb.append("Request: ").append(r.getRequestText()).append("\n");
        sb.append("Date: ").append(r.getCreatedAtString()).append("\n");
        sb.append("Status: ").append(r.getStatus());
        if (r.getTeacherResponse() != null) sb.append("\n\nTeacher Response: ").append(r.getTeacherResponse());
        Ui.info(sb.toString());
    }

    private void resolveRequest(FeedbackRequest r, boolean isCorrection) {
        TextInputDialog respDialog = new TextInputDialog();
        respDialog.setTitle("Resolve Request");
        respDialog.setHeaderText("Add a response (optional):");
        respDialog.showAndWait().ifPresent(response -> {
            bgExec.submit(() -> {
                if (isCorrection) feedbackService.updateCorrectionStatus(r.getId(), "Resolved", response);
                else feedbackService.updateAssistanceStatus(r.getId(), "Resolved", response);
                Platform.runLater(() -> {
                    r.setStatus("Resolved");
                    corrTable.refresh();
                    topicTable.refresh();
                });
            });
        });
    }

    private void loadFeedback() {
        bgExec.submit(() -> {
            List<FeedbackRequest> corrs = feedbackService.getAllCorrectionRequests();
            List<FeedbackRequest> assists = feedbackService.getAllAssistanceRequests();
            Platform.runLater(() -> {
                corrTable.setItems(FXCollections.observableArrayList(corrs));
                topicTable.setItems(FXCollections.observableArrayList(assists));
            });
        });
    }

    // ── Logout ────────────────────────────────────────────────────────────────
    @FXML
    private void onLogout() {
        bgExec.shutdownNow();
        authService.logout();
        javafx.application.Platform.exit();
    }

    // ── Helper Classes ────────────────────────────────────────────────────────

    /** TableView row combining a Subject and an editable marks string. */
    public static class ResultRow {
        private final Subject subject;
        private final javafx.beans.property.StringProperty marks = new javafx.beans.property.SimpleStringProperty("");

        public ResultRow(Subject subject, String marks) {
            this.subject = subject;
            this.marks.set(marks != null ? marks : "");
        }

        public Subject subject() { return subject; }
        public javafx.beans.property.StringProperty marksProperty() { return marks; }
        public String getMarks() { return marks.get(); }
        public void setMarks(String m) { marks.set(m); }
    }

    /** Editable TextField cell for marks entry in the results table. */
    private static class MarkCell extends TableCell<ResultRow, String> {
        private final TextField tf = new TextField();
        MarkCell() {
            tf.getStyleClass().add("cell-input");
            tf.setMaxWidth(90);
            tf.setTextFormatter(new TextFormatter<>(c -> c.getControlNewText().matches("\\d{0,3}") ? c : null));
            tf.textProperty().addListener((o, a, n) -> {
                ResultRow r = getTableRow() == null ? null : getTableRow().getItem();
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
