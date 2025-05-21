package com.weatherify.controller;

import com.weatherify.service.WeatherService;
import com.weatherify.service.WeatherResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;  // make sure this is imported
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/weather")
public class WeatherController {

    private final WeatherService weatherService;

    @Autowired
    public WeatherController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    @GetMapping("/current")
    public WeatherIcon getIcon(
        @RequestParam(required = false) String zip,
        @RequestParam(required = false) Double lat,
        @RequestParam(required = false) Double lon
    ) {
        WeatherResponse resp;
        if (zip != null && !zip.isBlank()) {
            resp = weatherService.fetchWeatherByQuery(zip);
        } else if (lat != null && lon != null) {
            resp = weatherService.fetchWeatherByCoords(lat, lon);
        } else {
            throw new IllegalArgumentException("Must supply either zip or lat+lon");
        }

        double precip = resp.current.precipMm;
        int cloud   = resp.current.cloud;
        String icon = precip >= 1.0 ? "rainy"
                    : cloud >= 60    ? "cloudy"
                    :                 "sunny";

        return new WeatherIcon(icon, resp.location.name);
    }

    public static class WeatherIcon {
        public String icon;
        public String city;
        public WeatherIcon(String icon, String city) {
            this.icon = icon;
            this.city = city;
        }
    }
}
