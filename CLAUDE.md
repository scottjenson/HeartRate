# HeartRate WearOS App — Project Context

## What We're Building

A minimal Wear OS 3.0+ app showing the device's heart rate as a large number. Single screen, always-on with ambient mode support, foreground service with Ongoing Activity, zone-based colors, and a Wear OS Tile for quick launching.

**Status**: Builds and runs. No sensor data in emulator — test on device. Tile added but not yet verified on device.

## Architecture Decisions

- **UI Framework**: Jetpack Compose for Wear (not XML layouts)
- **Theme**: MUST use `androidx.wear.compose.material3.MaterialTheme` wrapper — provides WearOS rounded font and correct color system. Do not remove it.
- **Sensor API**: `ExerciseClient` from Health Services (direct device sensors, not phone companion). Chosen over `MeasureClient` because `MeasureClient` stops recording in ambient — the value would be ~1 min stale on wrist-raise. `ExerciseClient` keeps the sensor recording continuously through ambient, so the reading is always fresh. Cost: each session writes a workout entry to the user's health data.
- **Min SDK**: API 30 (Wear OS 3.0+), compileSdk/targetSdk = 36
- **State Management**: ViewModel + StateFlow
- **Ambient Mode**: `AmbientLifecycleObserver` handles active/low-power display states. NOTE: the always-on (ambient) display is repainted by the system ~once per minute (burn-in/power); there is no supported way to get faster (e.g. 5s) updates in ambient. Pro fitness apps accept this too. "Fake ambient" (`FLAG_KEEP_SCREEN_ON` + manual dimming to stay interactive) was tried and rejected — it's a fragile OS-fighting hack and the OS still throttled sensor delivery. Full-rate updates only happen when interactive (wrist raised).
- **Foreground Service**: Required to keep app alive; uses `foregroundServiceType="health"`
- **Ongoing Activity**: Keeps a chip on the watch face while app is running

## Project Structure

```
HeartRate/
├── CLAUDE.md (this file)
├── README.md
├── build.gradle.kts (root)
├── settings.gradle.kts
└── app/
    ├── build.gradle.kts
    └── src/main/
        ├── kotlin/org/jenson/heartrate/
        │   ├── MainActivity.kt           — Lifecycle, permissions, ambient mode, MaterialTheme
        │   ├── HeartRateViewModel.kt     — Health Services integration
        │   ├── HeartRateScreen.kt        — Compose UI (large heart rate number, zone colors)
        │   ├── HeartRateService.kt       — Foreground service + Ongoing Activity
        │   └── HeartRateTileService.kt   — Wear OS launch tile
        └── AndroidManifest.xml
```

## Key Components

### MainActivity.kt
- Requests `BODY_SENSORS` / `health.READ_HEART_RATE` permission at runtime (SDK 36+ uses new permission)
- Sets up `AmbientLifecycleObserver`; `onResume()` safety-net resets `isAmbient = false`
- Sets `LocusId("hr_session")` to link Activity to the Ongoing Activity notification
- `setShowWhenLocked(true)` + `setTurnScreenOn(true)` to display over lock screen
- Starts/stops `HeartRateService` with Activity lifecycle

### HeartRateViewModel.kt
- Uses `ExerciseClient` running an `ExerciseType.WORKOUT` session (HR only, GPS off, auto-pause off)
- Startup: `getCapabilitiesAsync()` confirms `typeToCapabilities[WORKOUT]` supports `HEART_RATE_BPM`
- **Existing-session guard** (only one exercise allowed device-wide): `getCurrentExerciseInfoAsync()` before start —
  `OWNED_EXERCISE_IN_PROGRESS` (orphan from a crash) → `endExerciseAsync()` then restart;
  `OTHER_APP_IN_PROGRESS` (e.g. a running workout app) → surface `UNAVAILABLE`, don't fight it; else start clean
- Reads HR from `ExerciseUpdate.latestMetrics.getData(HEART_RATE_BPM)` via `ExerciseUpdateCallback`
- Ambient throttle: updates at most every 10s when `isAmbient = true`, full rate otherwise
- `endExerciseAsync()` in `onCleared()` — clean teardown so no phantom workout keeps recording

### HeartRateScreen.kt
- Heart rate drawn with native `android.graphics.Paint` (not Compose `Text`) — required for
  `setFontVariationSettings()` which is unavailable in `TextStyle` at the pinned Compose version
- Font: `google-sans-flex` → fallback `google-sans`, with `'ROND' 100, 'opsz' 96, 'wght' 650`
- Zone colors animated with `animateColorAsState(tween(500))`:
  - < 95 bpm: White
  - 95–104: Cyan `#00E5FF`
  - 105–119: Green `#64DD17`
  - 120–139: Yellow `#FFD600`
  - 140+: Red `#FF1744`
- Quit button (✕) at bottom — stops service and finishes Activity

### HeartRateService.kt
- Foreground service with `FOREGROUND_SERVICE_TYPE_HEALTH` (API 34+ guard)
- Ongoing Activity requires: `LocusIdCompat` on BOTH `NotificationCompat.Builder` AND
  `OngoingActivity.Builder`, `setCategory("workout")`, and `setLocusContext()` on the Activity
- `stopForeground(true)` in `onDestroy()`

### HeartRateTileService.kt
- `TileService` subclass — launch-only tile (no live data)
- `PrimaryLayout` with "Heart Rate" label, heart icon, "Open" `CompactChip`
- `.setResponsiveContentInsetEnabled(true)` required to avoid clip on round screens
- To add on device: long-press watch face → swipe to tile carousel → tap + → find Heart Rate

## Known Issues / TODOs

- [ ] App icon (currently using default placeholder)
- [ ] Tile preview assets (square + round) needed for Play Store
- [ ] Health Services returns no data in emulator — test on real device

## Building

```bash
JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" ./gradlew assembleDebug
```

## Simulating High Heart Rate (for color zone testing)

```bash
adb -s <device-ip>:32915 shell am broadcast -a 'whs.synthetic.user.START_EXERCISE' \
  --ei exercise_options_heart_rate 150 com.google.android.wearable.healthservices
```
