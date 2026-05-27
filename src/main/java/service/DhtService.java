package service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;

public class DhtService {

    private final AtomicReference<Double> lastTemp     = new AtomicReference<>(null);
    private final AtomicReference<Double> lastHumidity = new AtomicReference<>(null);

    private Thread readerThread;

    /**
     * Starts the Python script as a subprocess and reads its output in a background thread.
     * onData is called on every successful reading with (temperature, humidity).
     * Must be called before reading values.
     */
    public void start(BiConsumer<Double, Double> onData) {
        readerThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Process process = new ProcessBuilder("python3", "dht22.py")
                            .redirectErrorStream(false)
                            .start();

                    try (BufferedReader reader = new BufferedReader(
                            new InputStreamReader(process.getInputStream()))) {

                        String line;
                        while ((line = reader.readLine()) != null) {
                            if (Thread.currentThread().isInterrupted()) break;
                            parseLine(line, onData);
                        }
                    }

                    process.waitFor();

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (Exception e) {
                    System.err.println("DHT reader error: " + e.getMessage());
                    try { Thread.sleep(5000); } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }, "dht-reader");

        readerThread.setDaemon(true);
        readerThread.start();
    }

    public void stop() {
        if (readerThread != null) readerThread.interrupt();
    }

    public Double getTemperature() {
        return lastTemp.get();
    }

    public Double getHumidity() {
        return lastHumidity.get();
    }

    private void parseLine(String line, BiConsumer<Double, Double> onData) {
        try {
            String[] parts = line.trim().split(",");
            if (parts.length != 2) return;
            double temp     = Double.parseDouble(parts[0]);
            double humidity = Double.parseDouble(parts[1]);
            lastTemp.set(temp);
            lastHumidity.set(humidity);
            onData.accept(temp, humidity);
        } catch (NumberFormatException ignored) { }
    }
}
