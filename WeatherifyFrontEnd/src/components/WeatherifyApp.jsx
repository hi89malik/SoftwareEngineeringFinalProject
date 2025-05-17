// src/components/WeatherifyApp.jsx
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
const WEATHER_API_KEY = "9187915b62104c3ca2d44021251505";

export default function WeatherifyApp() {
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
    const position = await new Promise((resolve, reject) => {
      navigator.geolocation.getCurrentPosition(resolve, reject);
    });
    const { latitude, longitude } = position.coords;

    const weatherRes = await fetch(
      `https://api.weatherapi.com/v1/current.json?key=${WEATHER_API_KEY}&q=${latitude},${longitude}`
    );
    const weatherData = await weatherRes.json();

    setCityName(weatherData.location.name);

    const precip = weatherData.current.precip_mm;
    const cloud = weatherData.current.cloud;

    if (precip >= 1.0) {
      setWeatherIcon("rainy");
    } else if (cloud >= 60) {
      setWeatherIcon("cloudy");
    } else if (cloud < 60 && precip === 0) {
      setWeatherIcon("sunny");
    } else {
      setWeatherIcon(null);
    }
  } catch (err) {
    console.error("Failed to fetch weather data", err);
  }
};


  useEffect(() => {
  fetchAndSetWeather();
}, []);


  useEffect(() => {
    const queryParams = new URLSearchParams(window.location.search);
    const loginSuccess = queryParams.get("login_success");
    const loginError = queryParams.get("login_error");

    const checkLoginStatus = async () => {
      setIsLoading(true);
      try {
        const response = await fetch(`${BACKEND_URL}/api/v1/auth/spotify/status`, {
          method: 'GET',
          credentials: 'include',
        });
        if (response.ok) {
          const data = await response.json();
          setIsLoggedIn(data.loggedIn);
          if (data.loggedIn) {
            console.log("User is logged in (session active). User:", data.userDisplayName);
          } else {
            console.log("User is not logged in (no active session).");
          }
        } else {
          console.error("Failed to check login status:", response.status, await response.text());
          setIsLoggedIn(false);
        }
      } catch (error) {
        console.error("Error checking login status:", error);
        setIsLoggedIn(false);
      } finally {
        setIsLoading(false);
      }
    };

    if (loginSuccess === "true") {
      console.log("Login successful callback detected from URL.");
      setIsLoggedIn(true);
      setIsLoading(false);
      triggerPopup("Login successful!");
      
      fetchAndSetWeather();
      
      if (window.history.replaceState) {
        const url = new URL(window.location.href);
        url.searchParams.delete("login_success");
        url.searchParams.delete("login_error");
        window.history.replaceState({ path: url.href }, '', url.href);
      }
    } else if (loginError) {
      console.error("Login error detected from URL:", loginError);
      setIsLoggedIn(false);
      setIsLoading(false);
      triggerPopup(`Login failed: ${loginError}`);
      if (window.history.replaceState) {
        const url = new URL(window.location.href);
        url.searchParams.delete("login_success");
        url.searchParams.delete("login_error");
        window.history.replaceState({ path: url.href }, '', url.href);
      }
    } else {
      checkLoginStatus();
    }
  }, []);

  const handleLoginClick = () => {
    console.log(`Redirecting to backend for Spotify login: ${BACKEND_URL}/api/v1/auth/spotify/login`);
    window.location.href = `${BACKEND_URL}/api/v1/auth/spotify/login`;
  };

const handleGenerateClick = async () => {
  if (!weatherIcon) {
    triggerPopup("Weather data not ready yet, please try again.");
    return;
  }

  console.log("Sending weather to backend:", weatherIcon);
  triggerPopup("In progress!");

  try {
    const response = await fetch(`${BACKEND_URL}/api/v1/playlist/generate`, {
      method: "POST",
      credentials: "include",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({ weather: weatherIcon }),
    });

    const data = await response.json();

    if (response.ok) {
      triggerPopup("Playlist created and music started!");
      console.log("Playlist URL:", data.playlistUrl);
      window.open(data.playlistUrl, "_blank");
    } else {
      triggerPopup(data.message || "Failed to create playlist.");
    }
  } catch (err) {
    console.error("Generate error:", err);
    triggerPopup("An error occurred while generating the playlist.");
  }
};


  const handleLogoutClick = async () => {
    try {
      const response = await fetch(`${BACKEND_URL}/api/v1/auth/spotify/logout`, {
        method: 'GET',
        credentials: 'include',
      });
      if (response.ok) {
        setIsLoggedIn(false);
        triggerPopup("Successfully logged out.");
      } else {
        triggerPopup("Logout failed.");
      }
    } catch (err) {
      console.error("Logout error:", err);
      triggerPopup("An error occurred during logout.");
    }
  };

  if (isLoading) {
    return (
      <div className="weatherify-container" style={{ justifyContent: 'center', alignItems: 'center', display: 'flex' }}>
        <p style={{ fontSize: '24px', color: '#333' }}>Loading...</p>
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
        <div className="weather-buttons">
          <button
            aria-label="Sunny weather"
            className="weather-icon-button"
            disabled={weatherIcon !== "sunny"}
          >
            <IconSun size="48" />
          </button>
          <button
            aria-label="Cloudy weather"
            className="weather-icon-button"
            disabled={weatherIcon !== "cloudy"}
          >
            <IconCloud size="48" />
          </button>
          <button
            aria-label="Rainy weather"
            className="weather-icon-button"
            disabled={weatherIcon !== "rainy"}
          >
            <IconCloudRain size="48" />
          </button>
        </div>

        <div className="weatherify-card">
          <h1 className="weatherify-title">Weatherify</h1>
          <p className="weatherify-subtitle">Generate a playlist based on weather{cityName ? ` in ${cityName}` : ""}</p>
          <div className="login-button-container" style={{ flexDirection: 'column', alignItems: 'center' }}>
            {!isLoggedIn ? (
              <button className="login-button" onClick={handleLoginClick}>
                <IconPerson size="24" />
                Login
              </button>
            ) : (
              <>
                <button className="generate-button" onClick={handleGenerateClick}>
                  <IconUpload size="24" />
                  Generate
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
