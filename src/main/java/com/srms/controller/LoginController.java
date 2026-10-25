package com.srms.controller;

import com.srms.Nav;
import com.srms.data.AccountService;
import com.srms.data.DataStore;
import com.srms.model.Account;
import com.srms.util.Ui;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.SQLException;

public class LoginController {
    @FXML private ToggleGroup roleGroup;
    @FXML private ToggleButton teacherTab;
    @FXML private TextField usernameField, passwordText;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    @FXML
    private void initialize() {
        Ui.keepSelected(roleGroup);
        passwordText.textProperty().bindBidirectional(passwordField.textProperty());
    }

    @FXML
    private void toggleEye() {
        boolean show = !passwordText.isVisible();
        passwordText.setVisible(show);
        passwordText.setManaged(show);
        passwordField.setVisible(!show);
        passwordField.setManaged(!show);
    }

    @FXML
    private void onLogin() {
        String role = teacherTab.isSelected() ? "teacher" : "student";
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        Account a = DataStore.I.authenticate(username, password);
        if (a == null) {
            try {
                a = AccountService.authenticate(username, password);
            } catch (SQLException e) {
                e.printStackTrace();
                errorLabel.setText("Could not connect to the account database. Please try again.");
                return;
            }
        }
        if (a == null) { errorLabel.setText("Invalid username or password."); return; }
        if (!a.role().equals(role)) {
            errorLabel.setText("This is a " + a.role() + " account. Switch the login tab.");
            return;
        }
        DataStore.I.current = a;
        Nav.go(a.role().equals("teacher") ? "TeacherPortalView" : "StudentPortalView");
    }

    @FXML private void onRegister() { Nav.go("RegisterView"); }
    @FXML private void onContactAdmin() { Ui.info("Please contact your school administrator."); }
    @FXML private void onForgot() { Ui.info("A password reset link will be sent to your school email."); }
    @FXML private void onSso() { Ui.info("School account (SSO) login is not connected yet."); }
}
