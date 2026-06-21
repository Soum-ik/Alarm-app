# Glossary

Shared vocabulary for the alarm app. Update as terms are coined or refined during grilling.

- **Alarm** — A scheduled event that, at its fire time, triggers the *Alarm Session*.
- **Alarm Session** — The active, ringing state. Begins when an alarm fires and ends only when all *Dismissal Conditions* are satisfied. While active, the device makes sound/vibration and shows a full-screen UI.
- **Dismissal Conditions** — The set of requirements that must ALL be true to end an Alarm Session. Currently: (1) *Target Scan* succeeds, AND (2) *Charging Gate* is satisfied.
- **Target Scan** — Scanning the pre-configured QR/barcode (the "particular object") with the camera. The decisive unlock action.
- **Target Code** — The specific QR/barcode value the user registered during configuration, by scanning an existing barcode on something they own. The "particular object" is whatever physical thing carries this code.
- **Repeat Days** — The set of weekdays on which a recurring alarm fires. Empty set = one-shot alarm.
- **Backstop** — The 30-minute emergency valve: after an Alarm Session rings 30 min, a fallback dismissal (mechanism TBD) becomes available.
- **Charging Gate** — A Dismissal Condition requiring the phone to be physically plugged into a charger (and/or charging) at dismissal time.
- **Configuration Page** — The screen where the user creates/edits alarms and registers their Target Code.
- **Welcome Page** — The first-run introduction screen.
- **Snooze** — (TBD) Whether the alarm can be temporarily silenced without satisfying Dismissal Conditions. Currently undecided.
