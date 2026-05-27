import atlantafx.base.theme.PrimerDark;
import io.github.cdimascio.dotenv.Dotenv;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
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
import model.WeatherData;
import service.SystemService;
import service.DhtService;
import service.WeatherService;
import ui.NetworkWidget;
import ui.TileFactory;
import ui.WeatherWidget;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Dashboard extends Application {

    // --- Config ---
    private static final String BRIGHTNESS_PATH = "/sys/class/backlight/10-0045/brightness";
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");

    // --- Services ---
    private final SystemService   systemService = new SystemService();
    private final ExecutorService executor      = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "dashboard-bg");
        t.setDaemon(true);
        return t;
    });

    // --- UI labels & bars ---
    private Label lblTime, lblUptime, lblNet;
    private Label lblCpu, lblCpuTemp;
    private Label lblAmbTemp, lblHumidity;
    private Label lblRam, lblSwap, lblDisk;
    private ProgressBar progressCpu, progressRam, progressSwap, progressDisk;

    private WeatherWidget  weatherWidget;
    private WeatherService weatherService;
    private NetworkWidget  networkWidget;
    private DhtService     dhtService;
    private GridPane grid;

    private int tickCount  = 0;
    private int lastBright = -1;

    // -------------------------------------------------------------------------

    @Override
    public void start(Stage stage) {
        Dotenv dotenv = Dotenv.load();

        weatherService = new WeatherService(
                dotenv.get("OPENWEATHER_API_KEY"),
                dotenv.get("WEATHER_CITY")
        );
        weatherWidget = new WeatherWidget();
        networkWidget = new NetworkWidget();
        dhtService    = new DhtService();

        Application.setUserAgentStylesheet(new PrimerDark().getUserAgentStylesheet());
        stage.initStyle(StageStyle.UNDECORATED);

        AnchorPane root = new AnchorPane();
        root.setStyle("-fx-background-color: -color-bg-default;");

        // Weather widget — top right
        VBox weatherView = weatherWidget.getView();
        AnchorPane.setTopAnchor(weatherView, 0.0);
        AnchorPane.setRightAnchor(weatherView, 0.0);

        // Network widget — top left
        VBox networkView = networkWidget.getView();
        AnchorPane.setTopAnchor(networkView, 0.0);
        AnchorPane.setLeftAnchor(networkView, 0.0);

        root.getChildren().addAll(buildGrid(), weatherView, networkView);

        Scene scene = new Scene(root, 800, 400);
        scene.setCursor(Cursor.NONE);
        scene.setOnMouseClicked(e -> stage.setIconified(true));

        stage.setScene(scene);
        stage.setFullScreen(true);
        stage.setFullScreenExitHint("");
        stage.show();

        dhtService.start((temp, humidity) -> Platform.runLater(() -> {
            lblAmbTemp.setText(String.format("%.1f°C", temp));
            lblHumidity.setText(String.format("%.1f%%", humidity));
        }));
        refreshSystemData();
        refreshNetwork();
        refreshWeather();
        startTimeline();
    }

    // -------------------------------------------------------------------------
    // Layout
    // -------------------------------------------------------------------------

    private StackPane buildGrid() {
        grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);
        grid.setAlignment(Pos.CENTER);
        grid.setPadding(new Insets(16, 64, 16, 64));
        grid.setPrefSize(800, 400);

        ColumnConstraints col = new ColumnConstraints();
        col.setPercentWidth(33.3);
        grid.getColumnConstraints().addAll(col, col, col);
        RowConstraints row = new RowConstraints();
        row.setVgrow(Priority.ALWAYS);
        row.setPercentHeight(25);
        grid.getRowConstraints().addAll(row, row, row, row);

        // Row 0 — clock
        lblTime = new Label();
        lblTime.setStyle("-fx-font-size: 75px; -fx-font-weight: bold; -fx-text-fill: -color-accent-emphasis;");
        VBox clockBox = new VBox(lblTime);
        clockBox.setAlignment(Pos.CENTER);
        grid.add(clockBox, 0, 0, 3, 1);

        // Row 1
        grid.add(TileFactory.metricTile("Uptime Sistema",    lblUptime   = new Label("..."),                "⏱️"), 0, 1);
        grid.add(TileFactory.metricTile("Red / IP",          lblNet      = new Label(systemService.getLocalIPAddress()), "🌐"), 1, 1);
        grid.add(TileFactory.progressTile("Carga CPU",       lblCpu      = new Label("0%"),                 progressCpu  = new ProgressBar(0), "⚡"), 2, 1);

        // Row 2
        grid.add(TileFactory.metricTile("Temp. Dispositivo", lblCpuTemp  = new Label("0°C"),                "🖥️"), 0, 2);
        grid.add(TileFactory.metricTile("Temp. Ambiente",    lblAmbTemp  = new Label("22.0°C"),             "🌡️"), 1, 2);
        grid.add(TileFactory.metricTile("Humedad Rel.",      lblHumidity = new Label("45%"),                "💧"), 2, 2);

        // Row 3
        grid.add(TileFactory.progressTile("Memoria RAM",     lblRam      = new Label("0/0 GB"),             progressRam  = new ProgressBar(0), "📊"), 0, 3);
        grid.add(TileFactory.progressTile("Memoria SWAP",    lblSwap     = new Label("0/0 MB"),             progressSwap = new ProgressBar(0), "🔄"), 1, 3);
        grid.add(TileFactory.progressTile("Almacenamiento",  lblDisk     = new Label("0/0 GB"),             progressDisk = new ProgressBar(0), "💾"), 2, 3);

        StackPane wrapper = new StackPane(grid);
        AnchorPane.setTopAnchor(wrapper, 10.0);
        AnchorPane.setBottomAnchor(wrapper, 0.0);
        AnchorPane.setLeftAnchor(wrapper, 0.0);
        AnchorPane.setRightAnchor(wrapper, 0.0);
        return wrapper;
    }

    // -------------------------------------------------------------------------
    // Timeline
    // -------------------------------------------------------------------------

    private void startTimeline() {
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            int brightness = brightnessForHour(LocalTime.now().getHour());

            if (brightness != lastBright) {
                systemService.setBacklight(BRIGHTNESS_PATH, brightness);
                grid.setVisible(brightness > 0);
                lastBright = brightness;
            }

            if (brightness > 0) {
                lblTime.setText(LocalDateTime.now().format(TIME_FORMAT));
                tickCount++;
                if (tickCount % 30  == 0) refreshSystemData();
                refreshCpu();
                refreshNetwork();
                if (tickCount % 900 == 0) { refreshWeather(); tickCount = 0; }
            }
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private int brightnessForHour(int hour) {
        if (hour < 8)       return 0;
        else if (hour < 10) return 100;
        else if (hour < 21) return 150;
        else                return 100;
    }

    // -------------------------------------------------------------------------
    // Data refresh
    // -------------------------------------------------------------------------

    private void refreshSystemData() {
        double load = systemService.getCpuLoad();
        lblCpu.setText(String.format("%.1f%%", load * 100));
        progressCpu.setProgress(load);

        lblCpuTemp.setText(String.format("%.1f°C", systemService.getCpuTemperature()));
        lblUptime.setText(systemService.getSystemUptime());

        double[] ram = systemService.getRamMb();
        if (ram != null) {
            lblRam.setText(String.format("%.1f/%.1f GB", ram[0] / 1024, ram[1] / 1024));
            progressRam.setProgress(ram[0] / ram[1]);
        }

        double[] swap = systemService.getSwapMb();
        if (swap != null) {
            lblSwap.setText(String.format("%.0f/%.0f MB", swap[0], swap[1]));
            progressSwap.setProgress(swap[1] > 0 ? swap[0] / swap[1] : 0);
        }

        String[] disk = systemService.getDiskStats();
        if (disk != null) {
            lblDisk.setText(disk[1] + "/" + disk[2]);
            progressDisk.setProgress(Double.parseDouble(disk[0]) / 100.0);
        }
    }

    private void refreshWeather() {
        executor.submit(() -> {
            try {
                WeatherData data = weatherService.fetch();
                Platform.runLater(() -> weatherWidget.update(data));
            } catch (Exception e) {
                System.err.println("Weather fetch failed: " + e.getMessage());
            }
        });
    }

    private void refreshCpu() {
        double load = systemService.getCpuLoad();
        lblCpu.setText(String.format("%.1f%%", load * 100));
        progressCpu.setProgress(load);
    }

    private void refreshNetwork() {
        double[] speed = systemService.getNetworkSpeed();
        Platform.runLater(() -> networkWidget.update(speed[0], speed[1]));
    }

    // -------------------------------------------------------------------------

    @Override
    public void stop() {
        executor.shutdownNow();
        dhtService.stop();
    }

    public static void main(String[] args) { launch(args); }
}
