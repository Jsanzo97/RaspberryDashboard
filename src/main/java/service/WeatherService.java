package service;

import model.WeatherData;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class WeatherService {

    private static final String GEO_URL      = "https://api.openweathermap.org/geo/1.0/direct";
    private static final String ONECALL_URL  = "https://api.openweathermap.org/data/3.0/onecall";

    private final String apiKey;
    private final String city;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    private double cachedLat = Double.NaN;
    private double cachedLon = Double.NaN;

    public WeatherService(String apiKey, String city) {
        this.apiKey = apiKey;
        this.city   = city;
    }

    /** Fetches current weather synchronously. Call from a background thread. */
    public WeatherData fetch() throws Exception {
        if (Double.isNaN(cachedLat)) resolveCoordinates();

        String url = ONECALL_URL
                + "?lat="     + cachedLat
                + "&lon="     + cachedLon
                + "&appid="   + apiKey
                + "&units=metric"
                + "&exclude=minutely,hourly,alerts";

        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200)
            throw new IOException("One Call API returned HTTP " + response.statusCode());

        JsonObject json    = JsonParser.parseString(response.body()).getAsJsonObject();
        JsonObject current = json.getAsJsonObject("current");
        JsonObject today   = json.getAsJsonArray("daily").get(0).getAsJsonObject();
        JsonObject temp    = today.getAsJsonObject("temp");

        double currentTemp = current.get("temp").getAsDouble();
        double tempMax     = temp.get("max").getAsDouble();
        double tempMin     = temp.get("min").getAsDouble();
        double windMs      = current.get("wind_speed").getAsDouble();
        String icon        = current.getAsJsonArray("weather")
                                    .get(0).getAsJsonObject()
                                    .get("icon").getAsString();

        return new WeatherData(currentTemp, tempMax, tempMin, windMs * 3.6, icon);
    }

    /** Resolves city name to coordinates using the Geocoding API. Cached after first call. */
    private void resolveCoordinates() throws Exception {
        String url = GEO_URL
                + "?q="      + city.replace(" ", "%20")
                + "&limit=1"
                + "&appid="  + apiKey;

        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200)
            throw new IOException("Geocoding API returned HTTP " + response.statusCode());

        JsonArray results = JsonParser.parseString(response.body()).getAsJsonArray();
        if (results.isEmpty())
            throw new IOException("No coordinates found for city: " + city);

        JsonObject location = results.get(0).getAsJsonObject();
        cachedLat = location.get("lat").getAsDouble();
        cachedLon = location.get("lon").getAsDouble();
    }
}
