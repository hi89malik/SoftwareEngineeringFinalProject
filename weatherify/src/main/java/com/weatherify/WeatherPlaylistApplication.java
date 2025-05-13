package com.weatherify;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
public class WeatherPlaylistApplication {

    public static void main(String[] args) {
        SpringApplication.run(WeatherPlaylistApplication.class, args);
    }
}