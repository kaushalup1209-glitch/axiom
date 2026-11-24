# Axiom — by Kaushal Kr

**Master your time. Master your life.**

Native Android study companion for planning, focus, distraction management and discipline.
Package: `com.kaushalkr.axiom`.

## Status

Phase 01 (foundation and Liquid Glass design system) is complete. The five tabs, Distraction
Shield and Settings exist; only Settings has working features (theme and glass preferences).
Every other screen is a placeholder naming the phase that delivers it.

## Tech stack

| Area | Choice |
|---|---|
| Language / UI | Kotlin 2.4.21, Jetpack Compose (BOM 2026.11.01, Compose 1.13, Material 3 1.5) |
| Architecture | MVVM, manual DI (`AppContainer`), feature packages |
| Navigation | Navigation Compose 2.10 with type-safe routes |
| Preferences | DataStore Preferences 1.3 |
| Liquid Glass | Kyant0 Backdrop 2.0.1 + Shapes 1.2.1 |
| Build | AGP 9.5.2 (built-in Kotlin), Gradle 9.9.2, JDK 17+ (21 used here) |
| SDK | minSdk 26, targetSdk 37, compileSdk 37.2 |
| Tests | JUnit 4, Robolectric 4.18.1, Compose UI test |

Room, UsageStatsManager, SpeechRecognizer and the AI client are planned for their phases and
are not added yet.

## Build and test

Requires the Android SDK with `platforms;android-37.2` and `build-tools;37.0.0`.

```bash
./gradlew :app:assembleDebug        # debug APK: app/build/outputs/apk/debug/
./gradlew :app:testDebugUnitTest    # JVM + Robolectric tests
./gradlew :app:lintDebug
./gradlew :app:assembleRelease      # R8-minified, unsigned
```

`AxiomScreenshotCaptureTest` writes PNG renders to `app/build/screenshots/` for layout review.
Robolectric renders on the CPU, so judge blur and refraction on a real device.

## Documentation

- [Project structure and glass rendering model](docs/PROJECT_STRUCTURE.md)
- [Section index](docs/SECTION_INDEX.md)
- [Third-party notices](THIRD_PARTY_NOTICES.md)
