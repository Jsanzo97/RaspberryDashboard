package ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.*;

public class TileFactory {

    public static VBox metricTile(String title, Label valueLabel, String icon) {
        valueLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: -color-fg-default;");
        Label tLabel = new Label(icon + "  " + title);
        tLabel.setStyle("-fx-font-size: 13px; -fx-opacity: 0.8;");

        VBox box = new VBox(2, tLabel, valueLabel);
        box.setPadding(new Insets(10, 12, 10, 12));
        box.setStyle("-fx-background-color: -color-bg-subtle; -fx-background-radius: 10;");
        box.setAlignment(Pos.CENTER_LEFT);
        VBox.setVgrow(box, Priority.ALWAYS);
        return box;
    }

    public static VBox progressTile(String title, Label valueLabel, ProgressBar bar, String icon) {
        bar.setMaxWidth(Double.MAX_VALUE);
        bar.setPrefHeight(10);
        valueLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");

        Label tLabel = new Label(icon + "  " + title);
        tLabel.setStyle("-fx-font-size: 13px; -fx-opacity: 0.8;");

        BorderPane header = new BorderPane();
        header.setLeft(tLabel);
        header.setRight(valueLabel);

        VBox box = new VBox(5, header, bar);
        box.setPadding(new Insets(10, 10, 10, 10));
        box.setStyle("-fx-background-color: -color-bg-subtle; -fx-background-radius: 10;");
        box.setAlignment(Pos.CENTER_LEFT);
        VBox.setVgrow(box, Priority.ALWAYS);
        return box;
    }
}
