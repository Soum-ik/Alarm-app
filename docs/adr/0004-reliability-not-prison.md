# ADR 0004: Reliable, but not a prison (anti-cheat stance)

**Status:** Accepted
**Date:** 2026-06-21

## Context
Users can trivially escape any alarm by force-quitting, rebooting, or uninstalling. We had to decide how hard to fight this for a published product. A truly minimal build (no foreground service, no boot re-arm) risks the alarm silently failing to fire — unacceptable for an alarm people rely on. Maximum lockdown (device-admin, blocking uninstall/settings) is invasive and risks Play Store rejection.

## Decision
Build for **reliability, not imprisonment**:
- The alarm MUST fire reliably: use a foreground service and re-arm scheduled alarms on device boot (`BOOT_COMPLETED`).
- Use exact alarms and request battery-optimization exemption so the OS doesn't silently kill us.
- Do NOT fight uninstall, do NOT use device-admin lockdown, do NOT block the user from settings.

## Consequences
- The app is trustworthy (won't silently miss an alarm) but remains escapable by a determined user — accepted, since the value proposition targets honest users.
- Must handle `RECEIVE_BOOT_COMPLETED`, foreground-service notification, `SCHEDULE_EXACT_ALARM`, and battery-optimization prompts.
- Play Store policy compliance is maintained (no invasive admin APIs).
- OEM aggressive task-killing remains a reliability risk; mitigations (autostart guidance, etc.) tracked separately.
