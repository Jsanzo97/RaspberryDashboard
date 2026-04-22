package model;

public class WeatherData {
    public final double temp;
    public final double tempMax;
    public final double tempMin;
    public final double windKmh;
    public final String iconCode;

    public WeatherData(double temp, double tempMax, double tempMin, double windKmh, String iconCode) {
        this.temp     = temp;
        this.tempMax  = tempMax;
        this.tempMin  = tempMin;
        this.windKmh  = windKmh;
        this.iconCode = iconCode;
    }
}
