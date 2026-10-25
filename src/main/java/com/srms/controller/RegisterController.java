package com.srms.controller;

import com.srms.Nav;
import com.srms.config.DatabaseConnection;
import com.srms.model.ClassRoom;
import com.srms.model.User;
import com.srms.service.AuthService;
import com.srms.service.StudentService;
import com.srms.util.BCryptUtil;
import com.srms.util.ValidationUtil;
import com.srms.util.Ui;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.List;

public class RegisterController {
    @FXML private TextField regField, nameField, emailField, schoolField, phoneField;
    @FXML private PasswordField passField, confirmField;
    @FXML private ComboBox<String> roleBox;
    @FXML private ComboBox<ClassRoom> classBox;
    @FXML private Label errorLabel;

    private final StudentService studentService = new StudentService();

    @FXML
    private void initialize() {
        if (roleBox != null) {
            roleBox.setItems(FXCollections.observableArrayList("student", "teacher"));
            roleBox.setValue("student");
        }

        List<ClassRoom> classes = studentService.getAllClasses();
        if (classBox != null) {
            classBox.setItems(FXCollections.observableArrayList(classes));
            if (!classes.isEmpty()) classBox.setValue(classes.get(0));
        }
    }

    @FXML
    private void onRegister() {
        errorLabel.setText("");

        String reg = regField.getText().trim();
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String school = schoolField != null ? schoolField.getText().trim() : "Sunrise Public School";
        String phone = phoneField != null ? phoneField.getText().trim() : "";
        String pass = passField.getText();
        String confirm = confirmField.getText();

        // Validations
        if (reg.isBlank() || name.isBlank() || email.isBlank() || pass.isBlank()) {
            errorLabel.setText("All required fields must be filled in.");
            return;
        }
        if (!ValidationUtil.isValidRegNo(reg)) {
            errorLabel.setText("Registration number must be 3-20 alphanumeric characters.");
            return;
        }
        if (!ValidationUtil.isValidEmail(email)) {
            errorLabel.setText("Please enter a valid email address.");
            return;
        }
        if (!pass.equals(confirm)) {
            errorLabel.setText("Passwords do not match.");
            return;
        }
        if (pass.length() < 6) {
            errorLabel.setText("Password must be at least 6 characters.");
            return;
        }

        String cls = classBox != null && classBox.getValue() != null ? classBox.getValue().toString() : "Class 10 (A)";

        try {
            studentService.createStudent(reg, name, cls, email, phone, school, reg.toLowerCase(), pass);
            Ui.info("Account created successfully!\n\nUsername: " + reg.toLowerCase() + "\nPlease log in to continue.");
            Nav.go("LoginView");
        } catch (com.srms.exception.ValidationException ve) {
            errorLabel.setText(ve.getMessage());
        } catch (Exception e) {
            errorLabel.setText("Registration failed: " + e.getMessage());
        }
    }

    @FXML private void onBack() { Nav.go("LoginView"); }
}
