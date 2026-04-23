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
        tLabel.setStyle("-fx-font-size: 12px; -fx-opacity: 0.8;");
        tLabel.setWrapText(true);
        
        VBox box = new VBox(4, tLabel, valueLabel);
        box.setPadding(new Insets(8, 8, 8, 8));
        box.setStyle("-fx-background-color: -color-bg-subtle; -fx-background-radius: 8;");
        box.setAlignment(Pos.CENTER_LEFT);
        box.setMaxWidth(Double.MAX_VALUE);

        tLabel.prefWidthProperty().bind(box.widthProperty().subtract(24));

        VBox.setVgrow(box, Priority.ALWAYS);
        return box;
    }

    public static VBox progressTile(String title, Label valueLabel, ProgressBar bar, String icon) {
        bar.setMaxWidth(Double.MAX_VALUE);
        bar.setPrefHeight(8);
        valueLabel.setStyle("-fx-font-size: 10px; -fx-font-weight: bold;");

        Label tLabel = new Label(icon + "  " + title);
        tLabel.setStyle("-fx-font-size: 12px; -fx-opacity: 0.8;");
        tLabel.setWrapText(true);

        BorderPane header = new BorderPane();
        header.setLeft(tLabel);
        header.setRight(valueLabel);

        VBox box = new VBox(4, header, bar);
        box.setPadding(new Insets(8, 8, 8, 8));
        box.setStyle("-fx-background-color: -color-bg-subtle; -fx-background-radius: 10;");
        box.setAlignment(Pos.CENTER_LEFT);
        box.setMaxWidth(Double.MAX_VALUE);

        header.maxWidthProperty().bind(box.widthProperty().subtract(20));
        tLabel.prefWidthProperty().bind(header.widthProperty().subtract(valueLabel.widthProperty()).subtract(10));

        VBox.setVgrow(box, Priority.ALWAYS);
        return box;
    }
}
