# ADR 0006: v1 scope (multiple recurring alarms) & scan-existing target

**Status:** Accepted
**Date:** 2026-06-21

## Context
Needed to fix the feature scope of v1 and how the Target Code is registered, for a published product.

## Decision
- **Multiple alarms.** The app manages a list of independently configured alarms.
- **Recurring scheduling.** Each alarm supports repeat on selected days of week (standard alarm-clock behavior), in addition to one-shot.
- **Target registration = scan an existing barcode.** The user scans a code on something they already own and registers that value as the Target Code. No printing required.

## Philosophy (explicit)
v1 optimizes for **low-friction adoption over enforced rigor**. Because the user may register a portable item's barcode (e.g. a water bottle) and keep it bedside, location-based friction is NOT enforced. Combined with ADR 0004 (escapable by design), the product's stance is: *it helps users who want to be helped; it does not trap them.*

## Consequences
- Storage: a list of alarm records, each with time, repeat-days, enabled flag, Target Code, and per-alarm settings (e.g. charging gate on/off if later made configurable).
- Scheduling must handle day-of-week repeats and re-arming the next occurrence after each fire; DST/timezone correctness required.
- Barcode scanning (e.g. ML Kit / ZXing) used in BOTH configuration (register) and Alarm Session (dismiss).
- Future option (deferred): app-generated codes for users who want enforced location friction.
