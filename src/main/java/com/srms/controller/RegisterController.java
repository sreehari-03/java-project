package com.srms.controller;

import com.srms.Nav;
import com.srms.data.DataStore;
import com.srms.model.Account;
import com.srms.model.Student;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class RegisterController {
    private final DataStore ds = DataStore.I;

    @FXML private TextField schoolField, regField, nameField, classField, usernameField, emailField;
    @FXML private PasswordField passField, confirmField;
    @FXML private ToggleGroup accountGroup;
    @FXML private ToggleButton teacherTog;
    @FXML private CheckBox termsCheck;
    @FXML private Label errorLabel;

    @FXML
    private void initialize() {
        com.srms.util.Ui.keepSelected(accountGroup);
        classField.disableProperty().bind(teacherTog.selectedProperty()); // class only applies to students
    }

    @FXML
    private void onCreate() {
        boolean teacher = teacherTog.isSelected();
        String school = schoolField.getText().trim(), reg = regField.getText().trim(), name = nameField.getText().trim(),
                cls = classField.getText().trim(), user = usernameField.getText().trim(), email = emailField.getText().trim();

        if (school.isEmpty() || reg.isEmpty() || name.isEmpty() || user.isEmpty() || email.isEmpty() || passField.getText().isEmpty())
            { err("Please fill in all required fields."); return; }
        if (!teacher && cls.isEmpty()) { err("Class is required for students."); return; }
        if (!email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) { err("Enter a valid email address."); return; }
        if (passField.getText().length() < 6) { err("Password must be at least 6 characters."); return; }
        if (!passField.getText().equals(confirmField.getText())) { err("Passwords do not match."); return; }
        if (!termsCheck.isSelected()) { err("Please accept the Terms & Conditions."); return; }
        if (ds.usernameTaken(user)) { err("That username is already taken."); return; }
        if (!teacher && ds.regTaken(reg)) { err("This registration number is already registered."); return; }

        Account acc = new Account(user, passField.getText(), teacher ? "teacher" : "student", name, reg);
        ds.accounts.add(acc);
        if (!teacher) {
            ds.students.add(new Student(reg, name, cls, email, school)); // student record created from school reg number
            ds.addActivity("New student registered: " + name);
        }
        ds.current = acc;
        Nav.go(teacher ? "TeacherPortalView" : "StudentPortalView");
    }

    @FXML private void onLogin() { Nav.go("LoginView"); }

    private void err(String m) { errorLabel.setText(m); }
}
