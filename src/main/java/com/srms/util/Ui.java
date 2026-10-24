package com.srms.util;

import javafx.beans.property.SimpleStringProperty;
import javafx.scene.Node;
import javafx.scene.control.*;

import java.util.function.Function;

/** Small helpers shared by all controllers. */
public final class Ui {
    private Ui() {}

    public static void info(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        a.setHeaderText(null);
        a.setTitle("Student Result Management System");
        a.showAndWait();
    }

    public static boolean confirm(String msg) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION, msg, ButtonType.YES, ButtonType.NO);
        a.setHeaderText(null);
        return a.showAndWait().filter(b -> b == ButtonType.YES).isPresent();
    }

    /** Stops a segmented ToggleGroup from ending up with nothing selected. */
    public static void keepSelected(ToggleGroup g) {
        g.selectedToggleProperty().addListener((o, old, n) -> { if (n == null && old != null) old.setSelected(true); });
    }

    public static Label badge(String text) {
        Label l = new Label(text);
        String kind = switch (text) {
            case "Pass", "Active", "Resolved" -> "badge-ok";
            case "Fail" -> "badge-fail";
            default -> "badge-pending";
        };
        l.getStyleClass().addAll("badge", kind);
        return l;
    }

    public static Button btn(String text, String style, Runnable action) {
        Button b = new Button(text);
        b.getStyleClass().addAll(style, "btn-small");
        b.setOnAction(e -> action.run());
        return b;
    }

    public static <T> void fit(TableView<T> t) {
        t.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        t.setPlaceholder(new Label("No records to display"));
    }

    public static <T> TableColumn<T, String> col(String title, Function<T, String> f) {
        TableColumn<T, String> c = new TableColumn<>(title);
        c.setCellValueFactory(d -> new SimpleStringProperty(f.apply(d.getValue())));
        return c;
    }

    /** Text column rendered as a coloured status pill (Pass / Active / Resolved / Pending / Fail). */
    public static <T> TableColumn<T, String> badgeCol(String title, Function<T, String> f) {
        TableColumn<T, String> c = col(title, f);
        c.setCellFactory(x -> new TableCell<T, String>() {
            @Override protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                setText(null);
                setGraphic(empty || v == null ? null : badge(v));
            }
        });
        return c;
    }

    public static <T> TableColumn<T, Void> actionCol(String title, Function<T, Node> maker) {
        TableColumn<T, Void> c = new TableColumn<>(title);
        c.setSortable(false);
        c.setCellFactory(x -> new TableCell<T, Void>() {
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                T item = (empty || getTableRow() == null) ? null : getTableRow().getItem();
                setGraphic(item == null ? null : maker.apply(item));
            }
        });
        return c;
    }
}
