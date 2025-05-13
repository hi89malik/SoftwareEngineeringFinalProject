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
import se.michaelthelin.spotify.requests.authorization.authorization_code.AuthorizationCodeRefreshRequest;

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
        try {
            AuthorizationCodeRequest authorizationCodeRequest = spotifyApi.authorizationCode(code).build();
            AuthorizationCodeCredentials credentials = authorizationCodeRequest.execute();

            session.setAttribute(SPOTIFY_ACCESS_TOKEN_KEY, credentials.getAccessToken());
            session.setAttribute(SPOTIFY_REFRESH_TOKEN_KEY, credentials.getRefreshToken());
            long expiresInMillis = TimeUnit.SECONDS.toMillis(credentials.getExpiresIn());
            session.setAttribute(SPOTIFY_TOKEN_EXPIRY_TIME_KEY, System.currentTimeMillis() + expiresInMillis);

            // Fetch and store user ID
            SpotifyApi userSpecificApi = new SpotifyApi.Builder()
                    .setAccessToken(credentials.getAccessToken())
                    .build();
            User user = userSpecificApi.getCurrentUsersProfile().build().execute();
            session.setAttribute(SPOTIFY_USER_ID_KEY, user.getId());

            logger.info("Spotify tokens obtained and stored for user: {}", user.getId());
            return true;

        } catch (IOException | SpotifyWebApiException | ParseException e) {
            logger.error("Error exchanging Spotify authorization code for tokens: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Retrieves a valid access token for the current session, refreshing if
     * necessary.
     * 
     * @param session The HTTP session.
     * @return The access token, or null if unable to obtain/refresh.
     */
    public String getValidAccessToken(HttpSession session) {
        String accessToken = (String) session.getAttribute(SPOTIFY_ACCESS_TOKEN_KEY);
        Long expiryTime = (Long) session.getAttribute(SPOTIFY_TOKEN_EXPIRY_TIME_KEY);
        String refreshToken = (String) session.getAttribute(SPOTIFY_REFRESH_TOKEN_KEY);

        if (accessToken == null || expiryTime == null || refreshToken == null) {
            logger.warn("No Spotify tokens found in session. User needs to login.");
            return null;
        }

        // Check if token is expired or close to expiring (e.g., within 5 minutes)
        if (System.currentTimeMillis() >= (expiryTime - TimeUnit.MINUTES.toMillis(5))) {
            logger.info("Spotify access token expired or nearing expiration. Refreshing...");
            return refreshAccessToken(refreshToken, session);
        }
        return accessToken;
    }

    /**
     * Refreshes the access token using the refresh token.
     * 
     * @param refreshToken The refresh token.
     * @param session      The HTTP session to store the new tokens.
     * @return The new access token, or null on failure.
     */
    private String refreshAccessToken(String refreshToken, HttpSession session) {
        // Use the global spotifyApi instance which has client ID and secret set
        AuthorizationCodeRefreshRequest refreshRequest = spotifyApi.authorizationCodeRefresh()
                .refresh_token(refreshToken)
                .build();
        try {
            AuthorizationCodeCredentials credentials = refreshRequest.execute();
            String newAccessToken = credentials.getAccessToken();
            long newExpiresInMillis = TimeUnit.SECONDS.toMillis(credentials.getExpiresIn());

            session.setAttribute(SPOTIFY_ACCESS_TOKEN_KEY, newAccessToken);
            session.setAttribute(SPOTIFY_TOKEN_EXPIRY_TIME_KEY, System.currentTimeMillis() + newExpiresInMillis);

            // Ensure that Spotify gives a new access token during a refresh (when access token expires)
            if (credentials.getRefreshToken() != null && !credentials.getRefreshToken().isEmpty()) {
                session.setAttribute(SPOTIFY_REFRESH_TOKEN_KEY, credentials.getRefreshToken());
                logger.info("Spotify refresh token was also updated.");
            }

            logger.info("Spotify access token refreshed successfully for session ID: {}", session.getId());
            return newAccessToken;
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            logger.error("Could not refresh Spotify access token: {}", e.getMessage(), e);
            // If refresh fails, clear out the tokens to force re-login
            clearTokens(session);
            return null;
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