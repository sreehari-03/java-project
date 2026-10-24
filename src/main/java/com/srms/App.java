package com.srms;

import javafx.application.Application;
import javafx.stage.Stage;

public class App extends Application {
    @Override
    public void start(Stage stage) {
        stage.setTitle("Student Result Management System");
        stage.setMinWidth(1100);
        stage.setMinHeight(740);
        Nav.init(stage);
        Nav.go("LoginView");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
