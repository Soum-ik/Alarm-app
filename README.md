# Alarm — the alarm you can't cheat

An Android alarm app whose alarms don't stop until you **scan a configured object** and
**plug the phone into a charger**. No snooze. A 30-minute emergency backstop is the only escape.

The full design rationale lives in [docs/](docs/) — a glossary plus 8 ADRs capturing every
decision and *why*.

## What it does

- **Welcome screen** → **alarm list** → **add/edit alarm**.
- Each alarm: time, weekday repeat, optional charger requirement, and a **registered barcode**
  (you scan something you own — a bottle, a book — and that becomes the object you must re-scan to dismiss).
- When an alarm fires: a full-screen, over-the-lock-screen UI rings at full volume and vibrates.
  It stops **only** when you scan the right barcode **and** the phone is charging.
- After 30 minutes a "stop anyway" backstop appears (emergency valve, ADR 0005).
- Survives reboot and app-kill (foreground service + re-arm on boot). Does **not** fight uninstall.

## Requirements

- **Android Studio** (Koala / 2024.1+), JDK 17.
- An Android device or emulator, **minSdk 26** (Android 8.0), target 34.
- This app cannot be built from a plain CLI without the Android SDK — open it in Android Studio.

## Build & run

1. Open this folder in Android Studio (`File → Open`).
2. **Bootstrap the wrapper jar.** `gradlew`/`gradlew.bat` are included, but the binary they load,
   `gradle/wrapper/gradle-wrapper.jar`, is **not** checked in (it cannot be authored as text).
   Android Studio's first Gradle sync regenerates it automatically. CLI alternative: with a
   system Gradle ≥ 8.9 installed, run `gradle wrapper` once in this folder, then
   `./gradlew :app:installDebug`. Until the jar exists, `./gradlew` will fail with
   "Could not find or load main class org.gradle.wrapper.GradleWrapperMain" — that's expected.
3. Run the `app` configuration on your device/emulator.

## Permissions you'll be asked for (and why)

| Permission | Why | ADR |
|------------|-----|-----|
| Camera | Scan the barcode to register + dismiss | 0002 |
| Notifications (Android 13+) | Show the full-screen alarm | — |
| Exact alarm (Android 12+) | Fire at the precise minute | 0004 |
| Ignore battery optimizations | Don't let the OS kill the alarm | 0004 |

The app surfaces the exact-alarm and battery prompts as buttons on the alarm-list screen
when they're not yet granted.

## Testing the ring flow fast

Set an alarm one minute out, register any barcode you have handy, lock the phone, and wait.
To dismiss: scan that same barcode and connect a charger. To test the backstop without waiting
30 minutes, temporarily lower `BACKSTOP_MILLIS` in
[`AlarmSession.kt`](app/src/main/java/co/growthmap/alarm/alarm/AlarmSession.kt).

## Known limitations / open items

- **Backstop fallback** is currently a plain "stop" button after 30 min. Whether it should be a
  math puzzle or PIN is still open (ADR 0005, marked TBD).
- **OEM task-killers** (Xiaomi, Samsung, etc.) may still kill background apps; production hardening
  (autostart guidance per OEM) is not yet implemented.
- The barcode target can be any portable item — by design (ADR 0006, low-friction over enforced rigor).

## Layout

```
app/src/main/java/co/growthmap/alarm/
  MainActivity.kt            navigation + permission gates
  AlarmApp.kt
  data/                      Room entity, DAO, db, repository
  alarm/                     scheduler, receivers, service, ring activity, session state
  scan/                      CameraX + ML Kit barcode scanner composable
  ui/                        theme, viewmodel, screens (welcome/list/edit)
  util/                      permission helpers
docs/                        GLOSSARY.md + adr/*.md
```
