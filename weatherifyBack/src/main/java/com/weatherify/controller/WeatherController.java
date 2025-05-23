package com.weatherify.controller;

import com.weatherify.service.WeatherService;
import com.weatherify.service.WeatherResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;  // ensure RequestMapping, GetMapping, RequestParam, CrossOrigin imported

@RestController
@RequestMapping("/api/v1/weather")
@CrossOrigin(origins = "${cors.allowed.origins}", allowCredentials = "true")
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

        // determine icon
        String icon = precip >= 1.0
            ? "rainy"
            : cloud >= 60
                ? "cloudy"
                : "sunny";

        // map to descriptive condition
        String condition;
        switch (icon) {
            case "rainy":
                condition = "Moderate Rain";
                break;
            case "cloudy":
                condition = "Cloudy";
                break;
            default:
                condition = "Clear";
        }

        return new WeatherIcon(icon, resp.location.name, condition);
    }

    public static class WeatherIcon {
        public String icon;
        public String city;
        public String condition;

        public WeatherIcon(String icon, String city, String condition) {
            this.icon = icon;
            this.city = city;
            this.condition = condition;
        }
    }
}
