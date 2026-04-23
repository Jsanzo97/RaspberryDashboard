package service;

import java.io.*;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

public class SystemService {

    public double getCpuLoad() {
        try (BufferedReader reader = new BufferedReader(new FileReader("/proc/loadavg"))) {
            String line = reader.readLine();
            return line != null ? Math.min(Double.parseDouble(line.split(" ")[0]) / 4.0, 1.0) : 0;
        } catch (Exception e) { return 0; }
    }

    public double getCpuTemperature() {
        try (BufferedReader reader = new BufferedReader(new FileReader("/sys/class/thermal/thermal_zone0/temp"))) {
            return Double.parseDouble(reader.readLine()) / 1000.0;
        } catch (Exception e) { return 0; }
    }

    public String getSystemUptime() {
        try (BufferedReader reader = new BufferedReader(new FileReader("/proc/uptime"))) {
            double seconds = Double.parseDouble(reader.readLine().split(" ")[0]);
            int days    = (int) (seconds / 86400);
            int hours   = (int) ((seconds % 86400) / 3600);
            int minutes = (int) ((seconds % 3600) / 60);
            return days + "d " + hours + "h " + minutes + "m";
        } catch (Exception e) { return "N/A"; }
    }

    public String getLocalIPAddress() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                if (iface.isLoopback() || !iface.isUp()) continue;
                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    if (addr.getHostAddress().contains(".") && !addr.isLoopbackAddress())
                        return addr.getHostAddress();
                }
            }
        } catch (Exception ignored) { }
        return "127.0.0.1";
    }

    /** Returns {usedMb, totalMb} for RAM, or null on failure. */
    public double[] getRamMb() {
        try {
            Process p = Runtime.getRuntime().exec("free -m");
            BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
            in.readLine();
            String line = in.readLine();
            if (line != null) {
                String[] parts = line.trim().split("\\s+");
                return new double[]{ Double.parseDouble(parts[2]), Double.parseDouble(parts[1]) };
            }
        } catch (Exception ignored) { }
        return null;
    }

    /** Returns {usedMb, totalMb} for SWAP, or null on failure. */
    public double[] getSwapMb() {
        try {
            Process p = Runtime.getRuntime().exec("free -m");
            BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
            in.readLine(); in.readLine(); // skip header + RAM line
            String line = in.readLine();
            if (line != null) {
                String[] parts = line.trim().split("\\s+");
                return new double[]{ Double.parseDouble(parts[2]), Double.parseDouble(parts[1]) };
            }
        } catch (Exception ignored) { }
        return null;
    }

    /** Returns {usedPercent, usedLabel, totalLabel} for root disk, or null on failure. */
    public String[] getDiskStats() {
        try {
            Process p = Runtime.getRuntime().exec("df -h /");
            BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
            in.readLine();
            String line = in.readLine();
            if (line != null) {
                String[] parts = line.trim().split("\\s+");
                // parts[1]=total, parts[2]=used, parts[4]=percent
                return new String[]{ parts[4].replace("%", ""), parts[2], parts[1] };
            }
        } catch (Exception ignored) { }
        return null;
    }

    public void setBacklight(String brightnessPath, int value) {
        try (FileWriter fw = new FileWriter(brightnessPath)) {
            fw.write(String.valueOf(value));
        } catch (IOException ignored) { }
    }
    // Stores previous sample for delta calculation
    private long lastRxBytes = -1;
    private long lastTxBytes = -1;
    private long lastSampleTime = -1;

    /**
     * Returns {uploadBytesPerSec, downloadBytesPerSec} since last call.
     * First call returns {0, 0} as it establishes the baseline.
     */
    public double[] getNetworkSpeed() {
        try (BufferedReader reader = new BufferedReader(new FileReader("/proc/net/dev"))) {
            String line;
            long rxBytes = 0, txBytes = 0;

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                // Skip loopback and header lines
                if (line.startsWith("lo:") || !line.contains(":")) continue;

                String[] parts = line.split(":\\s*|\\s+");
                // parts[0]=iface, parts[1]=rx_bytes, parts[9]=tx_bytes
                if (parts.length >= 10) {
                    rxBytes += Long.parseLong(parts[1]);
                    txBytes += Long.parseLong(parts[9]);
                }
            }

            long now = System.currentTimeMillis();

            if (lastSampleTime < 0) {
                // First call — establish baseline
                lastRxBytes = rxBytes;
                lastTxBytes = txBytes;
                lastSampleTime = now;
                return new double[]{ 0, 0 };
            }

            double elapsed = (now - lastSampleTime) / 1000.0;
            double upload   = (txBytes - lastTxBytes) / elapsed;
            double download = (rxBytes - lastRxBytes) / elapsed;

            lastRxBytes    = rxBytes;
            lastTxBytes    = txBytes;
            lastSampleTime = now;

            return new double[]{ Math.max(0, upload), Math.max(0, download) };

        } catch (Exception e) { return new double[]{ 0, 0 }; }
    }
}
