package com.weatherify.service;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.model_objects.specification.Playlist;
import se.michaelthelin.spotify.model_objects.specification.Track;
import se.michaelthelin.spotify.requests.data.browse.GetRecommendationsRequest;
import se.michaelthelin.spotify.requests.data.playlists.AddItemsToPlaylistRequest;
import se.michaelthelin.spotify.requests.data.playlists.CreatePlaylistRequest;
import se.michaelthelin.spotify.requests.data.player.StartResumeUsersPlaybackRequest;

import java.util.*;

@Service
public class PlaylistService {

    private final SpotifyApi baseSpotifyApi;

    public PlaylistService(SpotifyApi spotifyApi) {
        this.baseSpotifyApi = spotifyApi;
    }

    private static final Map<String, List<String>> WEATHER_GENRE_MAP = Map.of(
            "sunny", List.of("pop", "happy", "dance"),
            "cloudy", List.of("indie", "chill", "acoustic"),
            "rainy", List.of("piano", "ambient", "sad")
    );

    public String generateWeatherPlaylist(String weather, HttpSession session) throws Exception {
        String accessToken = (String) session.getAttribute(SpotifyAuthService.SPOTIFY_ACCESS_TOKEN_KEY);
        String userId = (String) session.getAttribute(SpotifyAuthService.SPOTIFY_USER_ID_KEY);

        if (accessToken == null || userId == null) {
            throw new IllegalStateException("User is not logged in.");
        }

        SpotifyApi userApi = new SpotifyApi.Builder()
                .setAccessToken(accessToken)
                .build();

        List<String> genres = WEATHER_GENRE_MAP.getOrDefault(weather.toLowerCase(), List.of("pop"));

        // Get song recommendations
        GetRecommendationsRequest recommendationsRequest = userApi.getRecommendations()
                .limit(10)
                .seed_genres(genres.toArray(new String[0]))
                .build();

        Track[] tracks = recommendationsRequest.execute().getTracks();
        String[] trackUris = Arrays.stream(tracks).map(Track::getUri).toArray(String[]::new);

        // Create playlist
        String playlistName = "Weatherify - " + weather.substring(0, 1).toUpperCase() + weather.substring(1);
        CreatePlaylistRequest createPlaylistRequest = userApi.createPlaylist(userId, playlistName)
                .public_(true)
                .description("Generated based on the weather")
                .build();

        Playlist playlist = createPlaylistRequest.execute();

        // Add songs to playlist
        AddItemsToPlaylistRequest addItemsRequest = userApi.addItemsToPlaylist(playlist.getId(), trackUris).build();
        addItemsRequest.execute();

        // Try to start playback (requires active device)
        try {
            StartResumeUsersPlaybackRequest playRequest = userApi
                    .startResumeUsersPlayback()
                    .uris(trackUris)
                    .build();
            playRequest.execute();
        } catch (Exception e) {
            // Playback might fail if no active device
            System.err.println("Could not start playback: " + e.getMessage());
        }

        return playlist.getExternalUrls().get("spotify");
    }
}
