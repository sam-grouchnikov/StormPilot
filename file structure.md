# StormPilot File Structure

This project now groups app code by responsibility under `app/src/main/java/com/example/stormpilot`.

```text
com/example/stormpilot/
├── MainActivity.kt
├── StormPilotApp.kt
├── core/
│   └── AppSettings.kt
├── di/
│   └── RoutingModule.kt
├── features/
│   ├── alerts/
│   │   ├── data/
│   │   │   ├── AlertsRepository.kt
│   │   │   └── NwsAlert.kt
│   │   └── presentation/
│   │       └── AlertsViewModel.kt
│   ├── assistant/
│   │   ├── GenAIViewModel.kt
│   │   └── GenAIWeatherTools.kt
│   ├── auth/
│   │   └── ui/
│   │       ├── SignIn.kt
│   │       ├── SignUp.kt
│   │       └── WelcomePage.kt
│   ├── dashboard/
│   │   └── ui/
│   │       ├── Dashboard.kt
│   │       ├── DynamicNavBar.kt
│   │       ├── aichat/
│   │       ├── alerts/
│   │       └── weather/
│   ├── location/
│   │   └── data/
│   │       └── LocationRepository.kt
│   ├── map/
│   │   ├── data/routing/
│   │   ├── presentation/
│   │   └── ui/
│   ├── navigation/
│   │   └── NavSkeleton.kt
│   ├── settings/
│   │   └── ui/
│   │       └── Settings.kt
│   └── weather/
│       ├── data/
│       │   ├── WeatherModels.kt
│       │   └── WeatherRepository.kt
│       └── presentation/
│           └── WeatherViewModel.kt
└── ui/theme/
```

## Where To Find Things

- App entry points: `MainActivity.kt` and `StormPilotApp.kt`.
- App-wide settings and dark-mode helpers: `core/AppSettings.kt`.
- Hilt bindings: `di/RoutingModule.kt`.
- Login, signup, and welcome screens: `features/auth/ui`.
- Bottom-tab app shell: `features/navigation/NavSkeleton.kt`.
- Settings screen: `features/settings/ui/Settings.kt`.
- Dashboard shell and pager: `features/dashboard/ui`.
- Dashboard alert map and alert summary UI: `features/dashboard/ui/alerts`.
- Dashboard forecast UI: `features/dashboard/ui/weather`.
- Dashboard AI chat UI: `features/dashboard/ui/aichat`.
- AI chat state and weather tools: `features/assistant`.
- Shared device location tracking: `features/location/data`.
- NWS alert models and fetching: `features/alerts/data`.
- Alert grouping and city-name state: `features/alerts/presentation`.
- Weather API models and fetching: `features/weather/data`.
- Weather screen state: `features/weather/presentation`.
- Map screen UI: `features/map/ui`.
- Map state, search, routing progress, and alert overlays: `features/map/presentation`.
- Routing API clients, parsing, models, and formatters: `features/map/data/routing`.
- Color, typography, and Material theme code: `ui/theme`.
