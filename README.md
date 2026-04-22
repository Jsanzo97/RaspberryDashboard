# 🖥️ RaspberryDashboard

[![Java](https://img.shields.io/badge/Java-17-grey?style=flat&logo=openjdk&logoColor=white&labelColor=orange)](https://www.oracle.com/java/)
[![Maven](https://img.shields.io/badge/Maven-3.8.1-grey?style=flat&logo=apache-maven&logoColor=white&labelColor=red)](https://maven.apache.org/)
[![JavaFX](https://img.shields.io/badge/JavaFX-17-grey?style=flat&logo=java&logoColor=white&labelColor=blue)](https://openjfx.io/)
[![AtlantaFX](https://img.shields.io/badge/Theme-AtlantaFX-grey?style=flat&logo=java&logoColor=white&labelColor=blueviolet)](https://github.com/mkpaz/atlantafx)

A modern, full-featured system dashboard built for Raspberry Pi with the official touchscreen. Displays real-time system metrics and environmental data through a clean, touch-friendly interface with automatic brightness control.

---

## 🚀 Features

### 🕐 Clock & System Info
- **Live Clock**: Always-visible current time display.
- **System Uptime**: Card showing how long the device has been running.
- **Network & IP**: Card displaying the current network interface and assigned IP address.

### 📊 System Monitoring
- **CPU Load**: Real-time processor usage with progress bar.
- **CPU Temperature**: Live thermal readout of the device's processor.
- **RAM Usage**: Memory consumption with usage bar (`used / total GB`).
- **Swap Usage**: Swap space utilization with progress bar.
- **Storage Usage**: Disk usage on the main partition with progress bar.

### 🌡️ Environmental Sensors
- **Ambient Temperature**: Room temperature readout from an external sensor.
- **Ambient Humidity**: Current relative humidity from an external sensor.

### 🌤️ Weather Widget
- **Current Temperature**: Live outdoor temperature from OpenWeatherMap.
- **Daily High / Low**: Max shown in red, min shown in blue, alongside the weather icon.
- **Wind Speed**: Current wind speed in km/h.
- **Weather Icon**: Condition icon updated every 15 minutes.

### ⚙️ Application
- **Ultra-Fast Startup**: Optimized Fat JAR execution eliminates Maven overhead at launch.
- **Brightness Management**: Automatic screen brightness adjustment (`rpi_backlight`) based on time of day.
- **Modern UI**: **AtlantaFX** (PrimerDark) for a high-end look & feel.
- **Autostart**: Bash script configured to launch automatically when the desktop starts.
- **Standalone**: Bundles all required dependencies and native binaries for ARM architectures.

---

## 🛠️ Technologies

- **Language**: Java 17
- **GUI Framework**: JavaFX 17
- **Dependency Manager**: Maven
- **Styling**: AtlantaFX — PrimerDark theme
- **Weather API**: OpenWeatherMap (requires API key)
- **Hardware Target**: Raspberry Pi (with `backlight` support via `/sys/class/backlight/`)

---

## 📦 Installation & Build

Since the project uses an optimized launcher to avoid module errors, follow these steps:

**1. Clone the repository:**

```bash
git clone https://github.com/Jsanzo97/RaspberryDashboard.git
cd RaspberryDashboard
```

**2. Set up your environment variables** by creating a `.env` file in the project root:

```env
OPENWEATHER_API_KEY=your_api_key_here
WEATHER_CITY=YourCity
```

**3. Build the project (Generate JAR):**

```bash
mvn clean package
```

This will generate `dashboard-rpi-1.0-SNAPSHOT-jar-with-dependencies.jar` inside the `target/` folder.

**4. Run manually:**

```bash
./launch_dashboard.sh
```

---

## ⚙️ Autostart Configuration

To have the dashboard launch automatically when the Raspberry Pi boots:

**1. Make sure the script has execution permissions:**

```bash
chmod +x launch_dashboard.sh
```

**2. Create the autostart entry for LXDE:**

```bash
nano ~/.config/autostart/dashboard.desktop
```

**3. Paste the following content (adjust the path accordingly):**

```ini
[Desktop Entry]
Type=Application
Name=RaspberryDashboard
Exec=/home/pi/your-path/launch_dashboard.sh
```

---

## 🔍 Project Structure

```
RaspberryDashboard/
├── src/main/java/
│   ├── Dashboard.java       # Main class with UI logic and brightness control
│   └── Main.java            # Entry launcher for Fat JAR compatibility
├── launch_dashboard.sh      # Bash script that sets up the graphical environment and launches the app
├── .env                     # API key and city config (not committed)
└── pom.xml                  # Maven configuration with native dependencies for Linux-ARM
```

---

## 👀 Dashboard

<img src="https://github.com/Jsanzo97/RaspberryDashboard/blob/main/images/dashboard_screenshot.png">
