// src/main/java/com/weatherify/util/WeatherToGenres.java
package com.weatherify.util;

import java.util.List;
import java.util.Map;

public class WeatherToGenres {
    private static final Map<String, List<String>> GENRES_BY_WEATHER = Map.of(
        "Clear",        List.of("pop", "rock"),
        "Cloudy",       List.of("rap", "r&b"),
        "Moderate Rain", List.of("jazz", "classical")
    );

    /**
     * Returns the list of genres corresponding to the given weather condition.
     * Defaults to ["pop","rock"] if the condition isn’t recognized.
     */
    public static List<String> getGenresForCondition(String condition) {
        return GENRES_BY_WEATHER.getOrDefault(condition, GENRES_BY_WEATHER.get("Clear"));
    }
}
