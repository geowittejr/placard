# Placard

**Turn your phone into a giant, readable sign.**

Type a message (or tap a saved one) and Placard fills the screen with it in the largest letters that
fit, without ever splitting a word. Useful for airport pickups, a noisy bar, talking with someone who is
hard of hearing, across a quiet library, from a car window, or cheering at a game.

## Features (POC)

- **Fit to screen**: binary-searched font size so the text is as large as possible, on balanced lines
- **Scrolling** ticker mode for longer messages (tap to pause)
- Six high-contrast color styles, each tested to WCAG contrast of at least 4.5:1
- Full-screen, immersive display; screen stays awake; optional full brightness while showing
- **Double-tap to flip** the sign 180° so the person across the table can read it
- The sign follows how you hold the phone (landscape gives bigger letters), even with rotation lock on
- Saved phrases: tap to show instantly, long-press to edit, × to remove; your draft is remembered

## Why it's low risk

- **No permissions**: keep-screen-on and brightness are per-window settings that need none
- **No network, no third-party libraries**: Kotlin and the Android framework only
- A single custom `View` draws the sign with `StaticLayout`; there is nothing exotic to break

## Run it

**Android Studio:** File → Open → this `placard` folder → Gradle sync → pick or create an emulator
(API 36) → Run ▶.

**Command line** (with `ANDROID_HOME` set or a `local.properties` with `sdk.dir=...`):

```powershell
.\gradlew.bat assembleDebug
.\gradlew.bat installDebug
.\gradlew.bat test
```

## Project layout

```
app/src/main/java/com/georgewitte/placard/
  MainActivity.kt        composer: message, style, mode, saved phrases
  DisplayActivity.kt     the full-screen sign (system Back closes it)
  ui/BigTextView.kt      auto-fit + ticker drawing, tap / double-tap handling
  ui/Ui.kt               edge-to-edge insets + drawing helpers
  data/PlacardStore.kt   SharedPreferences persistence
  logic/TextFit.kt       fit search, word and ticker helpers (unit tested)
  logic/Phrases.kt       saved-phrase rules (unit tested)
  logic/SignStyles.kt    color styles + WCAG contrast check (unit tested)
```

## Build setup

AGP 9.2.1 (built-in Kotlin) · Gradle 9.4.1 · compileSdk/targetSdk 36 · minSdk 26 · JDK 17+.

## Before publishing to Google Play

- Confirm the final `applicationId`, create an upload key, build with `.\gradlew.bat bundleRelease`
- Data safety form: **no data collected or shared**
- Screenshots sell this app: show an airport pickup sign and a flipped sign across a table

## Next ideas

- Quick-settings tile or home-screen shortcut that opens straight to a favorite phrase
- Emoji-only "reaction" signs (👍 ❤️ 😂) and a big countdown/number mode
- Custom colors and a font picker
