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

    private final Label lblTemp = new Label("--°C");
    private final Label lblWind = new Label("-- km/h");
    private final Label lblMax  = new Label("--°C");
    private final Label lblMin  = new Label("--°C");
    private final ImageView icon = new ImageView();
    private final VBox root;

    public WeatherWidget() {
        lblTemp.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: -color-fg-default;");

        lblMax.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #e05c5c;");
        lblMax.setMaxWidth(Double.MAX_VALUE);
        lblMax.setAlignment(Pos.CENTER_RIGHT);

        lblMin.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #6ab0f5;");
        lblMin.setMaxWidth(Double.MAX_VALUE);
        lblMin.setAlignment(Pos.CENTER_RIGHT);

        VBox maxMinCol = new VBox(lblMax, lblMin);
        maxMinCol.setAlignment(Pos.TOP_RIGHT);

        HBox iconRow = new HBox(icon, maxMinCol);
        iconRow.setAlignment(Pos.CENTER_RIGHT);
        icon.fitHeightProperty().bind(maxMinCol.heightProperty());
        icon.setPreserveRatio(true);

        root = new VBox(iconRow, lblTemp, lblWind);
        root.setAlignment(Pos.TOP_RIGHT);
        root.setPadding(new Insets(8, 8, 0, 0));
    }

    public VBox getView() {
        return root;
    }

    public void update(WeatherData data) {
        lblTemp.setText(String.format("%.0f°C", data.temp));
        lblMax.setText(String.format("%.0f°C", data.tempMax));
        lblMin.setText(String.format("%.0f°C", data.tempMin));
        icon.setImage(new Image("https://openweathermap.org/img/wn/" + data.iconCode + "@2x.png"));
        updateWind(data.windKmh);
    }

    private void updateWind(double kmh) {
        String color = ColorScale.forWind(kmh);
        lblWind.setText(String.format("💨 %.0f km/h", kmh));
        lblWind.setStyle("-fx-font-size: 12px; -fx-text-fill: " + color + ";");
    }
}
