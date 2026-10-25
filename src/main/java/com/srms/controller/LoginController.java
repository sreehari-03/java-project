package com.srms.controller;

import com.srms.Nav;
import com.srms.config.DatabaseConnection;
import com.srms.exception.AuthenticationException;
import com.srms.model.User;
import com.srms.service.AuthService;
import com.srms.util.Ui;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class LoginController {
    @FXML private ToggleGroup roleGroup;
    @FXML private ToggleButton teacherTab;
    @FXML private TextField usernameField, passwordText;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    private final AuthService authService = new AuthService();

    @FXML
    private void initialize() {
        Ui.keepSelected(roleGroup);
        passwordText.textProperty().bindBidirectional(passwordField.textProperty());
        // Show DB status hint
        if (!DatabaseConnection.getInstance().isAvailable()) {
            errorLabel.setText("⚠ Database offline — demo logins: teacher/teacher123 or student/student123");
            errorLabel.setStyle("-fx-text-fill: #B06000; -fx-font-size: 11px;");
        }
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
        errorLabel.setText("");
        String role = teacherTab.isSelected() ? "teacher" : "student";
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isBlank()) { errorLabel.setText("Please enter your username."); return; }
        if (password.isEmpty()) { errorLabel.setText("Please enter your password."); return; }

        try {
            User user = authService.login(username, password, role);
            // Navigate to the appropriate portal
            if ("teacher".equals(user.getRole())) {
                Nav.go("TeacherPortalView");
            } else {
                Nav.go("StudentPortalView");
            }
        } catch (AuthenticationException e) {
            errorLabel.setText(e.getMessage());
        } catch (Exception e) {
            errorLabel.setText("Login failed. Please try again.");
        }
    }

    @FXML private void onRegister() { Nav.go("RegisterView"); }
    @FXML private void onContactAdmin() { Ui.info("Please contact your school administrator."); }
    @FXML private void onForgot() { Ui.info("A password reset link will be sent to your school email."); }
    @FXML private void onSso() { Ui.info("School account (SSO) login is not connected yet."); }
}
