import atlantafx.base.theme.PrimerDark;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.io.*;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Enumeration;

public class Dashboard extends Application {

    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    private Label lblTime, lblCpuTemp, lblCpu, lblRam, lblSwap, lblUptime, lblAmbTemp, lblHumidity, lblDisk, lblNet;
    private ProgressBar progressCpu, progressRam, progressSwap, progressDisk;
    private GridPane grid;
    
    private int tickCount = 0;
    private int lastBright = -1;
    private final String BRIGHTNESS_PATH = "/sys/class/backlight/10-0045/brightness";

    @Override
    public void start(Stage stage) {
        Application.setUserAgentStylesheet(new PrimerDark().getUserAgentStylesheet());
        stage.initStyle(StageStyle.UNDECORATED);

        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: -color-bg-default;");

        grid = new GridPane();
        grid.setHgap(12); grid.setVgap(10);
        grid.setAlignment(Pos.CENTER);
        grid.setPadding(new Insets(16, 64, 16, 64)); 
        grid.setPrefSize(800, 400);

        setupGridConstraints();

        // --- UI ---
        lblTime = new Label();
        lblTime.setStyle("-fx-font-size: 70px; -fx-font-weight: bold; -fx-text-fill: -color-accent-emphasis;");
        VBox headerBox = new VBox(lblTime); headerBox.setAlignment(Pos.CENTER);
        grid.add(headerBox, 0, 0, 3, 1);
        
        grid.add(createMetricTile("Uptime Sistema", lblUptime = new Label("..."), "⏱️"), 0, 1);
        grid.add(createMetricTile("Red / IP", lblNet = new Label(getLocalIPAddress()), "🌐"), 1, 1);
        grid.add(createProgressTile("Carga CPU", lblCpu = new Label("0%"), progressCpu = new ProgressBar(0)), 2, 1);

        grid.add(createMetricTile("Temp. Dispositivo", lblCpuTemp = new Label("0°C"), "🖥️"), 0, 2);
        grid.add(createMetricTile("Temp. Ambiente", lblAmbTemp = new Label("22.0°C"), "🏠"), 1, 2);
        grid.add(createMetricTile("Humedad Rel.", lblHumidity = new Label("45%"), "💧"), 2, 2);

        grid.add(createProgressTile("Memoria RAM", lblRam = new Label("0/0 GB"), progressRam = new ProgressBar(0)), 0, 3);
        grid.add(createProgressTile("Memoria SWAP", lblSwap = new Label("0/0 MB"), progressSwap = new ProgressBar(0)), 1, 3);
        grid.add(createProgressTile("Almacenamiento", lblDisk = new Label("0/0 GB"), progressDisk = new ProgressBar(0)), 2, 3);

        root.getChildren().add(grid);

        Scene scene = new Scene(root, 800, 400);
        scene.setCursor(Cursor.NONE);
        scene.setOnMouseClicked(event -> stage.setIconified(true));

        stage.setScene(scene);
        stage.setFullScreen(true);
        stage.setFullScreenExitHint("");
        stage.show();

        updateSystemData();
        startMasterTimeline();
    }

    private void startMasterTimeline() {
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            LocalTime now = LocalTime.now();
            int currentBright;

            if (now.getHour() >= 0 && now.getHour() < 8) {
                currentBright = 0;
            } else if (now.getHour() >= 8 && now.getHour() < 10) {
                currentBright = 100;
            } else if (now.getHour() >= 10 && now.getHour() < 21) {
                currentBright = 150;
            } else { 
                currentBright = 100;
            }

            if (currentBright != lastBright) {
                setBacklight(currentBright);
                grid.setVisible(currentBright > 0);
                lastBright = currentBright;
            }

            if (currentBright > 0) {
                lblTime.setText(LocalDateTime.now().format(timeFormatter));
                
                tickCount++;
                if (tickCount >= 30) {
                    updateSystemData();
                    tickCount = 0;
                }
            }
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void setBacklight(int value) {
        try (FileWriter fw = new FileWriter(BRIGHTNESS_PATH)) {
            fw.write(String.valueOf(value));
        } catch (IOException e) {
	}
    }

    private void updateSystemData() {
        double load = getCpuLoad();
        lblCpu.setText(String.format("%.1f%%", load * 100));
        progressCpu.setProgress(load);

        updateMemoryStats();
        updateDiskStats();

        double temp = getCpuTemperature();
        lblCpuTemp.setText(String.format("%.1f°C", temp));
        lblCpuTemp.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: " + 
            (temp > 85 ? "#ff0000" : (temp > 75 ? "#ff8c00" : "-color-fg-default")));

        lblUptime.setText(getSystemUptime());
        lblNet.setText(getLocalIPAddress());
    }

