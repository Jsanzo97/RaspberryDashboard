package ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.io.BufferedReader;
import java.io.FileReader;

public class NetworkWidget {

    private final Label lblInterface = new Label("📶  WiFi");
    private final Label lblUp        = new Label("↑  --");
    private final Label lblDown      = new Label("↓  --");
    private final Label lblSignal    = new Label("📡  --");
    private final VBox  root;

    public NetworkWidget() {
        lblInterface.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: -color-fg-default;");
        lblInterface.setAlignment(Pos.CENTER_LEFT);
        lblInterface.setMaxWidth(Double.MAX_VALUE);

        lblUp.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #e05c5c;");
        lblUp.setAlignment(Pos.CENTER_LEFT);
        lblUp.setMaxWidth(Double.MAX_VALUE);

        lblDown.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #6ab0f5;");
        lblDown.setAlignment(Pos.CENTER_LEFT);
        lblDown.setMaxWidth(Double.MAX_VALUE);

        lblSignal.setStyle("-fx-font-size: 12px; -fx-text-fill: #a0a0a0;");
        lblSignal.setAlignment(Pos.CENTER_LEFT);
        lblSignal.setMaxWidth(Double.MAX_VALUE);

        root = new VBox(lblInterface, lblUp, lblDown, lblSignal);
        root.setAlignment(Pos.TOP_LEFT);
        root.setPadding(new Insets(8, 0, 0, 8));
    }

    public VBox getView() {
        return root;
    }

    public void update(double uploadBytesPerSec, double downloadBytesPerSec) {
        lblUp.setText("↑  " + formatSpeed(uploadBytesPerSec));
        lblDown.setText("↓  " + formatSpeed(downloadBytesPerSec));
        updateSignalQuality();
    }

    private void updateSignalQuality() {
        int dbm = getWifiRSSI();
        if (dbm == 0) {
            lblSignal.setText("📡  Desconectado");
            lblSignal.setStyle("-fx-font-size: 11px; -fx-text-fill: #666666;");
            return;
        }

        String color = ColorScale.forSignal(dbm);
        String quality;
        if (dbm >= -50)       quality = "Máxima";
        else if (dbm >= -60)  quality = "Excelente";
        else if (dbm >= -70)  quality = "Buena";
        else if (dbm >= -80)  quality = "Mala";
        else                  quality = "Muy Mala";

        lblSignal.setText("📡  " + quality);
        lblSignal.setStyle("-fx-font-size: 11px; -fx-text-fill: " + color + ";");
    }

    private int getWifiRSSI() {
        try (BufferedReader br = new BufferedReader(new FileReader("/proc/net/wireless"))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.contains("wlan0")) {
                    String[] parts = line.trim().split("\\s+");
                    return (int) Double.parseDouble(parts[3]);
                }
            }
        } catch (Exception ignored) { }
        return 0;
    }

    private String formatSpeed(double bytesPerSec) {
        if (bytesPerSec >= 1_048_576)
            return String.format("%.1f MB/s", bytesPerSec / 1_048_576);
        else if (bytesPerSec >= 1024)
            return String.format("%.0f KB/s", bytesPerSec / 1024);
        else
            return String.format("%.0f B/s", bytesPerSec);
    }
}
