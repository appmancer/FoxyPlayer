# 🦊🎵 Foxy Player

A modern Android music player app built with Kotlin and Jetpack Compose for streaming music from cloud storage.

## Features

- 🎵 Clean, modern Material Design 3 interface
- 🦊 Foxy-themed player experience
- 📱 Built with Jetpack Compose
- 🎯 Kotlin-first development
- 🏗️ MVVM architecture with Repository pattern

## Setup & Build Instructions

### Prerequisites

- Android Studio Hedgehog (2023.1.1) or later
- Android SDK API 24+ (Android 7.0+)
- Kotlin 2.0.20+
- Java 8+

### Building the Project

1. **Clone the repository:**
   ```bash
   git clone <repository-url>
   cd FoxyPlayer
   ```

2. **Open in Android Studio:**
   - Open Android Studio
   - Select "Open an Existing Project"
   - Navigate to the project directory and open it

3. **Build the project:**
   ```bash
   # Clean and build
   ./gradlew clean build
   
   # Build debug APK
   ./gradlew assembleDebug
   
   # Run tests
   ./gradlew test
   ```

4. **Run the app:**
   - Connect an Android device or start an emulator
   - Click "Run" in Android Studio or use:
   ```bash
   ./gradlew installDebug
   ```

## Project Structure

```
app/src/main/java/com/foxy/player/
├── data/           # Data layer (repositories, network, local storage)
├── domain/         # Domain layer (use cases, entities, repository interfaces)
├── presentation/   # Presentation layer (UI, ViewModels, Compose screens)
├── ui/theme/      # Material Design 3 theming
└── MainActivity.kt # Main entry point
```

## Technology Stack

- **Language:** Kotlin
- **UI Framework:** Jetpack Compose + Material Design 3
- **Architecture:** MVVM with Repository pattern
- **Build System:** Gradle (Kotlin DSL)
- **Minimum SDK:** 24 (Android 7.0)
- **Target SDK:** 34 (Android 14)

## Development

### Code Style
- Follow official Kotlin coding conventions
- Use ktlint for code formatting
- Use detekt for code quality analysis

### Quality Gates
- ✅ Build success: `./gradlew build`
- ✅ Unit tests: `./gradlew test`
- ✅ Code style: `./gradlew ktlintCheck`
- ✅ Static analysis: `./gradlew detekt`
- ✅ Android lint: `./gradlew lint`

## Contributing

This project follows Test-Driven Development (TDD) practices:

1. Write failing tests first (RED)
2. Implement minimal code to pass (GREEN)
3. Refactor while keeping tests green (REFACTOR)

## License

[Add your license information here]