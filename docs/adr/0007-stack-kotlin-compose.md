# ADR 0007: Stack is native Kotlin + Jetpack Compose

**Status:** Accepted
**Date:** 2026-06-21

## Context
The mechanic depends on low-level Android: `AlarmManager` exact alarms, a foreground service, a full-screen lock-screen Activity, `BOOT_COMPLETED` re-arming, charging-state detection, and ML Kit barcode scanning. Cross-platform frameworks would still require native Android code for all of this.

## Decision
Build as a **native Android app in Kotlin using Jetpack Compose** for UI. Persistence via Room. Barcode scanning via ML Kit + CameraX. No snooze (see ADR 0008).

## Consequences
- Minimum SDK 26 (API 26, Android 8) for foreground-service ergonomics; target latest.
- Requires Android Studio + Gradle to build/run; cannot be built in this CLI environment.
- Libraries: AndroidX, Compose, Room, ML Kit Barcode, CameraX, WorkManager not needed (using AlarmManager directly for exactness).
