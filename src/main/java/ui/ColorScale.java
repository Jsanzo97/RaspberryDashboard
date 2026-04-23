package ui;

public class ColorScale {

    // Shared 5-step scale: Cyan → Green → Yellow → Orange → Red
    public static final String STEP_1 = "#22d3ee"; // Best / Calm
    public static final String STEP_2 = "#4ade80";
    public static final String STEP_3 = "#facc15";
    public static final String STEP_4 = "#fb923c";
    public static final String STEP_5 = "#f87171"; // Worst / Intense

    /**
     * Returns a color from the 5-step scale based on wind speed in km/h.
     * Calm → Intense
     */
    public static String forWind(double kmh) {
        if (kmh <= 15)       return STEP_1;
        else if (kmh <= 30)  return STEP_2;
        else if (kmh <= 50)  return STEP_3;
        else if (kmh <= 70)  return STEP_4;
        else                 return STEP_5;
    }

    /**
     * Returns a color from the 5-step scale based on WiFi RSSI in dBm.
     * Strong → Weak (scale is inverted — lower dBm = worse signal)
     */
    public static String forSignal(int dbm) {
        if (dbm >= -50)       return STEP_1;
        else if (dbm >= -60)  return STEP_2;
        else if (dbm >= -70)  return STEP_3;
        else if (dbm >= -80)  return STEP_4;
        else                  return STEP_5;
    }
}
