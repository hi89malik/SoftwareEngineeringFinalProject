package com.weatherify.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import se.michaelthelin.spotify.SpotifyApi;

import java.net.URI;

@Configuration
public class AppConfig {

    @Value("${spotify.client.id}")
    private String spotifyClientId;

    @Value("${spotify.client.secret}")
    private String spotifyClientSecret;

    @Value("${spotify.redirect.uri}")
    private String spotifyRedirectUri;

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public SpotifyApi spotifyApi() {
        // basic setup for client credentials flow or when you have an access
        // token
        // For user-specific actions, need user authorization and set the access
        // token.
        return new SpotifyApi.Builder()
                .setClientId(spotifyClientId)
                .setClientSecret(spotifyClientSecret)
                .setRedirectUri(URI.create(spotifyRedirectUri))
                .build();
    }
}