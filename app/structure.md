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
│   ├── auth/
│   │   └── ui/
│   │       ├── SignIn.kt
│   │       ├── SignUp.kt
│   │       ├── WelcomePage.kt
│   │       └── components/
│   ├── common/
│   │   └── ui/
│   ├── dashboard/
│   │   └── ui/
│   │       ├── Dashboard.kt
│   │       ├── DynamicNavBar.kt
│   │       ├── aichat/
│   │       ├── alerts/
│   │       └── weather/
│   ├── map/
│   │   └── ui/
│   │       ├── navigation/
│   │       ├── radar/
│   │       └── search/
│   ├── navigation/
│   │   └── ui/
│   │       ├── NavSkeleton.kt
│   │       └── components/
│   ├── settings/
│   │   └── ui/
│   │       └── Settings.kt
│   ├── shared/
│   │   ├── data/
│   │   │   ├── alerts/
│   │   │   ├── assistant/
│   │   │   ├── location/
│   │   │   ├── routing/
│   │   │   ├── search/
│   │   │   └── weather/
│   │   └── viewmodels/
└── ui/theme/
```

## Where To Find Things

- App entry points: `MainActivity.kt` and `StormPilotApp.kt`.
- App-wide settings and dark-mode helpers: `core/AppSettings.kt`.
- Hilt bindings: `di/RoutingModule.kt`.
- Shared UI widgets: `features/common/ui`.
- Login, signup, and welcome screens: `features/auth/ui`.
- Bottom-tab app shell: `features/navigation/NavSkeleton.kt`.
- Settings screen: `features/settings/ui/Settings.kt`.
- Dashboard shell and pager: `features/dashboard/ui`.
- Dashboard alert map and alert summary UI: `features/dashboard/ui/alerts`.
- Dashboard forecast UI: `features/dashboard/ui/weather`.
- Dashboard AI chat UI: `features/dashboard/ui/aichat`.
- Centralized ViewModels and UI state: `features/shared/viewmodels`.
- Centralized data fetching, models, and service helpers: `features/shared/data`.
- AI chat weather tools: `features/shared/data/assistant`.
- Shared device location tracking: `features/shared/data/location`.
- NWS alert models and fetching: `features/shared/data/alerts`.
- Weather API models and fetching: `features/shared/data/weather`.
- Map screen UI: `features/map/ui`, with navigation widgets in `features/map/ui/navigation` and search chrome in `features/map/ui/search`.
- Map state, search, routing progress, and alert overlays: `features/shared/viewmodels`.
- Routing API clients, parsing, models, and formatters: `features/shared/data/routing`.
- Search geocoding and drive-metric enrichment: `features/shared/data/search`.
- App shell navigation: `features/navigation/ui`.
- Color, typography, and Material theme code: `ui/theme`.
