# Spirals

An interactive Fibonacci spiral visualizer built with Kotlin Multiplatform and Compose Multiplatform. Renders animated golden spirals with adjustable parameters across Android, Desktop, and Web.

## Prerequisites

- **JDK 11** or higher
- **Android Studio** (for Android development) or any IDE with Kotlin support
- **Android SDK** with API level 24+ (for Android builds)

## Running the App

### Desktop (Recommended for Quick Testing)

```shell
./gradlew :composeApp:run
```

This launches the desktop application immediately. The spiral visualization will open in a native window.

### Android

1. Connect an Android device or start an emulator
2. Build and install:
   ```shell
   ./gradlew :composeApp:installDebug
   ```

Or open the project in Android Studio and use the run button.

### Web (WebAssembly)

```shell
./gradlew :composeApp:wasmJsBrowserDevelopmentRun
```

Opens in your default browser. Requires a modern browser with WebAssembly support (Chrome, Firefox, Safari, Edge).

### Web (JavaScript Fallback)

```shell
./gradlew :composeApp:jsBrowserDevelopmentRun
```

Use this for older browsers without full Wasm support.

## Project Structure

```
composeApp/          # Compose Multiplatform UI (Android, Desktop, Web)
  src/commonMain/    # Shared UI code
  src/androidMain/   # Android-specific code
  src/jvmMain/       # Desktop-specific code
  src/wasmJsMain/    # Web (Wasm) specific code
shared/              # Shared business logic
  src/commonMain/    # Fibonacci generation, spiral geometry
server/              # Ktor server (optional)
```

## Features

- Animated Fibonacci spiral rendering
- Adjustable number of spiral segments
- Play/pause animation controls
- Golden ratio visualization
- Responsive layout across all platforms