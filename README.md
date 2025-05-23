Here is the revised `README.md` content formatted as plain text, without emojis or decorative characters:

---

# Weatherify

A full-stack application that generates Spotify playlists based on current weather conditions using React (frontend) and Spring Boot (backend).

---

## Overview

This project is composed of two main components:

* **weatherifyFront**: The frontend built with React and Vite.
* **weatherifyBack**: The backend built using Java Spring Boot.

---

## Project Structure

```
WeatherifyFinalProjectSubmission/
├── weatherifyFront/
│   ├── src/
│   │   ├── components/         # Contains WeatherifyApp UI logic
│   │   ├── icons/              # Icon components (e.g., sun, rain, cloud)
│   │   ├── primitives/         # UI primitives like Button, IconButton
│   │   ├── styles/             # Custom CSS for layout
│   │   └── index.jsx           # Entry point
│   └── public/                 # Public assets
│
├── weatherifyBack/
│   ├── src/main/java/com/weatherify/
│   │   ├── config/             # Spotify API config and CORS setup
│   │   ├── controller/         # REST controllers (auth, weather, playlist)
│   │   ├── service/            # Service logic for Spotify and weather APIs
│   │   ├── util/               # Weather-to-genre mapping logic
│   │   └── WeatherPlaylistApplication.java # Main application entry
│   └── resources/
│       ├── application.properties  # Contains Spotify/Weather API keys
│       └── genres_dict.json        # Mapping from weather to genre list
```

---

## Prerequisites

Install the following tools before running the app:

* Node.js and npm: [https://nodejs.org](https://nodejs.org)
* Java 17 or higher: [https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)
* Maven: [https://maven.apache.org](https://maven.apache.org)

Verify installation with:

```bash
node -v
npm -v
java -version
mvn -v
```

---

## Running the Application

### Backend (Spring Boot)

```bash
cd weatherifyBack
mvn spring-boot:run
```

The backend will run at: `http://localhost:8080`

### Frontend (React + Vite)

```bash
cd weatherifyFront
npm install
npm run dev
```

The frontend will run at: `http://localhost:5173`

---

## API Keys Setup

### File: `weatherifyBack/src/main/resources/application.properties`

```properties
spotify.client.id=your_client_id
spotify.client.secret=your_client_secret
spotify.redirect.uri=http://localhost:8080/api/v1/auth/spotify/callback
weatherapi.key=your_weatherapi_key
```

### File: `weatherifyFront/src/components/WeatherifyApp.jsx`

Add your WeatherAPI key in this file if needed for frontend weather fetches.

---

## Backend API Endpoints

* `GET /api/v1/auth/spotify/login` – Initiates Spotify login
* `GET /api/v1/auth/spotify/callback` – Handles OAuth callback
* `GET /api/v1/auth/spotify/status` – Checks user login status
* `POST /api/v1/playlist/generate` – Generates playlist (in development)
* `GET /api/v1/weather` – Returns current weather info

---

## Frontend Features

* Weather-based visual UI (sun, cloud, rain icons)
* Spotify login/logout functionality
* Location-based weather fetching
* Popups for login success/failure
* "Generate Playlist" button (currently non-functional or under development)

---

## Notes

* The "Generate Playlist" feature is currently a placeholder.
* Session-based authentication is used for Spotify login via backend.
* Weather information is used to suggest genres and potentially build playlists.
