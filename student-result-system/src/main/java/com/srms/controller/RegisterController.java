package com.srms.controller;

import com.srms.Nav;
import com.srms.exception.ValidationException;
import com.srms.service.StudentService;
import com.srms.util.Ui;
import com.srms.util.ValidationUtil;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class RegisterController {
    @FXML private TextField schoolField, regField, nameField, classField, usernameField, emailField;
    @FXML private PasswordField passField, confirmField;
    @FXML private ToggleGroup accountGroup;
    @FXML private ToggleButton teacherTog;
    @FXML private CheckBox termsCheck;
    @FXML private Label errorLabel;

    private final StudentService studentService = new StudentService();

    @FXML
    private void initialize() {
        Ui.keepSelected(accountGroup);
        classField.disableProperty().bind(teacherTog.selectedProperty());
    }

    @FXML
    private void onCreate() {
        errorLabel.setText("");
        if (teacherTog.isSelected()) {
            errorLabel.setText("Teacher accounts are created by the school administrator. Please contact your administrator.");
            return;
        }

        String school = schoolField.getText().trim();
        String reg = regField.getText().trim();
        String name = nameField.getText().trim();
        String cls = classField.getText().trim();
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passField.getText();

        if (school.isBlank() || reg.isBlank() || name.isBlank() || cls.isBlank()
                || username.isBlank() || email.isBlank() || password.isBlank()) {
            errorLabel.setText("Please fill in all required fields.");
            return;
        }
        if (!ValidationUtil.isValidRegNo(reg)) {
            errorLabel.setText("Registration number must be 3-20 alphanumeric characters.");
            return;
        }
        if (!ValidationUtil.isValidName(name)) {
            errorLabel.setText("Name must be 2-100 characters.");
            return;
        }
        if (!ValidationUtil.isValidEmail(email)) {
            errorLabel.setText("Enter a valid email address.");
            return;
        }
        if (password.length() < 6) {
            errorLabel.setText("Password must be at least 6 characters.");
            return;
        }
        if (!password.equals(confirmField.getText())) {
            errorLabel.setText("Passwords do not match.");
            return;
        }
        if (!termsCheck.isSelected()) {
            errorLabel.setText("Please accept the Terms & Conditions.");
            return;
        }

        try {
            studentService.createStudent(reg, name, cls, email, null, school, username, password);
            Ui.info("Student account created. You can now sign in.");
            Nav.go("LoginView");
        } catch (ValidationException e) {
            errorLabel.setText(e.getMessage());
        } catch (Exception e) {
            errorLabel.setText("Registration failed: " + e.getMessage());
        }
    }

    @FXML
    private void onLogin() {
        Nav.go("LoginView");
    }
}
