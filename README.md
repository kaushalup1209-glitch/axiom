# Axiom — by Kaushal Kr

**Master your time. Master your life.**

Native Android study companion for planning, focus, distraction management and discipline.
Package: `com.kaushalkr.axiom`.

## Status

- **Phase 01** (foundation and Liquid Glass design system): complete.
- **Phase 02** (Home dashboard): complete. Greeting and date, daily goal ring, quick-start focus
  timer, study time, streak and Distraction Shield access use real data. Distraction time shows
  "Not tracked" until Phase 06.
- **Phase 03** (tasks): complete. The Planner tab is a task list stored offline in Room:
  - create, edit, delete and complete tasks with title, description, subject, deadline (date and
    optional time), estimated duration, priority (Low, Medium, High, Urgent), subtasks and repeat
    (daily, weekdays, weekly, monthly; completing schedules the next one);
  - Today, Upcoming, Overdue and Done views; search, priority and subject filters, five sorts;
  - swipe right to complete, swipe left to delete, long-press drag to reorder (in "My order");
  - undo for every deletion and completion from a Liquid Glass snackbar;
  - Home's "Today's tasks" card uses real data (overdue and due today, progress, tick to complete);
  - `TaskCommand` / `TaskCommandExecutor` expose the same operations as structured, JSON-ready
    commands for the AI assistant (Phase 11).
- **Phase 04** (routines, timetable and academic planner): code complete, **not yet built or
  tested**. The Planner tab switches between Tasks, Routine and Academics:
  - Routine: day timeline, week overview and month calendar; study, break, sleep and personal
    blocks with start time, end time and length; one-off or repeating on chosen weekdays; blocks
    may run past midnight;
  - create, edit, delete (with undo) and duplicate blocks; long-press and drag a block on the
    day timeline to move it in 5-minute steps (with undo);
  - overlap detection while editing, with "move to the next free time", "shorten to fit" or
    "keep both";
  - link open to-do tasks to a block; automation switches (auto focus, distraction tracking,
    app restriction) are stored now for Phases 05–08;
  - Academics: subjects with colours, syllabus chapters (add, tick, reorder, delete with undo),
    exams and deadlines with countdowns;
  - Home shows the ongoing block with its progress, the next blocks, tomorrow's first block and
    the next exam, all from real data (the Phase 02 sample routine is removed);
  - `RoutineCommand` / `RoutineCommandExecutor` give the AI assistant structured commands to
    create, reschedule and list blocks, find free time, and create subjects, chapters and exams.
- **Liquid Glass tiers:** Lite is the default (lightweight glass, background blur only behind the
  navigation bar, dialogs and sheets, no shaders); Full keeps the original Kyant0 Liquid Glass;
  Off renders no glass. Weak devices are steered to Off automatically.
- Focus, Insights, AI and Distraction Shield are placeholders naming their phase.

## Tech stack

| Area | Choice |
|---|---|
| Language / UI | Kotlin 2.4.21, Jetpack Compose (BOM 2026.11.01, Compose 1.13, Material 3 1.5) |
| Architecture | MVVM, manual DI (`AppContainer`), feature packages |
| Navigation | Navigation Compose 2.10 with type-safe routes |
| Storage | DataStore Preferences 1.3 (settings, active focus session), Room 2.8 + KSP (focus history, tasks, routines, subjects, exams) |
| Liquid Glass | Kyant0 Backdrop 2.0.1 + Shapes 1.2.1 |
| Build | AGP 9.5.2 (built-in Kotlin), Gradle 9.9.2, JDK 17+ (21 used here) |
| SDK | minSdk 26, targetSdk 37, compileSdk 37.2 |
| Tests | JUnit 4, Robolectric 4.18.1, Compose UI test, kotlinx-serialization-json (tests only) |

UsageStatsManager, SpeechRecognizer and the AI client are planned for their phases and are not
added yet.

## Build and test

Requires the Android SDK with `platforms;android-37.2` and `build-tools;37.0.0`.

```bash
./gradlew :app:assembleDebug        # debug APK: app/build/outputs/apk/debug/
./gradlew :app:testDebugUnitTest    # JVM + Robolectric tests
./gradlew :app:lintDebug
./gradlew :app:assembleRelease      # R8-minified, unsigned
./gradlew :app:assemblePerf         # release-optimized, profileable, debug-signed → dist/
```

`AxiomScreenshotCaptureTest` writes PNG renders to `app/build/screenshots/` for layout review.
Robolectric renders on the CPU, so judge blur and refraction on a real device.

## Documentation

- [Project structure and glass rendering model](docs/PROJECT_STRUCTURE.md)
- [Section index](docs/SECTION_INDEX.md)
- [Third-party notices](THIRD_PARTY_NOTICES.md)
