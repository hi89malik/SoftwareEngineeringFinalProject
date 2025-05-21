package com.weatherify.controller;

import com.weatherify.service.SpotifyAuthService;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletResponse;
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
    private static final String SPOTIFY_STATE_KEY = "spotify_auth_state";
    private static final String SCOPES =
        "playlist-read-private playlist-modify-public playlist-modify-private user-read-private user-read-email";

    private static final String FRONTEND_BASE = "http://localhost:5173";

    private final SpotifyApi spotifyApi;
    private final SpotifyAuthService spotifyAuthService;

    @Autowired
    public AuthController(SpotifyApi spotifyApi, SpotifyAuthService spotifyAuthService) {
        this.spotifyApi = spotifyApi;
        this.spotifyAuthService = spotifyAuthService;
    }

    @GetMapping("/login")
    public void spotifyLogin(HttpSession session, HttpServletResponse response) throws IOException {
        spotifyAuthService.clearTokens(session);
        String state = generateState();
        session.setAttribute(SPOTIFY_STATE_KEY, state);

        AuthorizationCodeUriRequest uriRequest = spotifyApi.authorizationCodeUri()
            .scope(SCOPES)
            .state(state)
            .show_dialog(true)
            .build();

        URI uri = uriRequest.execute();
        logger.info("Redirecting to Spotify: {}", uri);
        response.sendRedirect(uri.toString());
    }

    @GetMapping("/callback")
    public void spotifyCallback(
        @RequestParam("code") String code,
        @RequestParam("state") String returnedState,
        HttpSession session,
        HttpServletResponse response
    ) throws IOException {
        String storedState = (String) session.getAttribute(SPOTIFY_STATE_KEY);
        session.removeAttribute(SPOTIFY_STATE_KEY);

        if (storedState == null || !storedState.equals(returnedState)) {
            logger.error("State mismatch. Stored: {}, Returned: {}", storedState, returnedState);
            response.sendRedirect(FRONTEND_BASE + "/?login_error=state_mismatch");
            return;
        }

        boolean success = spotifyAuthService.exchangeCodeForTokens(code, session);
        if (success) {
            logger.info("Login successful for session {}", session.getId());
            response.sendRedirect(FRONTEND_BASE + "/?login_success=true");
        } else {
            logger.error("Token exchange failed for session {}", session.getId());
            response.sendRedirect(FRONTEND_BASE + "/?login_error=token_exchange_failed");
        }
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String,Object>> getStatus(HttpSession session) {
        boolean loggedIn = spotifyAuthService.isUserLoggedIn(session);
        return ResponseEntity.ok(Map.of(
            "loggedIn", loggedIn
        ));
    }

    @GetMapping("/logout")
    public ResponseEntity<Map<String,Object>> logout(HttpSession session) {
        spotifyAuthService.clearTokens(session);
        logger.info("Cleared Spotify session for {}", session.getId());
        return ResponseEntity.ok(Map.of(
            "loggedOut", true
        ));
    }

    private String generateState() {
        SecureRandom rnd = new SecureRandom();
        byte[] bytes = new byte[16];
        rnd.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
