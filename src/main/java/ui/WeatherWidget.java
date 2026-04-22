package ui;

import model.WeatherData;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class WeatherWidget {

    private final Label lblTemp    = new Label("--°C");
    private final Label lblWind    = new Label("-- km/h");
    private final Label lblCity;
    private final Label lblMax     = new Label("--°C");
    private final Label lblMin     = new Label("--°C");
    private final ImageView icon   = new ImageView();

    private final VBox root;

    public WeatherWidget(String cityName) {
        this.lblCity = new Label(cityName);

        icon.setFitWidth(45);
        icon.setPreserveRatio(true);

        lblTemp.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: -color-fg-default;");
        lblTemp.setPadding(new Insets(0, 4, 0, 0));

        lblWind.setStyle("-fx-font-size: 12px; -fx-text-fill: white;");
        lblWind.setPadding(new Insets(0, 4, 0, 0));

        lblCity.setStyle("-fx-font-size: 11px; -fx-opacity: 0.6; -fx-font-weight: bold;");
        lblCity.setPadding(new Insets(0, 4, 0, 0));

        lblMax.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #e05c5c;");
        lblMin.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #6ab0f5;");

        VBox maxMinCol = new VBox(6, lblMax, lblMin);
        maxMinCol.setAlignment(Pos.CENTER);
        maxMinCol.setPrefHeight(45);
        maxMinCol.setMaxHeight(45);
        maxMinCol.setPadding(new Insets(0, 4, 0, 0));

        HBox iconRow = new HBox(4, icon, maxMinCol);
        iconRow.setAlignment(Pos.CENTER_RIGHT);

        root = new VBox(iconRow, lblTemp, lblWind, lblCity);
        root.setAlignment(Pos.TOP_RIGHT);
        root.setPadding(new Insets(8, 8, 0, 0));
    }

    /** Returns the VBox to be placed in the scene. */
    public VBox getView() {
        return root;
    }

    /** Updates all labels and icon. Must be called on the JavaFX Application Thread. */
    public void update(WeatherData data) {
        lblTemp.setText(String.format("%.0f°C", data.temp));
        lblWind.setText(String.format("💨 %.0f km/h", data.windKmh));
        lblMax.setText(String.format("%.0f°C", data.tempMax));
        lblMin.setText(String.format("%.0f°C", data.tempMin));
        icon.setImage(new Image("https://openweathermap.org/img/wn/" + data.iconCode + "@2x.png"));
    }
}
