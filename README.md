
# Weatherify
A React + Spring Boot application that generates Spotify playlists based on your local weather.

---

## Getting Started (Local Setup)

### Prerequisites

To run this project locally, make sure you install the following:

### 1. Node.js & npm
- Download & install from: https://nodejs.org
- After installation, verify:
  ```bash
  node -v
  npm -v
  ```

### 2. Java 17 or higher
- Download Java JDK (Oracle): https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html
- Set JAVA_HOME environment variable correctly.
- Verify:
  ```bash
  java -version
  ```

### 3. Maven
- Download & install: https://maven.apache.org/download.cgi
- Add to your system path.
- Verify:
  ```bash
  mvn -v
  ```

### 4. Execution Policy (Windows)
To enable scripts and permissions:
- Open PowerShell as Administrator
- Run:
  ```powershell
  Set-ExecutionPolicy RemoteSigned
  ```

---

## Running the App Locally

### Frontend (React + Vite)
```bash
# Navigate to the frontend folder
cd weatherify-frontend

# Install dependencies
npm install

# Start the dev server
npm run dev
```

Your frontend will run at: `http://localhost:5173`

### Backend (Java Spring Boot)
```bash
# Navigate to the backend folder
cd weatherifyBack

# Build and run the Spring Boot app
mvn spring-boot:run
```

Your backend will run at: `http://localhost:8080`

---

## API Keys Required

### WeatherAPI Key
- Create an account at: https://www.weatherapi.com
- Add your key to the frontend file:
  ```js
  const WEATHER_API_KEY = "YOUR_API_KEY";
  ```

### Spotify Web API
- Register your app at: https://developer.spotify.com/dashboard
- Add these to `application.properties` (backend):
  ```properties
  spotify.client.id=your_client_id
  spotify.client.secret=your_client_secret
  spotify.redirect.uri=http://localhost:8080/api/v1/auth/spotify/callback
  ```

---

## API Integration Overview

### WeatherAPI (Frontend)
Used directly in React to:
- Detect user's location using browser's geolocation.
- Fetch real-time weather conditions.
- Determine which weather icon to highlight:
  - Sunny → Pop & Rock
  - Cloudy → Classical & Jazz
  - Rainy → R&B & Blues
- API Endpoint:
  ```
  https://api.weatherapi.com/v1/current.json?key=YOUR_KEY&q=latitude,longitude
  ```

### Spotify Web API (Backend)
Used in Spring Boot to:
- Authenticate the user via OAuth.
- Store session-based access and refresh tokens.
- Later (optional): Generate playlists based on weather genres.
- Key endpoints:
  - `/api/v1/auth/spotify/login`
  - `/api/v1/auth/spotify/callback`
  - `/api/v1/auth/spotify/status`
  - `/api/v1/auth/spotify/logout`

---

## Frontend Functionality Explained

### useEffect – Weather Fetching
On component mount:
- Grabs geolocation.
- Calls WeatherAPI.
- Based on `precip_mm` and `cloud` values:
  - Sets `weatherIcon` to one of: `"sunny"`, `"cloudy"`, `"rainy"`.

### useEffect – Spotify Login Check
- Checks for login success/error in URL.
- If session is already active, hits `/status` from the backend to confirm.

### Button Behavior
- Login: Redirects to backend login, starts Spotify OAuth.
- Generate: Placeholder for future playlist generation logic.
- Logout: Clears session on backend, resets frontend state.

---

## UI Features

- Large centered weather icon buttons (sun, cloud, rain)
- Disabled buttons based on current weather condition
- Subtitle displays current city
- Login/Logout state-aware buttons
- In-browser popups for:
  - Login success/failure
  - Logout
  - Generate status

---

## Project Structure

```
weatherify/
├── weatherify-frontend/
│   ├── src/
│   │   ├── components/
│   │   │   └── WeatherifyApp.jsx
│   │   ├── styles/
│   │   │   └── weatherify.css
│   └── index.html
│
├── weatherifyBack/
│   ├── src/main/java/com/weatherify/
│   │   ├── controller/
│   │   ├── service/
│   │   └── WeatherPlaylistApplication.java
│   └── resources/
│       └── application.properties
```

---

## Notes

- Currently, the playlist "Generate" button is a placeholder and shows a popup.
- The app handles state with React hooks and user auth via backend sessions.
- Spotify playlist creation can be added once OAuth and tokens are fully set up.
