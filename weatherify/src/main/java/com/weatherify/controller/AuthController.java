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

@Controller
@RequestMapping("/api/v1/auth/spotify")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private static final String SPOTIFY_STATE_KEY = "spotify_auth_state";
    // scopes:
    private static final String SCOPES = "playlist-read-private playlist-modify-public playlist-modify-private user-read-private user-read-email";

    private final SpotifyApi spotifyApi;
    private final SpotifyAuthService spotifyAuthService;

    @Autowired
    public AuthController(SpotifyApi spotifyApi, SpotifyAuthService spotifyAuthService) {
        this.spotifyApi = spotifyApi;
        this.spotifyAuthService = spotifyAuthService;
    }

    @GetMapping("/login")
    public void spotifyLogin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String state = generateState();
        request.getSession().setAttribute(SPOTIFY_STATE_KEY, state);

        AuthorizationCodeUriRequest authorizationCodeUriRequest = spotifyApi.authorizationCodeUri()
                .scope(SCOPES)
                .state(state)
                .show_dialog(true) // forces the user to re-approve if they've already done so
                .build();

        URI uri = authorizationCodeUriRequest.execute();
        logger.info("Redirecting to Spotify for authorization: {}", uri.toString());
        response.sendRedirect(uri.toString());
    }

    @GetMapping("/callback")
    public ResponseEntity<?> spotifyCallback(@RequestParam("code") String code,
            @RequestParam("state") String returnedState,
            HttpSession session) {
        logger.info("Received callback from Spotify with code: {} and state: {}", code, returnedState);
        String storedState = (String) session.getAttribute(SPOTIFY_STATE_KEY);

        if (storedState == null || !storedState.equals(returnedState)) {
            logger.error("State mismatch. Stored: {}, Returned: {}. Possible CSRF attack.", storedState, returnedState);
            return ResponseEntity.badRequest().body("Error: State mismatch. Please try logging in again.");
        }
        session.removeAttribute(SPOTIFY_STATE_KEY); // Clean up state

        try {
            boolean success = spotifyAuthService.exchangeCodeForTokens(code, session);
            if (success) {
                logger.info("Successfully obtained and stored Spotify tokens for session ID: {}", session.getId());
                // TODO: Redirect to a frontend page indicating success
                return ResponseEntity
                        .ok("Login successful! You can now use the playlist generation features. (Session ID: "
                                + session.getId() + ")");
            } else {
                return ResponseEntity.status(500).body("Error: Could not obtain Spotify tokens.");
            }
        } catch (Exception e) {
            logger.error("Error during Spotify token exchange: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Error: An unexpected error occurred during Spotify login.");
        }
    }

    private String generateState() {
        SecureRandom random = new SecureRandom();
        byte[] stateBytes = new byte[16];
        random.nextBytes(stateBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(stateBytes);
    }
}