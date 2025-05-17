package com.weatherify.controller;

import com.weatherify.service.PlaylistGenerationService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/playlist")
public class PlaylistController {

    @Autowired
    private PlaylistGenerationService playlistService;

    public static class PlaylistRequest {
        private String weather;
        public String getWeather() { return weather; }
        public void setWeather(String weather) { this.weather = weather; }
    }
    
    @PostMapping("/generate")
    public ResponseEntity<?> generatePlaylist(@RequestBody PlaylistRequest request, HttpSession session) {
    	System.out.println("Received request body: " + request);
    	String weather = request.getWeather();
        if (weather == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Weather not provided."));
        }

        try {
            String playlistUrl = playlistService.generateWeatherPlaylist(weather, session);
            return ResponseEntity.ok(Map.of("playlistUrl", playlistUrl));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("message", "Playlist generation failed: " + e.getMessage()));
        }
    }
}
