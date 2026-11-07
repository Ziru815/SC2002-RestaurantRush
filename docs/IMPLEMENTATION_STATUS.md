# Implementation status — 7 November 2026

## Stage 1
The immutable `stage-1` tag remains `8d7fa09ddae3053f97f9a7ebe9738f3335740133`.
Default launch uses Regular customers; Stage 1 regression tests and saved victory/defeat inputs still pass.

## Stage 2
Integrated code baseline: `2ece569a68a1ed92d3aaa1b05bc14c60cc851b79`. Run `sh scripts/run.sh --stage2`.

| Part | Implemented scope | Verification |
| --- | --- | --- |
| A | VIP loss 5 and half-up 90% quote; Critic loss 12 and Pasta preference; inherited state rules | 6 customer scenarios |
| B | One quote per order, immutable bill, unchanged preparation and order guards | 8 order scenarios |
| C | Shared employee workflow; invalid actions preserve tasks; no repeated payment; timing/statistics | 3 staff scenario groups, covering all three types |
| D | Factory, arrival callback, stage label, launch flags, complete 18-turn games | 10 controller scenarios |

Both test entry points run all nine suites exactly once: 72 checks pass.
Inputs are in `src/test/resources`; recorded CLI output is in `docs/evidence/stage2`.
Victory: 4 paid, $58.50, average satisfaction 75.5. Defeat: 0 paid, $0.00, N/A.
Names in the class diagram match the implementation: `arrivalFactory` and `stageLabel`.

## Remaining coursework
Stage 3, the final sequence diagram, report/reflections, presentation, team walkthrough and required AI histories/declarations remain separate deliverables. Automated code acceptance does not certify these submissions.
