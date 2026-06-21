# ADR 0003: Charging Gate requires "plugged in now" at dismissal

**Status:** Accepted
**Date:** 2026-06-21

## Context
The charger requirement was motivated by two goals: forcing the user out of bed, and keeping the phone charged. These conflict: an overnight-charged phone (charger by the bed) provides no friction, while a friction charger (across the room, plugged in only at dismissal) does not keep the phone charged overnight. A choice was required.

## Decision
The Charging Gate requires the device to be **actively plugged in / charging at the moment of dismissal**. The "keep phone charged overnight" goal is dropped where it conflicts; friction is prioritized.

## Consequences
- At dismissal we check live charging state (e.g. `BatteryManager` / `ACTION_POWER_CONNECTED`).
- The intended setup is charger placed away from bed; user must walk to it. This is the "feature," accepted even though the alarm is otherwise escapable (see ADR 0004) — it serves honest users, not as anti-cheat.
- Requires reading battery/power state (no dangerous permission needed for charging state on Android).
- Edge case: no charger available at all is handled by the timed backstop (ADR 0005).
