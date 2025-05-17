package com.weatherify.controller;

import com.weatherify.service.PlaylistService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/playlist")
public class PlaylistController {

    private final PlaylistService playlistService;

    @Autowired
    public PlaylistController(PlaylistService playlistService) {
        this.playlistService = playlistService;
    }

    @PostMapping("/generate")
    public ResponseEntity<?> generatePlaylist(@RequestBody Map<String, String> requestBody, HttpSession session) {
        String weather = requestBody.get("weather");

        try {
            String playlistUrl = playlistService.generateWeatherPlaylist(weather, session);
            return ResponseEntity.ok(Map.of("playlistUrl", playlistUrl));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
