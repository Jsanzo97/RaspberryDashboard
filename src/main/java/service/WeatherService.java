package service;

import model.WeatherData;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class WeatherService {

    private final String apiKey;
    private final String city;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public WeatherService(String apiKey, String city) {
        this.apiKey = apiKey;
        this.city   = city;
    }

    /** Fetches current weather synchronously. Call from a background thread. */
    public WeatherData fetch() throws Exception {
        String url = "https://api.openweathermap.org/data/2.5/weather"
                + "?q=" + city.replace(" ", "%20")
                + "&appid=" + apiKey
                + "&units=metric&lang=es";

        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200)
            throw new IOException("OpenWeatherMap returned HTTP " + response.statusCode());

        JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
        JsonObject main = json.getAsJsonObject("main");

        double temp    = main.get("temp").getAsDouble();
        double tempMax = main.get("temp_max").getAsDouble();
        double tempMin = main.get("temp_min").getAsDouble();
        double windMs  = json.getAsJsonObject("wind").get("speed").getAsDouble();
        String icon    = json.getAsJsonArray("weather").get(0).getAsJsonObject().get("icon").getAsString();

        return new WeatherData(temp, tempMax, tempMin, windMs * 3.6, icon);
    }
}
