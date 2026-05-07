# ThermalFaker

A modern Android app for spoofing device temperatures using Shizuku.

## Features

- **Battery Temperature Spoofing**: Modify reported battery temperature using `dumpsys` commands via Shizuku
- **CPU Temperature Support (Root Required)**: Attempts to write to thermal zone sysfs nodes (requires root access via Shizuku)
- **Quick Reset**: Easily revert to real temperature values
- **Modern Material 3 UI**: Beautiful, dynamic theming with dark mode support
- **Real-time Temperature Display**: Shows current temperature with animated thermometer arc

## How it Works

ThermalFaker uses Shizuku to execute shell commands at ADB level:

- **Battery Temperature**: Uses `dumpsys battery set temp <value>` to change the reported battery temperature (value is in tenths of a degree Celsius)
- **Reset**: Uses `dumpsys battery reset` to revert all battery properties to real values
- **CPU Temperature**: Tries to write to `/sys/class/thermal/thermal_zone*/temp` files (requires root access)

## Requirements

- **Android 12+** (API 31 or higher)
- **Shizuku** installed and running on your device
- **ADB/Shizuku Permission** granted to the app
- **Root Access (Optional)**: For CPU temperature spoofing

## Getting Started

### Prerequisites

1. Install Shizuku from [GitHub](https://github.com/RikkaApps/Shizuku) or Google Play
2. Start Shizuku and grant it necessary permissions
3. Download and install ThermalFaker

### Building from Source

```bash
# Clone the repository
git clone https://github.com/yourusername/thermalfaker.git
cd thermalfaker

# Open in Android Studio or build with Gradle
./gradlew assembleDebug
```

## Usage

1. Open ThermalFaker
2. Grant Shizuku permission when prompted
3. Enter your target temperature in Celsius
4. Tap "Apply Spoof" to set the temperature
5. To revert, tap "Reset"

## Technology Stack

| Component | Technology |
|-----------|------------|
| Language | Kotlin |
| UI Framework | Jetpack Compose |
| Architecture | MVVM |
| Dependency Injection | Hilt |
| Shizuku Integration | Shizuku API 13.1.5 |
| Local Storage | (Optional - DataStore/ Room) |
| Target SDK | 34 |
| Minimum SDK | 31 |

## Project Structure

```
com.thermalfaker.app/
├── ThermalFakerApp.kt              # Application class with Hilt setup
├── core/
│   ├── shizuku/
│   │   ├── ShizukuManager.kt       # Shizuku permission & service management
│   │   └── TemperatureManager.kt   # Temperature command execution
│   └── util/
│       └── Logger.kt               # Simple logging utility
├── data/
│   └── model/
│       └── AppModels.kt            # Data models for UI state & settings
├── di/
│   └── AppModule.kt                # Hilt dependency injection module
└── ui/
    ├── MainActivity.kt             # Main activity with edge-to-edge
    ├── navigation/
    │   ├── Screen.kt               # Navigation routes
    │   └── ThermalFakerNavHost.kt  # Compose navigation setup
    ├── screens/
    │   ├── MainScreen.kt           # Home screen with temperature controls
    │   └── InfoScreen.kt           # About/info screen
    ├── theme/
    │   ├── Color.kt                # App colors
    │   ├── Theme.kt                # Material 3 theme
    │   └── Type.kt                 # Typography
    └── viewmodel/
        └── MainViewModel.kt        # ViewModel for main screen
```

## License

This project is provided for educational purposes only. Temperature spoofing may violate terms of service of other apps. Use responsibly.
