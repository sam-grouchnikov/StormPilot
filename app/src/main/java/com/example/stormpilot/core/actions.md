## StormPilot AI Actions

Spring Boot should return assistant text plus a list of typed actions; Android validates each action and executes it against local map/navigation state.

Field markers are limited to `(R)` for required and `(O)` for optional.

### Response Envelope
- (R) `reply`: User-facing assistant text to show in chat, sheet, snackbar, or dialog.
- (R) `actions`: Ordered list of actions for Android to validate and execute.
- (R) `actions[].type`: One of the supported action types below.
- (O) `actions[].id`: Stable action id for logging, confirmations, and retries.
- (O) `actions[].requiresConfirmation`: Whether Android must confirm before executing the action.

### `ANSWER_ONLY`
Shows a text response without changing map, route, alert, or navigation state.

- (R) `reply`: User-facing answer text.

### `ASK_CLARIFICATION`
Asks the user for missing information before Spring Boot chooses a route, destination, alert, or weather action.

- (R) `question`: The question Android should display to the user.
- (R) `missingField`: The missing value, such as `destination`, `routePreference`, `alertLocation`, or `confirmation`.
- (O) `choices`: Suggested answers or destination choices.

### `SET_DESTINATION`
Selects a destination point on the map and lets Android prepare route state for that location.

- (R) `label`: Human-readable destination name.
- (R) `latitude`: Destination latitude.
- (R) `longitude`: Destination longitude.
- (O) `address`: Full address or formatted place label.
- (O) `placeId`: Backend or search-provider id for the selected place.
- (O) `confidence`: Backend confidence score for the location match.

### `SHOW_ALERTS_OVERLAY`
Turns on the severe weather alerts layer and optionally focuses or filters it.

- (O) `focusLocation`: Map point to center after enabling the overlay.
- (O) `focusAlertId`: Alert id to focus if Android can match it to a rendered polygon.

### `SHOW_ALERT_DETAIL`
Opens alert detail UI for a specific active weather alert.

- (R) `event`: Alert event name, such as `Severe Thunderstorm Warning`.
- (R) `latitude`: Latitude of a point inside or near the alert polygon.
- (R) `longitude`: Longitude of a point inside or near the alert polygon.
- (O) `alertId`: NWS/backend alert id.
- (O) `alert`: Full alert data if Spring Boot already has the selected alert detail.

### `PREVIEW_ROUTE`
Draws or prepares a proposed route without starting navigation.

- (R) `label`: Human-readable route name, such as `Western edge route`.
- (R) `destination`: Destination object containing `label`, `latitude`, and `longitude`.
- (O) `routeGeoJson`: Route geometry when Spring Boot returns the preview route directly.
- (O) `waypoints`: Intermediate points for a route via selected storm-edge or detour points.
- (O) `distanceMeters`: Total route distance.
- (O) `durationSeconds`: Estimated route duration.
- (O) `warningCount`: Number of active warning polygons the preview route intersects.
- (O) `warningError`: Warning message for routes that cannot fully avoid alert polygons.

### `REQUEST_CONFIRMATION`
Asks the user to approve one or more pending actions before Android executes them.

- (R) `message`: Confirmation prompt to display.
- (R) `pendingActions`: Ordered actions to execute if the user confirms.
- (O) `confirmLabel`: Confirm button label.
- (O) `cancelLabel`: Cancel button label.
- (O) `riskSummary`: Short explanation of weather, alert, or navigation risk behind the confirmation.
