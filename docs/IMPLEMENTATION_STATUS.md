# Implementation status — 3 November 2026

## Preserved Stage 1
Tag `stage-1` -> `8d7fa09ddae3053f97f9a7ebe9738f3335740133` (PR #7 merge). Regular customer, orders/tables, employees and 18-turn controller are implemented. The four suites passed 40/40; saved victory/defeat evidence is in `evidence/stage1/`.

The historical checklist includes additional manual checks not all independently signed off. Do not turn all boxes green merely because the automated suites pass. Stage 1 diagrams still need the team's match-to-code review.

## Stage 2 scaffold
| Owner | Supplied now | Still required |
| --- | --- | --- |
| A / Ziru | Customer default quote/type-label hooks; VIP/Critic declarations and TODO methods | loss 5/12, VIP quote, Critic preference, customer tests |
| B | Order integration marker; Stage2OrderTests placeholder | lock polymorphic quote; price/immutability tests |
| C | Existing generic employee flow; cashier reminder; Stage2StaffTests placeholder | real mixed-customer employee/timing/error tests; fix defects only if demonstrated |
| D / Tianren | Stage2Arrivals declaration; integration markers; Stage2ControllerTests placeholder | fixed arrival setup, --stage2 wiring, type display, integration tests and stage evidence |

`sh scripts/test.sh` = existing 40 tests plus 5 extension checks. `sh scripts/test-stage2.sh` = pending/failing until the four members implement real tests and features. No Stage 2 completion tag exists at scaffold creation.

## Stage 3 / submission
Host, ComboMeal, Happy Hour, final stage comparisons, three verified class diagrams, one Stage 2/3 sequence diagram, report with individual reflections, video, full AI histories and declarations remain to be completed.
