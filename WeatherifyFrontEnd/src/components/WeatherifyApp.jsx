import React, { useState, useEffect } from "react";
import {
  IconSun,
  IconCloud,
  IconCloudRain,
  IconPerson,
  IconUpload,
} from "../icons";
import "../styles/weatherify.css";

const BACKEND_URL = "http://127.0.0.1:8080";

export default function WeatherifyApp() {
  const [zip, setZip] = useState("");
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [popupMessage, setPopupMessage] = useState("");
  const [showPopup, setShowPopup] = useState(false);
  const [weatherIcon, setWeatherIcon] = useState(null);
  const [cityName, setCityName] = useState("");

  const triggerPopup = (message) => {
    setPopupMessage(message);
    setShowPopup(true);
    setTimeout(() => setShowPopup(false), 3000);
  };

  const fetchAndSetWeather = async () => {
    try {
      let url;
      if (zip) {
        url = `${BACKEND_URL}/api/v1/weather/current?zip=${zip}`;
      } else {
        const position = await new Promise((resolve, reject) => {
          navigator.geolocation.getCurrentPosition(resolve, reject);
        });
        const { latitude, longitude } = position.coords;
        url = `${BACKEND_URL}/api/v1/weather/current?lat=${latitude}&lon=${longitude}`;
      }

      const response = await fetch(url, {
        method: "GET",
        credentials: "include",
      });
      if (!response.ok) {
        throw new Error(`Weather API error: ${response.status}`);
      }

      const { city, icon } = await response.json();
      setCityName(city);
      setWeatherIcon(icon);
    } catch (err) {
      console.error("Failed to fetch weather data", err);
      triggerPopup("Could not retrieve weather.");
    }
  };

  // Initial weather fetch on mount
  useEffect(() => {
    fetchAndSetWeather();
  }, []);

  // Handle login flow
  useEffect(() => {
    const params = new URLSearchParams(window.location.search);
    const loginSuccess = params.get("login_success");
    const loginError = params.get("login_error");

    const checkLoginStatus = async () => {
      setIsLoading(true);
      try {
        const res = await fetch(`${BACKEND_URL}/api/v1/auth/spotify/status`, {
          credentials: "include",
        });
        if (res.ok) {
          const data = await res.json();
          setIsLoggedIn(data.loggedIn);
        } else {
          setIsLoggedIn(false);
        }
      } catch {
        setIsLoggedIn(false);
      } finally {
        setIsLoading(false);
      }
    };

    if (loginSuccess === "true") {
      setIsLoggedIn(true);
      setIsLoading(false);
      triggerPopup("Login successful!");
      fetchAndSetWeather();

      const url = new URL(window.location.href);
      url.searchParams.delete("login_success");
      url.searchParams.set("login", "true");
      window.history.replaceState({}, "", url.href);

    } else if (loginError) {
      setIsLoggedIn(false);
      setIsLoading(false);
      triggerPopup(`Login failed: ${loginError}`);
      const url = new URL(window.location.href);
      url.searchParams.delete("login_error");
      window.history.replaceState({}, "", url.href);

    } else {
      checkLoginStatus();
    }
  }, []);

  // Keep 'login' param in sync
  useEffect(() => {
    if (!isLoading) {
      const url = new URL(window.location.href);
      if (isLoggedIn) url.searchParams.set("login", "true");
      else url.searchParams.delete("login");
      window.history.replaceState({}, "", url.href);
    }
  }, [isLoggedIn, isLoading]);

  const handleLoginClick = () =>
    (window.location.href = `${BACKEND_URL}/api/v1/auth/spotify/login`);
  const handleLogoutClick = async () => {
    try {
      const res = await fetch(`${BACKEND_URL}/api/v1/auth/spotify/logout`, {
        credentials: "include",
      });
      if (res.ok) {
        setIsLoggedIn(false);
        triggerPopup("Successfully logged out.");
      } else {
        triggerPopup("Logout failed.");
      }
    } catch {
      triggerPopup("An error occurred during logout.");
    }
  };

  const handleGenerateClick = async () => {
    if (!weatherIcon) {
      triggerPopup("Weather data not ready yet—please try again.");
      return;
    }
    triggerPopup("In progress!");

    try {
      const res = await fetch(`${BACKEND_URL}/api/v1/playlist/generate`, {
        method: "POST",
        credentials: "include",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ weather: weatherIcon }),
      });
      const data = await res.json();
      if (res.ok) {
        triggerPopup("Playlist created and music started!");
        window.open(data.playlistUrl, "_blank");
      } else {
        triggerPopup(data.message || "Failed to create playlist.");
      }
    } catch {
      triggerPopup("An error occurred while generating the playlist.");
    }
  };

  if (isLoading) {
    return (
      <div
        className="weatherify-container"
        style={{ display: "flex", justifyContent: "center", alignItems: "center" }}
      >
        <p style={{ fontSize: "24px", color: "#333" }}>Loading...</p>
      </div>
    );
  }

  return (
    <>
      {showPopup && (
        <div className="popup-overlay">
          <div className="popup-message">{popupMessage}</div>
        </div>
      )}

      <div className="weatherify-container">
        {/* ZIP code input + button, only visible when logged in */}
        {isLoggedIn && (
          <div className="zip-input-container" style={{ marginBottom: "1rem" }}>
            <input
              type="text"
              value={zip}
              placeholder="Enter ZIP code"
              onChange={(e) => setZip(e.target.value)}
              onKeyDown={(e) => {
                if (e.key === "Enter") fetchAndSetWeather();
              }}
              style={{ padding: "0.3rem", fontSize: "0.9rem", marginRight: "0.5rem" }}
            />
            <button
              onClick={fetchAndSetWeather}
              style={{ padding: "0.3rem 0.8rem", fontSize: "0.9rem" }}
            >
              Set Location
            </button>
          </div>
        )}

        {/* Weather icons */}
        <div className="weather-buttons">
          <button aria-label="Sunny" className="weather-icon-button" disabled={weatherIcon !== "sunny"}>
            <IconSun size="48" />
          </button>
          <button aria-label="Cloudy" className="weather-icon-button" disabled={weatherIcon !== "cloudy"}>
            <IconCloud size="48" />
          </button>
          <button aria-label="Rainy" className="weather-icon-button" disabled={weatherIcon !== "rainy"}>
            <IconCloudRain size="48" />
          </button>
        </div>

        {/* Main card */}
        <div className="weatherify-card">
          <h1 className="weatherify-title">Weatherify</h1>
          <p className="weatherify-subtitle">
            Generate a playlist based on weather{cityName ? ` in ${cityName}` : ""}
          </p>
          <div
            className="login-button-container"
            style={{ display: "flex", flexDirection: "column", alignItems: "center" }}
          >
            {!isLoggedIn ? (
              <button className="login-button" onClick={handleLoginClick}>
                <IconPerson size="24" /> Login
              </button>
            ) : (
              <>
                <button className="generate-button" onClick={handleGenerateClick}>
                  <IconUpload size="24" /> Generate
                </button>
                <button className="logout-button" onClick={handleLogoutClick}>
                  Logout
                </button>
              </>
            )}
          </div>
        </div>
      </div>
    </>
  );
}
