package com.weatherify.service;

import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import se.michaelthelin.spotify.model_objects.specification.User;
import se.michaelthelin.spotify.requests.authorization.authorization_code.AuthorizationCodeRequest;

import org.apache.hc.core5.http.ParseException;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Service
public class SpotifyAuthService {

    private static final Logger logger = LoggerFactory.getLogger(SpotifyAuthService.class);

    public static final String SPOTIFY_ACCESS_TOKEN_KEY = "spotify_access_token";
    public static final String SPOTIFY_REFRESH_TOKEN_KEY = "spotify_refresh_token";
    public static final String SPOTIFY_TOKEN_EXPIRY_TIME_KEY = "spotify_token_expiry_time";
    public static final String SPOTIFY_USER_ID_KEY = "spotify_user_id";
    public static final String SPOTIFY_USER_DISPLAY_NAME_KEY = "spotify_user_display_name";

    private final SpotifyApi spotifyApi;

    @Autowired
    public SpotifyAuthService(SpotifyApi spotifyApi) {
        this.spotifyApi = spotifyApi;
    }

    /**
     * Exchanges the authorization code for access and refresh tokens and stores
     * them in the session.
     * 
     * @param code    The authorization code from Spotify.
     * @param session The HTTP session.
     * @return true if successful, false otherwise.
     */
    public boolean exchangeCodeForTokens(String code, HttpSession session) {
        String shortCode = code.substring(0, Math.min(code.length(), 10)) + "..."; // For logging

        try {
            logger.info("Session ID [{}]: Attempting to exchange code [{}] for tokens.", session.getId(), shortCode);
            AuthorizationCodeRequest authorizationCodeRequest = spotifyApi.authorizationCode(code).build();

            logger.debug("Session ID [{}]: Making token request to Spotify with code [{}].", session.getId(),
                    shortCode);
            AuthorizationCodeCredentials credentials = authorizationCodeRequest.execute();

            String shortAccessToken = credentials.getAccessToken().substring(0,
                    Math.min(credentials.getAccessToken().length(), 10)) + "...";
            logger.info("Session ID [{}]: Successfully exchanged code [{}] for tokens. Access token starts with: [{}].",
                    session.getId(), shortCode, shortAccessToken);

            session.setAttribute(SPOTIFY_ACCESS_TOKEN_KEY, credentials.getAccessToken());
            session.setAttribute(SPOTIFY_REFRESH_TOKEN_KEY, credentials.getRefreshToken());
            long expiresInMillis = TimeUnit.SECONDS.toMillis(credentials.getExpiresIn());
            session.setAttribute(SPOTIFY_TOKEN_EXPIRY_TIME_KEY, System.currentTimeMillis() + expiresInMillis);

            // // Fetch and store user ID
            // SpotifyApi userSpecificApi = new SpotifyApi.Builder()
            // .setAccessToken(credentials.getAccessToken())
            // .build();
            // User user = userSpecificApi.getCurrentUsersProfile().build().execute();
            // session.setAttribute(SPOTIFY_USER_ID_KEY, user.getId());
            // session.setAttribute(SPOTIFY_USER_DISPLAY_NAME_KEY, user.getDisplayName());

            // logger.info("Spotify tokens obtained and stored for user: {}", user.getId());
            return true;

        } catch (IOException | SpotifyWebApiException | ParseException e) {
            logger.error("Error exchanging Spotify authorization code for tokens: {}", e.getMessage(), e);
            return false;
        }
    }

    public boolean isUserLoggedIn(HttpSession session) {
        return session.getAttribute(SPOTIFY_ACCESS_TOKEN_KEY) != null &&
                session.getAttribute(SPOTIFY_REFRESH_TOKEN_KEY) != null &&
                session.getAttribute(SPOTIFY_USER_ID_KEY) != null;
    }

    public void clearTokens(HttpSession session) {
        session.removeAttribute(SPOTIFY_ACCESS_TOKEN_KEY);
        session.removeAttribute(SPOTIFY_REFRESH_TOKEN_KEY);
        session.removeAttribute(SPOTIFY_TOKEN_EXPIRY_TIME_KEY);
        session.removeAttribute(SPOTIFY_USER_ID_KEY);
        logger.info("Cleared Spotify tokens for session ID: {}", session.getId());
    }
}