    private void setupGridConstraints() {
        ColumnConstraints col = new ColumnConstraints();
        col.setPercentWidth(33.3);
        grid.getColumnConstraints().addAll(col, col, col);
        RowConstraints rowHeight = new RowConstraints();
        rowHeight.setVgrow(Priority.ALWAYS);
        rowHeight.setPercentHeight(25); 
        grid.getRowConstraints().addAll(rowHeight, rowHeight, rowHeight, rowHeight);
    }

    private double getCpuLoad() {
        try (BufferedReader reader = new BufferedReader(new FileReader("/proc/loadavg"))) {
            String line = reader.readLine();
            return line != null ? Math.min(Double.parseDouble(line.split(" ")[0]) / 4.0, 1.0) : 0;
        } catch (Exception e) { return 0; }
    }

    private void updateMemoryStats() {
        try {
            Process p = Runtime.getRuntime().exec("free -m");
            BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
            in.readLine(); String ramLine = in.readLine();
            if (ramLine != null) {
                String[] ram = ramLine.trim().split("\\s+");
                double total = Double.parseDouble(ram[1]);
                double used = Double.parseDouble(ram[2]);
                lblRam.setText(String.format("%.1f/%.1f GB", used/1024, total/1024));
                progressRam.setProgress(used / total);
            }
            String swapLine = in.readLine();
            if (swapLine != null) {
                String[] swap = swapLine.trim().split("\\s+");
                double sTotal = Double.parseDouble(swap[1]);
                double sUsed = Double.parseDouble(swap[2]);
                if (sTotal > 0) {
                    lblSwap.setText(String.format("%.0f/%.0f MB", sUsed, sTotal));
                    progressSwap.setProgress(sUsed / sTotal);
                } else { lblSwap.setText("Off"); progressSwap.setProgress(0); }
            }
        } catch (Exception e) { }
    }

    private void updateDiskStats() {
        try {
            Process p = Runtime.getRuntime().exec("df -h /");
            BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
            in.readLine(); String line = in.readLine();
            if (line != null) {
                String[] disk = line.trim().split("\\s+");
                lblDisk.setText(disk[2] + "/" + disk[1]);
                progressDisk.setProgress(Double.parseDouble(disk[4].replace("%", "")) / 100.0);
            }
        } catch (Exception e) { }
    }

    private double getCpuTemperature() {
        try (BufferedReader reader = new BufferedReader(new FileReader("/sys/class/thermal/thermal_zone0/temp"))) {
            return Double.parseDouble(reader.readLine()) / 1000.0;
        } catch (Exception e) { return 0; }
    }

    private String getSystemUptime() {
    	try (BufferedReader reader = new BufferedReader(new FileReader("/proc/uptime"))) {
            double seconds = Double.parseDouble(reader.readLine().split(" ")[0]);
            int days = (int) (seconds / 86400);
            int hours = (int) ((seconds % 86400) / 3600);
            int minutes = (int) ((seconds % 3600) / 60);
        
            return days + "d " + hours + "h " + minutes + "m";
    	} catch (Exception e) { return "N/A"; }
    } 

    private String getLocalIPAddress() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                if (iface.isLoopback() || !iface.isUp()) continue;
                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    if (addr.getHostAddress().contains(".") && !addr.isLoopbackAddress()) return addr.getHostAddress();
                }
            }
        } catch (Exception e) { }
        return "127.0.0.1";
    }

    private VBox createMetricTile(String title, Label valueLabel, String icon) {
        valueLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: -color-fg-default;");
        Label tLabel = new Label(icon + " " + title);
        tLabel.setStyle("-fx-font-size: 11px; -fx-opacity: 0.8;");
        VBox box = new VBox(2, tLabel, valueLabel);
        box.setPadding(new Insets(10, 12, 10, 12));
        box.setStyle("-fx-background-color: -color-bg-subtle; -fx-background-radius: 10;");
        box.setAlignment(Pos.CENTER_LEFT);
        box.setMaxHeight(Double.MAX_VALUE);
        VBox.setVgrow(box, Priority.ALWAYS);
        return box;
    }

    private VBox createProgressTile(String title, Label valueLabel, ProgressBar bar) {
        bar.setMaxWidth(Double.MAX_VALUE);
        bar.setPrefHeight(10);
        valueLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");
        Label tLabel = new Label(title);
        tLabel.setStyle("-fx-font-size: 11px; -fx-opacity: 0.8;");
        BorderPane header = new BorderPane();
        header.setLeft(tLabel);
        header.setRight(valueLabel);
        VBox box = new VBox(5, header, bar);
        box.setPadding(new Insets(10, 10, 10, 10));
        box.setStyle("-fx-background-color: -color-bg-subtle; -fx-background-radius: 10;");
        box.setAlignment(Pos.CENTER_LEFT);
        box.setMaxHeight(Double.MAX_VALUE);
        VBox.setVgrow(box, Priority.ALWAYS);
        return box;
    }

    public static void main(String[] args) { launch(args); }
}
