package com.srms;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.UncheckedIOException;

/** Screen switching: Login <-> Register <-> Teacher/Student portals. */
public final class Nav {
    private static Scene scene;

    private Nav() {}

    public static void init(Stage stage) {
        scene = new Scene(new StackPane(), 1280, 800);
        scene.getStylesheets().add(Nav.class.getResource("/css/style.css").toExternalForm());
        stage.setScene(scene);
    }

    /** Loads /fxml/{name}.fxml into the scene and returns its controller. */
    public static <T> T go(String name) {
        try {
            FXMLLoader loader = new FXMLLoader(Nav.class.getResource("/fxml/" + name + ".fxml"));
            Parent root = loader.load();
            scene.setRoot(root);
            return loader.getController();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
