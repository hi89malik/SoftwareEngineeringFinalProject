// src/main/java/com/weatherify/controller/PlaylistController.java
package com.weatherify.controller;

import com.weatherify.service.PlaylistService;
import com.weatherify.service.SpotifyAuthService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.specification.User;

import java.io.IOException;
import java.util.Map;
import org.apache.hc.core5.http.ParseException;  // <<< import ParseException

@RestController
@RequestMapping("/api/v1/playlist")
public class PlaylistController {

    private final PlaylistService playlistService;
    private final SpotifyAuthService authService;

    @Autowired
    public PlaylistController(PlaylistService playlistService,
                              SpotifyAuthService authService) {
        this.playlistService = playlistService;
        this.authService     = authService;
    }

    @PostMapping("/generate")
    public ResponseEntity<?> generatePlaylist(HttpSession session) {
        // 1) Grab stored access token
        String token = (String) session.getAttribute(SpotifyAuthService.SPOTIFY_ACCESS_TOKEN_KEY);
        if (token == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
        }

        // 2) Build a SpotifyApi instance with that token
        SpotifyApi userApi = new SpotifyApi.Builder()
            .setAccessToken(token)
            .build();

        // 3) Retrieve the user's Spotify ID at runtime (now catching ParseException too)
        String userId;
        try {
            User user = userApi.getCurrentUsersProfile().build().execute();
            userId = user.getId();
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            return ResponseEntity.status(500)
                .body(Map.of("error", "Failed to fetch user profile: " + e.getMessage()));
        }

        // 4) Define some sample URIs
        String[] sampleUris = { "spotify:track:3n3Ppam7vgaVa1iaRUc9Lp" };

        // 5) Create playlist + add tracks (createPlaylistWithTracks already throws ParseException)
        try {
            String url = playlistService.createPlaylistWithTracks(
                userApi,
                userId,
                "Weatherify Test Playlist",
                "A test playlist generated on demand",
                sampleUris
            );
            return ResponseEntity.ok(Map.of("playlistUrl", url));
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            return ResponseEntity.status(500)
                .body(Map.of("error", e.getMessage()));
        }
    }
}
