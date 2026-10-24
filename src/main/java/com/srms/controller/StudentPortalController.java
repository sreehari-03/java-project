package com.srms.controller;

import com.srms.Nav;
import com.srms.data.DataStore;
import com.srms.model.Request;
import com.srms.model.Student;
import com.srms.util.Ui;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;

import java.util.LinkedHashMap;
import java.util.Map;

public class StudentPortalController {
    private final DataStore ds = DataStore.I;
    private Student me;
    private Map<ToggleButton, Node> pages;

    @FXML private ToggleGroup navGroup;
    @FXML private ToggleButton navDash, navResults, navCorrection, navFocus, navProfile;
    @FXML private Label sideName;
    @FXML private Node dashPage, resultsPage, correctionPage, focusPage, profilePage;
    // dashboard
    @FXML private Label avatar, dpName, dpMeta, donutLabel, donutCaption;
    @FXML private PieChart donut;
    @FXML private BarChart<String, Number> subjectChart;
    // results
    @FXML private ComboBox<String> termBox;
    @FXML private TableView<String[]> resultTable;
    @FXML private Label remarkLabel;
    // correction
    @FXML private ComboBox<String> corrSubject;
    @FXML private TextArea corrText;
    @FXML private TableView<Request> corrTable;
    // special focus
    @FXML private ComboBox<String> focusSubject, focusTopic;
    @FXML private TextArea focusText;
    @FXML private TableView<Request> focusTable;
    // profile
    @FXML private Label profName, profReg, profClass, profEmail, profSchool;

    @FXML
    private void initialize() {
        me = ds.find(ds.current.reg()).orElseThrow();
        sideName.setText(me.name + " · Student");
        pages = new LinkedHashMap<>();
        pages.put(navDash, dashPage); pages.put(navResults, resultsPage); pages.put(navCorrection, correctionPage);
        pages.put(navFocus, focusPage); pages.put(navProfile, profilePage);

        // results table
        Ui.fit(resultTable);
        resultTable.getColumns().addAll(
                Ui.<String[]>col("SUBJECT", r -> r[0]),
                Ui.<String[]>col("MARKS %", r -> r[1]),
                Ui.<String[]>badgeCol("STATUS", r -> r[2]));
        termBox.setItems(FXCollections.observableArrayList(DataStore.TERMS));
        String latest = ds.latestTerm(me.reg);
        termBox.setValue(latest != null ? latest : DataStore.TERMS.get(0));
        termBox.valueProperty().addListener((o, a, b) -> loadResults());

        // correction requests
        ObservableList<String> subjects = FXCollections.observableArrayList(DataStore.SUBJECTS);
        corrSubject.setItems(subjects);
        corrSubject.getSelectionModel().selectFirst();
        Ui.fit(corrTable);
        corrTable.setItems(ds.corrections.filtered(r -> r.reg.equals(me.reg)));
        corrTable.getColumns().addAll(
                Ui.<Request>col("SUBJECT", r -> r.subject),
                Ui.<Request>col("COMMENT", r -> r.message),
                Ui.<Request>col("SUBMITTED", r -> r.date),
                Ui.<Request>badgeCol("STATUS", r -> r.status));

        // special focus requests
        focusSubject.setItems(subjects);
        focusSubject.valueProperty().addListener((o, a, b) -> {
            if (b != null) { focusTopic.setItems(FXCollections.observableArrayList(DataStore.TOPICS.get(b))); focusTopic.getSelectionModel().selectFirst(); }
        });
        focusSubject.getSelectionModel().selectFirst();
        Ui.fit(focusTable);
        focusTable.setItems(ds.topics.filtered(r -> r.reg.equals(me.reg)));
        focusTable.getColumns().addAll(
                Ui.<Request>col("SUBJECT", r -> r.subject),
                Ui.<Request>col("TOPIC", r -> r.topic),
                Ui.<Request>col("SUBMITTED", r -> r.date),
                Ui.<Request>badgeCol("STATUS", r -> r.status));

        // profile
        profName.setText(me.name); profReg.setText(me.reg); profClass.setText(me.cls);
        profEmail.setText(me.email); profSchool.setText(me.school);

        navGroup.selectedToggleProperty().addListener((o, a, n) -> { if (n == null) a.setSelected(true); else show(n); });
        navDash.setSelected(true);
    }

    private void show(Toggle t) {
        pages.forEach((b, p) -> { boolean on = b == t; p.setVisible(on); p.setManaged(on); });
        if (t == navDash) refreshDashboard();
        else if (t == navResults) loadResults();
        else if (t == navCorrection) corrTable.refresh();
        else if (t == navFocus) focusTable.refresh();
    }

    private void refreshDashboard() {
        String term = ds.latestTerm(me.reg);
        int[] m = term == null ? null : ds.getResult(me.reg, term);
        double avg = m == null ? 0 : ds.average(me.reg, term);

        avatar.setText(me.name.substring(0, 1).toUpperCase());
        dpName.setText(me.name);
        dpMeta.setText("Reg No: " + me.reg + "   ·   Class " + me.cls + "   ·   " + me.school);

        donut.setData(FXCollections.observableArrayList(new PieChart.Data("Score", avg), new PieChart.Data("Remaining", 100 - avg)));
        donutLabel.setText(Math.round(avg) + "%");
        donutCaption.setText(term == null ? "No results yet" : term + " average");

        XYChart.Series<String, Number> s = new XYChart.Series<>();
        s.setName("Marks");
        if (m != null) for (int i = 0; i < m.length; i++) s.getData().add(new XYChart.Data<>(DataStore.SUBJECTS.get(i), m[i]));
        subjectChart.getData().setAll(s);
    }

    private void loadResults() {
        String t = termBox.getValue();
        int[] m = ds.getResult(me.reg, t);
        ObservableList<String[]> rows = FXCollections.observableArrayList();
        if (m != null)
            for (int i = 0; i < m.length; i++)
                rows.add(new String[]{DataStore.SUBJECTS.get(i), m[i] + "%", m[i] >= DataStore.PASS ? "Pass" : "Fail"});
        resultTable.setItems(rows);
        String rk = ds.getRemark(me.reg, t);
        remarkLabel.setText(m == null ? "No results have been published for this term yet."
                : rk.isBlank() ? "Your teacher has not added remarks for this term." : rk);
    }

    @FXML private void goResults() { navResults.setSelected(true); }
    @FXML private void goCorrection() { navCorrection.setSelected(true); }
    @FXML private void goFocus() { navFocus.setSelected(true); }

    @FXML
    private void onSubmitCorrection() {
        String msg = corrText.getText().trim();
        if (msg.isEmpty()) { Ui.info("Please describe what needs correction or clarification."); return; }
        ds.addCorrection(me.reg, corrSubject.getValue(), msg);
        corrText.clear();
        Ui.info("Your correction request has been sent to your teacher.");
    }

    @FXML
    private void onSubmitFocus() {
        String msg = focusText.getText().trim();
        ds.addTopic(me.reg, focusSubject.getValue(), focusTopic.getValue(), msg.isEmpty() ? "—" : msg);
        focusText.clear();
        Ui.info("Your extra-focus request has been submitted.");
    }

    @FXML
    private void onLogout() {
        ds.current = null;
        Nav.go("LoginView");
    }
}
