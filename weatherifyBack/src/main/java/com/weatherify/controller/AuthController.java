package com.weatherify.controller;

import com.weatherify.service.SpotifyAuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.requests.authorization.authorization_code.AuthorizationCodeUriRequest;

import java.io.IOException;
import java.net.URI;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;

@Controller
@RequestMapping("/api/v1/auth/spotify")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    // Key used to store the anti-CSRF state in session
    private static final String SPOTIFY_STATE_KEY = "spotify_auth_state";

    // Scopes determine what Spotify data your app can access
    private static final String SCOPES = "playlist-read-private playlist-modify-public playlist-modify-private user-read-private user-read-email";

    private final SpotifyApi spotifyApi;
    private final SpotifyAuthService spotifyAuthService;

    @Autowired
    public AuthController(SpotifyApi spotifyApi, SpotifyAuthService spotifyAuthService) {
        this.spotifyApi = spotifyApi;
        this.spotifyAuthService = spotifyAuthService;
    }

    // Redirects the user to Spotify's authorization page
    @GetMapping("/login")
    public void spotifyLogin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String state = generateState(); // CSRF protection
        request.getSession().setAttribute(SPOTIFY_STATE_KEY, state);

        // Build the Spotify authorization URI
        AuthorizationCodeUriRequest authorizationCodeUriRequest = spotifyApi.authorizationCodeUri()
                .scope(SCOPES)
                .state(state)
                .show_dialog(true) // Always show login prompt
                .build();

        URI uri = authorizationCodeUriRequest.execute();
        logger.info("Redirecting to Spotify for authorization: {}", uri);
        response.sendRedirect(uri.toString());
    }

    // Spotify redirects here after the user logs in
    @GetMapping("/callback")
    public ResponseEntity<?> spotifyCallback(
            @RequestParam("code") String code,
            @RequestParam("state") String returnedState,
            HttpSession session,
            HttpServletResponse response) throws IOException {

        logger.info("Received callback from Spotify with code: {} and state: {}", code, returnedState);

        // Validate the state to prevent CSRF attacks
        String storedState = (String) session.getAttribute(SPOTIFY_STATE_KEY);
        if (storedState == null || !storedState.equals(returnedState)) {
            logger.error("State mismatch. Stored: {}, Returned: {}", storedState, returnedState);
            response.sendRedirect("http://localhost:5173/?login_error=state_mismatch");
            return null;
        }

        // Clean up stored state
        session.removeAttribute(SPOTIFY_STATE_KEY);

        // Try to exchange the authorization code for access and refresh tokens
        try {
            boolean success = spotifyAuthService.exchangeCodeForTokens(code, session);
            if (success) {
                logger.info("Successfully obtained and stored Spotify tokens for session ID: {}", session.getId());
                response.sendRedirect("http://localhost:5173/?login_success=true");
            } else {
                response.sendRedirect("http://localhost:5173/?login_error=token_failure");
            }
        } catch (Exception e) {
            logger.error("Error during Spotify token exchange: {}", e.getMessage(), e);
            response.sendRedirect("http://localhost:5173/?login_error=exception");
        }
        return null;
    }

    // Frontend uses this endpoint to check if the user is logged in
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getLoginStatus(HttpSession session) {
        boolean isLoggedIn = spotifyAuthService.isUserLoggedIn(session);
        if (isLoggedIn) {
            String displayName = (String) session.getAttribute(SpotifyAuthService.SPOTIFY_USER_DISPLAY_NAME_KEY);
            String userId = (String) session.getAttribute(SpotifyAuthService.SPOTIFY_USER_ID_KEY);
            return ResponseEntity.ok(Map.of(
                    "loggedIn", true,
                    "message", "Successfully logged into Spotify!",
                    "userDisplayName", displayName != null ? displayName : "N/A",
                    "userId", userId != null ? userId : "N/A",
                    "sessionId", session.getId()));
        } else {
            return ResponseEntity.ok(Map.of(
                    "loggedIn", false,
                    "message", "Not logged into Spotify.",
                    "sessionId", session.getId()));
        }
    }

    // Logs the user out by clearing tokens from the session
    @GetMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        spotifyAuthService.clearTokens(session);
        return ResponseEntity.ok("User logged out");
    }

    // Helper to generate a secure random state string
    private String generateState() {
        SecureRandom random = new SecureRandom();
        byte[] stateBytes = new byte[16];
        random.nextBytes(stateBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(stateBytes);
    }
}
