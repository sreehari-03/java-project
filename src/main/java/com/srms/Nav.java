package com.srms;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.ArrayDeque;
import java.util.Deque;

/** Screen switching: Login <-> Register <-> Teacher/Student portals. */
public final class Nav {
    private static Scene scene;
    private static String currentView;
    private static final Deque<String> history = new ArrayDeque<>();

    private Nav() {}

    public static void init(Stage stage) {
        scene = new Scene(new StackPane(), 1280, 800);
        scene.getStylesheets().add(Nav.class.getResource("/css/style.css").toExternalForm());
        stage.setScene(scene);
    }

    /** Loads /fxml/{name}.fxml into the scene and returns its controller. */
    public static <T> T go(String name) {
        return navigate(name, true);
    }

    /** Returns to the previously visited screen without adding another history entry. */
    public static boolean back() {
        if (history.isEmpty()) return false;
        navigate(history.peek(), false);
        history.pop();
        return true;
    }

    /** Clears prior authenticated screens before returning to the login screen. */
    public static void clearHistory() {
        history.clear();
    }

    private static <T> T navigate(String name, boolean rememberCurrent) {
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
            if (rememberCurrent && currentView != null && !currentView.equals(name)) {
                history.push(currentView);
            }
            currentView = name;
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
