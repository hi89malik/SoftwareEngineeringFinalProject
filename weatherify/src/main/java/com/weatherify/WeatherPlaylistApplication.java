package com.weatherify;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
// import com.weatherify.config.SpotifyConfigProperties; // You'll create this

@SpringBootApplication
// @EnableConfigurationProperties(SpotifyConfigProperties.class) // If you
// create a properties class
public class WeatherPlaylistApplication {

    public static void main(String[] args) {
        SpringApplication.run(WeatherPlaylistApplication.class, args);
    }
}