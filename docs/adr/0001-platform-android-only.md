# ADR 0001: Target Android only (initially)

**Status:** Accepted
**Date:** 2026-06-21

## Context
The core mechanic is an alarm that cannot be dismissed until the user (a) scans a configured target and (b) has the phone on a charger. This requires a full-screen, hard-to-bypass alarm that fires reliably even when the app is backgrounded or the device is idle. iOS structurally forbids this pattern (no true persistent background alarm; notifications are user-silenceable; apps get killed). Android permits it via foreground services, full-screen intents, and exact alarms.

## Decision
Build for **Android only** for the initial product.

## Consequences
- We can implement a genuinely hard-to-cheat alarm using `AlarmManager` exact alarms, a foreground service, and a full-screen Activity.
- Must handle Android's battery-optimization / Doze restrictions and the `SCHEDULE_EXACT_ALARM` and `USE_FULL_SCREEN_INTENT` permissions (Android 13/14 policy).
- No iOS users at launch. Cross-platform (Flutter/RN) is deferred; if chosen later, the dismissal mechanic would need an Android-specific native module anyway.
- OEM-specific aggressive task-killing (Xiaomi, Samsung, etc.) is a known reliability risk to be addressed.
