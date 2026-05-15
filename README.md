# WearOS Heart Rate App

A minimal Wear OS 3.0+ app that displays the device's live heart rate in a large Material Design 3 style.

## Features

- **Large Heart Rate Display**: Bold, easy-to-read BPM number using Wear Material Design 3
- **Health Services Integration**: Direct sensor access via `MeasureClient`
- **Ambient Mode Support**: Automatically dims in low-power ambient mode to save battery
- **Pulse Animation**: Optional heart pulse animation in active mode
- **Runtime Permissions**: Requests `BODY_SENSORS` permission on first run

## Architecture

```
HeartRateApp/
├── MainActivity.kt          - Single activity, handles lifecycle & ambient mode
├── HeartRateViewModel.kt    - Manages Health Services connection and heart rate state
├── HeartRateScreen.kt       - Compose UI with large BPM display
└── theme/
    ├── Theme.kt             - WearMaterial3 theme setup
    └── Type.kt              - Typography for large heart rate text
```

## Tech Stack

- **UI**: Jetpack Compose for Wear (Material Design 3)
- **Sensors**: androidx.health:health-services-client
- **State**: ViewModel + StateFlow
- **Min SDK**: 30 (Wear OS 3.0)
- **Target SDK**: 34

## Building & Running

### Prerequisites
- Android Studio Flamingo+
- Wear OS 3.0+ emulator or device
- API Level 30+

### Build
```bash
./gradlew build
```

### Run on Emulator
1. Create a Wear OS 3 AVD in Android Studio
2. Connect emulator
3. Run: `./gradlew installDebug`

### Grant Permissions
When app first launches, grant `BODY_SENSORS` permission at runtime.

## Testing Ambient Mode

### On Emulator
```bash
# Enter ambient mode
adb shell dumpsys window | grep mAmbientMode

# Manually toggle (if supported by emulator)
# Wear tilt gesture or check device settings
```

### On Device
Tilt the wrist away or let the screen dim naturally.

## Future Enhancements

- Heart rate history/graph
- Export data to Google Fit
- Customize alert thresholds
- Multiple color themes
