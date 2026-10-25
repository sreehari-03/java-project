package com.srms.controller;

import com.srms.Nav;
import com.srms.exception.ValidationException;
import com.srms.model.*;
import com.srms.service.*;
import com.srms.util.Ui;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StudentPortalController {
    private final StudentService studentService = new StudentService();
    private final ResultService resultService = new ResultService();
    private final FeedbackService feedbackService = new FeedbackService();
    private final AuthService authService = new AuthService();

    private final ExecutorService bgExec = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "student-bg");
        t.setDaemon(true);
        return t;
    });

    // Nav
    @FXML private ToggleGroup navGroup;
    @FXML private ToggleButton navDash, navResults, navCorrection, navFocus, navProfile;
    @FXML private Label sideName;
    @FXML private Node dashPage, resultsPage, correctionPage, focusPage, profilePage;

    // Dashboard
    @FXML private Label avatar, dpName, dpMeta, donutLabel, donutCaption;
    @FXML private PieChart donut;
    @FXML private BarChart<String, Number> subjectChart;
    @FXML private Label recentResultsPlaceholder;

    // Results
    @FXML private ComboBox<AcademicTerm> termBox;
    @FXML private TableView<StudentResult> resultTable;
    @FXML private Label remarkLabel;

    // Correction request
    @FXML private ComboBox<Subject> corrSubject;
    @FXML private TextArea corrText;
    @FXML private TableView<FeedbackRequest> corrTable;
    @FXML private ComboBox<AcademicTerm> corrTermBox;

    // Special focus
    @FXML private ComboBox<Subject> focusSubject;
    @FXML private ComboBox<String> focusTopic;
    @FXML private TextArea focusText;
    @FXML private TableView<FeedbackRequest> focusTable;

    // Profile
    @FXML private Label profName, profReg, profClass, profEmail, profSchool;

    private Student me;
    private Map<ToggleButton, Node> pages;
    private List<AcademicTerm> allTerms;
    private List<Subject> allSubjects;

    @FXML
    private void initialize() {
        User currentUser = AuthService.getCurrentUser();
        me = AuthService.getCurrentStudent();

        // Fallback: find student from service if not set
        if (me == null && currentUser != null) {
            me = studentService.findByUserId(currentUser.getId()).orElse(null);
        }
        // Offline demo fallback
        if (me == null) {
            me = new Student("REG001", "Rohan Mehta", "Class 10 (A)", "rohan@mail.com", "Sunrise Public School");
        }

        sideName.setText(me.getName() + " · Student");

        pages = new LinkedHashMap<>();
        pages.put(navDash, dashPage); pages.put(navResults, resultsPage);
        pages.put(navCorrection, correctionPage); pages.put(navFocus, focusPage);
        pages.put(navProfile, profilePage);

        allTerms = resultService.getAllTerms();
        allSubjects = resultService.getAllSubjects();

        setupResultsPage();
        setupCorrectionPage();
        setupFocusPage();
        setupProfilePage();

        navGroup.selectedToggleProperty().addListener((o, old, n) -> {
            if (n == null && old != null) old.setSelected(true);
            else if (n != null) show(n);
        });
        navDash.setSelected(true);
    }

    private void show(Toggle t) {
        pages.forEach((b, p) -> { boolean on = b == t; p.setVisible(on); p.setManaged(on); });
        if (t == navDash) refreshDashboard();
        else if (t == navResults) loadResults();
        else if (t == navCorrection) loadMyCorrectionRequests();
        else if (t == navFocus) loadMyAssistanceRequests();
    }

    // ── Dashboard ─────────────────────────────────────────────────────────────
    private void refreshDashboard() {
        AcademicTerm term = allTerms.isEmpty() ? null : allTerms.get(0);
        if (term == null) return;

        bgExec.submit(() -> {
            List<StudentResult> results = resultService.getResultsForStudentTerm(me, term);
            double avg = resultService.getAveragePercentage(me, term);
            String overallRemark = resultService.getOverallRemark(me.getId(), term.getId());

            Platform.runLater(() -> {
                String initials = me.getName() != null && !me.getName().isEmpty()
                        ? me.getName().substring(0, 1).toUpperCase() : "S";
                avatar.setText(initials);
                dpName.setText(me.getName());
                dpMeta.setText(me.getReg() + "   ·   " + me.getCls() + "   ·   " + me.getSchool());

                // Donut chart
                double displayAvg = avg < 0 ? 0 : avg;
                donut.setData(FXCollections.observableArrayList(
                        new PieChart.Data("Score", displayAvg),
                        new PieChart.Data("Remaining", Math.max(0, 100 - displayAvg))
                ));
                donutLabel.setText(String.format("%.0f%%", displayAvg));
                donutCaption.setText(results.isEmpty() ? "No results yet" : term.getTermName() + " average");

                // Bar chart per subject
                XYChart.Series<String, Number> s = new XYChart.Series<>();
                s.setName("Marks");
                for (StudentResult r : results) {
                    s.getData().add(new XYChart.Data<>(
                            abbreviate(r.getSubjectName()), r.getMarksObtained()));
                }
                subjectChart.getData().setAll(s);
            });
        });
    }

    // ── My Results ─────────────────────────────────────────────────────────────
    private void setupResultsPage() {
        Ui.fit(resultTable);
        resultTable.getColumns().addAll(
                Ui.<StudentResult>col("SUBJECT", r -> r.getSubjectName() != null ? r.getSubjectName() : ""),
                Ui.<StudentResult>col("MARKS (%)", r -> r.getMarksObtained() + "/" + r.getMaxMarks() + " — " + String.format("%.0f%%", r.getPercentage())),
                Ui.<StudentResult>badgeCol("STATUS", r -> r.isPass() ? "Pass" : "Fail"),
                Ui.<StudentResult>col("TEACHER REMARKS", r -> r.getTeacherRemarks() != null ? r.getTeacherRemarks() : "—")
        );
        termBox.setItems(FXCollections.observableArrayList(allTerms));
        if (!allTerms.isEmpty()) termBox.setValue(allTerms.get(0));
        termBox.valueProperty().addListener((o, a, b) -> loadResults());
    }

    private void loadResults() {
        AcademicTerm t = termBox.getValue();
        if (t == null) return;
        bgExec.submit(() -> {
            List<StudentResult> results = resultService.getResultsForStudentTerm(me, t);
            String remark = resultService.getOverallRemark(me.getId(), t.getId());
            Platform.runLater(() -> {
                resultTable.setItems(FXCollections.observableArrayList(results));
                remarkLabel.setText(results.isEmpty()
                        ? "No results have been published for this term yet."
                        : remark.isBlank() ? "Your teacher has not added remarks for this term." : remark);
            });
        });
    }

    // ── Correction Requests ───────────────────────────────────────────────────
    private void setupCorrectionPage() {
        ObservableList<Subject> subjects = FXCollections.observableArrayList(allSubjects);
        corrSubject.setItems(subjects);
        if (!subjects.isEmpty()) corrSubject.setValue(subjects.get(0));

        if (corrTermBox != null) {
            corrTermBox.setItems(FXCollections.observableArrayList(allTerms));
            if (!allTerms.isEmpty()) corrTermBox.setValue(allTerms.get(0));
        }

        Ui.fit(corrTable);
        corrTable.getColumns().addAll(
                Ui.<FeedbackRequest>col("SUBJECT", r -> r.getSubjectName()),
                Ui.<FeedbackRequest>col("REQUEST", r -> r.getRequestText()),
                Ui.<FeedbackRequest>col("SUBMITTED", r -> r.getCreatedAtString()),
                Ui.<FeedbackRequest>badgeCol("STATUS", FeedbackRequest::getStatus),
                Ui.<FeedbackRequest>col("RESPONSE", r -> r.getTeacherResponse() != null ? r.getTeacherResponse() : "—")
        );
    }

    private void loadMyCorrectionRequests() {
        bgExec.submit(() -> {
            List<FeedbackRequest> list = feedbackService.getCorrectionsByStudent(me.getId());
            Platform.runLater(() -> corrTable.setItems(FXCollections.observableArrayList(list)));
        });
    }

    @FXML
    private void onSubmitCorrection() {
        Subject subj = corrSubject.getValue();
        String text = corrText.getText().trim();
        Integer termId = (corrTermBox != null && corrTermBox.getValue() != null) ? corrTermBox.getValue().getId() : null;

        if (subj == null) { Ui.info("Please select a subject."); return; }
        if (text.isEmpty()) { Ui.info("Please describe what needs correction."); return; }

        bgExec.submit(() -> {
            try {
                feedbackService.submitCorrectionRequest(me, subj, termId, text);
                Platform.runLater(() -> {
                    corrText.clear();
                    loadMyCorrectionRequests();
                    Ui.info("Your correction request has been submitted to your teacher.");
                });
            } catch (ValidationException ve) {
                Platform.runLater(() -> Ui.info(ve.getMessage()));
            }
        });
    }

    // ── Special Focus / Assistance ────────────────────────────────────────────
    private void setupFocusPage() {
        ObservableList<Subject> subjects = FXCollections.observableArrayList(allSubjects);
        focusSubject.setItems(subjects);
        focusSubject.valueProperty().addListener((o, a, b) -> {
            if (b != null) {
                List<String> topics = feedbackService.getTopicsForSubject(b.getSubjectName());
                focusTopic.setItems(FXCollections.observableArrayList(topics));
                if (!topics.isEmpty()) focusTopic.setValue(topics.get(0));
            }
        });
        if (!subjects.isEmpty()) focusSubject.setValue(subjects.get(0));

        Ui.fit(focusTable);
        focusTable.getColumns().addAll(
                Ui.<FeedbackRequest>col("SUBJECT", r -> r.getSubjectName()),
                Ui.<FeedbackRequest>col("TOPIC", r -> r.getTopic() != null ? r.getTopic() : ""),
                Ui.<FeedbackRequest>col("SUBMITTED", r -> r.getCreatedAtString()),
                Ui.<FeedbackRequest>badgeCol("STATUS", FeedbackRequest::getStatus),
                Ui.<FeedbackRequest>col("RESPONSE", r -> r.getTeacherResponse() != null ? r.getTeacherResponse() : "—")
        );
    }

    private void loadMyAssistanceRequests() {
        bgExec.submit(() -> {
            List<FeedbackRequest> list = feedbackService.getAssistanceByStudent(me.getId());
            Platform.runLater(() -> focusTable.setItems(FXCollections.observableArrayList(list)));
        });
    }

    @FXML
    private void onSubmitFocus() {
        Subject subj = focusSubject.getValue();
        String topic = focusTopic.getValue();
        String comments = focusText.getText().trim();

        if (subj == null) { Ui.info("Please select a subject."); return; }
        if (topic == null || topic.isBlank()) { Ui.info("Please select a topic."); return; }

        bgExec.submit(() -> {
            try {
                feedbackService.submitAssistanceRequest(me, subj, topic, comments);
                Platform.runLater(() -> {
                    focusText.clear();
                    loadMyAssistanceRequests();
                    Ui.info("Your extra-focus request has been submitted.");
                });
            } catch (ValidationException ve) {
                Platform.runLater(() -> Ui.info(ve.getMessage()));
            }
        });
    }

    // ── Profile ───────────────────────────────────────────────────────────────
    private void setupProfilePage() {
        profName.setText(me.getName());
        profReg.setText(me.getReg());
        profClass.setText(me.getCls());
        profEmail.setText(me.getEmail());
        profSchool.setText(me.getSchool());
    }

    // ── Navigation shortcuts ──────────────────────────────────────────────────
    @FXML private void goResults() { navResults.setSelected(true); }
    @FXML private void goCorrection() { navCorrection.setSelected(true); }
    @FXML private void goFocus() { navFocus.setSelected(true); }

    @FXML
    private void onLogout() {
        bgExec.shutdownNow();
        authService.logout();
        Nav.go("TeacherPortalView");
    }

    private String abbreviate(String name) {
        if (name == null) return "";
        String[] words = name.split("\\s+");
        return words.length > 1 ? words[0].substring(0, Math.min(3, words[0].length())) + "." : name;
    }
}
