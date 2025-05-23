// src/main/java/com/weatherify/service/PlaylistService.java
package com.weatherify.service;

import org.springframework.stereotype.Service;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.specification.Playlist;
import se.michaelthelin.spotify.requests.data.playlists.AddItemsToPlaylistRequest;
import se.michaelthelin.spotify.requests.data.playlists.CreatePlaylistRequest;

import java.io.IOException;
import org.apache.hc.core5.http.ParseException;

@Service
public class PlaylistService {

    /**
     * Creates a new private playlist for the given SpotifyApi/user,
     * then adds the provided track URIs to it.
     *
     * @param api   a SpotifyApi instance already authenticated with a user token
     * @param userId the Spotify user ID
     * @param name   the name of the new playlist
     * @param desc   the playlist description
     * @param uris   an array of Spotify track URIs to add
     * @return the external Spotify URL of the created playlist
     */
    public String createPlaylistWithTracks(
            SpotifyApi api,
            String userId,
            String name,
            String desc,
            String[] uris
    ) throws IOException, SpotifyWebApiException, ParseException {

        // 1) Create the playlist
        CreatePlaylistRequest createRequest = api
            .createPlaylist(userId, name)
            .public_(false)
            .description(desc)
            .build();
        Playlist playlist = createRequest.execute();

        // 2) Add items to the playlist
        AddItemsToPlaylistRequest addRequest = api
            .addItemsToPlaylist(playlist.getId(), uris)
            .build();
        addRequest.execute();

        // 3) Return the external Spotify URL
        return playlist.getExternalUrls().get("spotify");
    }
}
