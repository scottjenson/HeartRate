# HeartRate WearOS App — Project Context

## What We're Building

A minimal Wear OS 3.0+ app showing the device's heart rate as a large number. Single screen, always-on with ambient mode support.

**Status**: Builds and runs on emulator. No sensor data visible in emulator — test on device.

## Architecture Decisions

- **UI Framework**: Jetpack Compose for Wear (not XML layouts)
- **Theme**: MUST use `androidx.wear.compose.material3.MaterialTheme` wrapper — this provides the WearOS rounded font and correct color system. Do not remove it.
- **Sensor API**: `MeasureClient` from Health Services (direct device sensors, not phone companion)
- **Min SDK**: API 30 (Wear OS 3.0+)
- **State Management**: ViewModel + StateFlow
- **Ambient Mode**: `AmbientLifecycleObserver` handles active/low-power display states

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
        │   ├── MainActivity.kt       — Lifecycle, permissions, ambient mode, MaterialTheme
        │   ├── HeartRateViewModel.kt — Health Services integration
        │   └── HeartRateScreen.kt    — Compose UI (large heart rate number)
        └── AndroidManifest.xml       — BODY_SENSORS permissions
```

## Key Components

### MainActivity.kt
- Requests `BODY_SENSORS` permission at runtime
- Sets up Compose with `AmbientLifecycleObserver`
- Wraps content in `MaterialTheme` (Wear Compose Material3)
- Passes `isAmbient` state to screen

### HeartRateViewModel.kt
- Calls `HealthServices.getClient().measureClient.registerMeasureCallback()`
- Exposes `heartRate: StateFlow<Int?>` and `availability: StateFlow<DataTypeAvailability>`
- Cleans up callback in `onCleared()`

### HeartRateScreen.kt
- Heart rate number centered, as large as fits (160sp bold, max value ~170)
- White in active mode, gray in ambient
- Availability status shown when sensor not ready

## Known Issues / TODOs

- [ ] App icon (currently using default placeholder)
- [ ] Health Services returns no data in emulator — need real device to verify sensor

## Building

```bash
JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" ./gradlew assembleDebug
```
