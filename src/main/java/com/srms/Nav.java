package com.srms;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;

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
        String resourcePath = "/fxml/" + name + ".fxml";
        URL resource = Nav.class.getResource(resourcePath);
        if (resource == null) {
            IllegalArgumentException error = new IllegalArgumentException(
                    "FXML resource not found: " + resourcePath);
            System.err.println("Unable to navigate to " + resourcePath);
            error.printStackTrace();
            throw error;
        }

        try {
            FXMLLoader loader = new FXMLLoader(resource);
            Parent root = loader.load();
            if (scene == null) {
                throw new IllegalStateException("Navigation scene has not been initialized.");
            }
            scene.setRoot(root);
            return loader.getController();
        } catch (IOException e) {
            System.err.println("Unable to load or display FXML resource: " + resourcePath);
            e.printStackTrace();
            throw new UncheckedIOException(
                    "Unable to load FXML resource: " + resourcePath, e);
        } catch (RuntimeException e) {
            System.err.println("Unable to load or display FXML resource: " + resourcePath);
            e.printStackTrace();
            throw e;
        }
    }
}
