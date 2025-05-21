package com.weatherify.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class WeatherResponse {
    public Location location;
    public Current current;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Location {
        public String name;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Current {
        @JsonProperty("precip_mm")
        public double precipMm;
        public int cloud;
    }
}