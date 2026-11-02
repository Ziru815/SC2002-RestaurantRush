# Implementation Status

## Implemented in the scaffold
- Java entry point, package structure and scripts; no external runtime dependencies.
- Fixed base configuration and immutable menu values in cents.
- Entity constructors, private state and read-only collection snapshots.
- Staff availability, deliberate skip and per-turn reset helpers.
- Integrated ConsoleUI → GameController → Restaurant preview.
- Status/menu/tasks/help, blank/unknown input recovery, EOF and quit.
- Explicit exceptions from pending business methods, before any state mutation.
- Nine runtime smoke scenarios for the above boundaries.

## Still required for completed Stage 1
- All TODO[A/B/C/D] business methods and actual gameplay input.
- Exactly 18 turns, scheduled arrivals and staff ordering.
- Seating, customer food selection, order registration and preparation.
- Ready-to-serve timing, eating, oldest-eligible automatic payment.
- Satisfaction loss, cancellation, abandonment and table cleanup.
- Final AND-based victory calculation and full reporting.
- Real gameplay tests, one reproducible victory run and one reproducible defeat run.
- A reviewed runnable commit tagged stage-1.

## Known development boundary
The preview intentionally stays at turn 0 with an empty restaurant. It is not an example of turn 1, which must include an arrival after gameplay is implemented. `start` is blocked at GameController.processTurn; replacing that exception is not enough to complete the turn. Implement and integrate the named helper methods as well.

The smoke suite checks the current scaffold contract. It must evolve with real gameplay. Passing it does not demonstrate the course's Stage 1 rules. No Stage 2 or Stage 3 feature is present.
