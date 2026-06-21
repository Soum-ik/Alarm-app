# ADR 0008: No snooze

**Status:** Accepted
**Date:** 2026-06-21

## Context
Snooze is the exact loophole this app exists to eliminate. A free snooze button would let users bypass the scan+charger ritual entirely.

## Decision
**No snooze.** The only ways to end an Alarm Session are: (1) satisfy the Dismissal Conditions (Target Scan + Charging Gate), or (2) use the 30-minute Backstop (ADR 0005).

## Consequences
- The Alarm Session UI has no snooze affordance.
- Simpler state machine: ringing -> dismissed (or backstop-dismissed). No snooze-pending state.
- Glossary "Snooze (TBD)" entry resolved to "does not exist."
