# ADR 0005: 30-minute timed backstop as emergency valve

**Status:** Accepted (fallback mechanism TBD)
**Date:** 2026-06-21

## Context
A user may be genuinely unable to satisfy the Dismissal Conditions: lost/damaged QR target, broken camera, or no charger available. Without a safety valve the alarm could ring indefinitely — a guaranteed angry-user / 1-star outcome and a real-world nuisance to others nearby.

## Decision
Provide a **timed backstop**: after the Alarm Session has been ringing for **30 minutes**, a fallback dismissal becomes available.

This is explicitly an **emergency valve**, not an everyday snooze/exit. 30 minutes is intentionally long so the normal path (scan + charger) is the path of least resistance.

## Consequences
- Need a timer in the Alarm Session that, at 30:00, surfaces a fallback dismissal.
- **OPEN QUESTION (TBD):** what is the fallback mechanism? Candidates: a hard math puzzle, a user-set emergency PIN, or a plain dismiss button. To be decided in a later grilling round.
- Real-world risk: 30 minutes of alarm can disturb cohabitants/neighbors. Accepted as the emergency threshold; the design assumes users normally dismiss in seconds via scan.
- Consider logging backstop usage as a product signal (frequent use = the scan/charger flow is too hard).
