package com.weatherify.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class WeatherService {

    @Value("${weather.api.key}")
    private String apiKey;

    private final RestTemplate rest = new RestTemplate();

    // Core fetch by arbitrary query (zip code or “lat,lon”)
    private WeatherResponse fetchWeather(String q) {
        String url = UriComponentsBuilder
            .fromHttpUrl("https://api.weatherapi.com/v1/current.json")
            .queryParam("key", apiKey)
            .queryParam("q", q)
            .toUriString();

        return rest.getForObject(url, WeatherResponse.class);
    }

    // By coordinates
    public WeatherResponse fetchWeatherByCoords(double lat, double lon) {
        return fetchWeather(lat + "," + lon);
    }

    // By ZIP code (or city name, etc.)
    public WeatherResponse fetchWeatherByQuery(String zip) {
        return fetchWeather(zip);
    }
}
