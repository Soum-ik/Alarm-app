# ADR 0002: Dismissal target is a QR/barcode scan

**Status:** Accepted
**Date:** 2026-06-21

## Context
The "particular object image" needs to be recognized to dismiss the alarm. Options considered:
1. **QR/barcode scan** — user registers a code; app matches the scanned value.
2. **Photo match** — compare camera frame to a stored photo of the object. Flaky under real-world lighting/angle; hard.
3. **On-device ML object classification** — names object *type* but cannot distinguish the user's specific object; adds model size.

## Decision
Use a **QR/barcode scan** as the Target Scan. The user registers a Target Code during configuration; dismissal requires scanning a matching code.

## Consequences
- Reliable, offline, fast even in poor lighting at wake-up time.
- Trivial to implement vs. ML; no model bundling.
- UX implication: the "object" is really "a thing with a QR sticker on it." User must print/place a code. We provide generated codes and/or let them scan any existing barcode (e.g. a cereal box, shampoo bottle) as their target.
- Open question: do we let the user pick ANY scannable code (incl. existing product barcodes) or only app-generated QR codes? (To be grilled.)
