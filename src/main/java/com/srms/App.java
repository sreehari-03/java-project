package com.srms;

import com.srms.model.User;
import com.srms.service.AuthService;
import javafx.application.Application;
import javafx.stage.Stage;

public class App extends Application {
    @Override
    public void start(Stage stage) {
        stage.setTitle("Student Result Management System");
        stage.setMinWidth(1100);
        stage.setMinHeight(740);

        // Pre-configure active teacher user session
        AuthService.setCurrentUser(new User(1, "teacher", "", "teacher", "Ms. Sharma", "sharma@school.edu"));

        Nav.init(stage);
        Nav.go("TeacherPortalView");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
