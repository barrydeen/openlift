# OpenLift

A lightweight, privacy-first, open-source **lifting tracker** for Android, optimized for
[GrapheneOS](https://grapheneos.org/). Built with Kotlin and Jetpack Compose.

OpenLift is deliberately minimal and fast: no accounts, no cloud, no trackers, no ads, and **no
network permission at all**. All of your training data lives locally on your device (Room / SQLite),
and you can export/import a single backup JSON file when you want to move it.

## Features

- **Pre-loaded exercise catalog** — every major barbell and dumbbell exercise, grouped by muscle
  group (Chest, Back, Shoulders, Quads, Hamstrings, Glutes, Calves, Biceps, Triceps, Forearms,
  Core, Full Body).
- **Workout sessions** — create a workout, add multiple exercises, and log sets (weight + reps)
  with warm-up marking.
- **Rest timer** — automatically starts a countdown after each logged set; configurable default
  rest time in Settings and adjustable per-session.
- **Last-session placeholders** — when you add an exercise to a new workout, the set count, weights,
  and reps are pre-filled from your most recent session for that exercise.
- **Most-used on top** — your most frequently used exercises appear at the top of the picker.
- **Progress graphs** — estimated 1-rep max (Epley) charted over time per exercise using
  [Vico](https://github.com/patrykandpatrick/vico), with best/latest/change stats.
- **Workout summary** — duration, volume, sets/reps, best e1RM, and a per-exercise breakdown.
- **Custom exercises** — create your own exercises inline.
- **Units** — store canonically in kg, display and enter in either kg or lb.
- **Local backup** — export/import the full database as a JSON file via the system file picker.

## Privacy & GrapheneOS

- **Zero network access.** The app declares no `INTERNET` permission and uses `usesCleartextTraffic=false`.
- **No Google Play Services, Firebase, analytics, or ads.** Only AndroidX, Room, and Vico.
- **Local-only storage** in SQLite via Room with `allowBackup=false` and backup excluded from
  cloud/device-transfer rules — data doesn't leave the device except through your explicit export.
- **Manual DI**, no heavy frameworks, R8 + resource shrinking for a small, fast APK (≈1.9 MB).
- **F-Droid-friendly** MIT-licensed build configuration.

## Getting started

### Prerequisites

- JDK 17+
- Android SDK (Android 15 / API 35, build-tools 35)

### Build

```bash
./gradlew :app:assembleRelease
# or
./gradlew :app:assembleDebug
```

The APK lands in `app/build/outputs/apk/`.

### Tests

```bash
./gradlew :app:testDebugUnitTest
```

The unit tests cover the e1RM calculators (Epley/Brzycki), unit conversion, progress aggregation,
exercise catalog parsing, and full Room repository behavior (including the placeholder-seeding
logic) using Robolectric on the JVM — no emulator required.

## Architecture

- **UI** — Jetpack Compose + Material 3, single-activity with Navigation Compose and a bottom bar.
- **State** — ViewModel + StateFlow, `viewModelFactory` wired to an `AppContainer` (manual DI).
- **Data** — `Room` entities/DAOs, an `ExerciseSeeder` that loads `assets/exercises.json`,
  `ExerciseRepository` / `WorkoutRepository` / `SettingsRepository`, and a `BackupManager`.
- **Domain** — pure functions for `OneRepMax`, `UnitConverter`, `TimeFormatter`, `Stats`,
  `ProgressCalculator`, and `PickerOrder` (all unit-tested).

```
app/src/main/java/dev/openlift/app/
├── OpenLiftApplication.kt
├── MainActivity.kt
├── di/AppContainer.kt
├── data/                 (repositories, seeder, backup)
│   └── local/            (Room entities, DAOs, database)
├── domain/               (testable pure logic)
└── ui/
    ├── navigation/
    ├── theme/
    ├── components/
    ├── home/ history/ workout/ picker/ summary/ progress/ settings/
    └── *ViewModel.kt for each screen
```

## License

MIT. See [LICENSE](LICENSE).
