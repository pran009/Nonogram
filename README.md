# Nonogram

A native Android nonogram (picross) game built with Kotlin and Jetpack Compose, structured
to be published on Google Play.

## Features

- 32 hand-drawn pixel-art puzzles in three packs (5×5, 10×10, 15×15), each verified by a
  unit test to be solvable by logic alone.
- Daily puzzle (same for everyone on a given day) and unlimited random puzzles, all
  generated so that they never require guessing.
- Tap or drag to fill, cross mode, axis-locked drag painting, pinch-zoom and pan on large
  boards, double-tap to reset zoom.
- Optional mistake checking, undo, logical hints, timer, best times, auto-save and resume.
- Light and dark themes, adaptive launcher icon, no permissions, no network, no ads.

## Project layout

```
app/src/main/java/app/nonogram/puzzle/
  model/Puzzle.kt            puzzle definition + clue derivation
  logic/LineSolver.kt        line-logic solver (verification + hints)
  logic/PuzzleGenerator.kt   random solvable puzzle generator
  data/Puzzles.kt            built-in puzzle catalogue (ASCII art)
  data/ProgressStore.kt      SharedPreferences persistence
  ui/GameController.kt       game state: board, undo, timer, mistakes
  ui/BoardCanvas.kt          the interactive board (Canvas + gestures)
  ui/GameScreen.kt, HomeScreen.kt, LevelsScreen.kt, NonogramApp.kt
```

## Build

Open the folder in Android Studio and press Run, or from a terminal:

```
gradlew.bat assembleDebug
gradlew.bat testDebugUnitTest
```

Set `JAVA_HOME` to Android Studio's bundled JDK if `java` is not on your PATH
(`C:\Program Files\Android\Android Studio\jbr`).

## Adding puzzles

Add a `p("id", "Name", "row", "row", ...)` entry in `data/Puzzles.kt` using `#` for filled
and `.` for empty. Run the unit tests: `everyBuiltInPuzzleIsLineSolvable` fails and names any
puzzle that would require guessing.

## Publishing to Google Play

1. **Choose your application id.** It must be globally unique and cannot change after the
   first upload. Edit `applicationId` (and optionally `namespace`) in `app/build.gradle.kts`,
   e.g. `com.yourname.nonogram`.
2. **Create an upload keystore** (keep it and its passwords safe; losing it means you can
   never update the app):
   ```
   "C:\Program Files\Android\Android Studio\jbr\bin\keytool" -genkeypair -v -keystore upload-keystore.jks -alias upload -keyalg RSA -keysize 2048 -validity 10000
   ```
3. **Configure signing.** Copy `keystore.properties.example` to `keystore.properties` in the
   project root and fill in the path and passwords. The file is git-ignored.
4. **Build the release bundle:**
   ```
   gradlew.bat bundleRelease
   ```
   Output: `app/build/outputs/bundle/release/app-release.aab`.
5. **Play Console.** Create the app, upload the `.aab` to Internal testing first, then
   Production. You will need:
   - App icon 512×512 PNG and a 1024×500 feature graphic.
   - At least 2 phone screenshots (run the app on an emulator and capture).
   - A short and full description, content rating questionnaire (puzzle game, no user
     content), target audience, and a privacy policy URL. This app collects no data, so a
     one-paragraph policy stating that is enough. In the Data safety form declare that no
     data is collected or shared.
6. **Each update:** bump `versionCode` (must increase) and `versionName` in
   `app/build.gradle.kts`, rebuild the bundle, upload.

## Ideas for later

- More puzzle packs (20×20, colour nonograms).
- Achievements / leaderboards through Google Play Games Services.
- Rewarded hints via AdMob if you want monetisation.
