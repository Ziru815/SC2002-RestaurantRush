# Stage 2 acceptance — all feature items initially pending

The scaffold is not a completed stage. A passing Stage 1 regression run does not clear these boxes. Each checked item needs a commit, command and actual result.

- [ ] All three types use the same seat/order/cook/serve/eat/pay flow.
- [ ] Waiting loss Regular 8 / VIP 5 / Critic 12; clamp 0; loss only QUEUED/SEATED.
- [ ] VIP bills use 90% (Salad 810, Burger 1350, Pasta 1620 cents); others full price.
- [ ] Bill quoted once on placement, locked, and collected once; no unpaid revenue or double discount.
- [ ] Critic preference is documented and observed; scaffold choice: always Pasta. Demonstrate that Regular ID 4 would choose Salad but Critic ID 4 chooses Pasta.
- [ ] Six arrivals at 1/4/7/10/13/16 with at least one VIP and Critic; scaffold choice: VIP ID 2 and Critic ID 4.
- [ ] Customer behavior is polymorphic, without type-check chains in controller/UI/staff.
- [ ] Default Stage 1 regression path remains functional; --stage2 launches the complete mixed game.
- [ ] Stage 2 screen identifies stage/customer types, chosen meals, bills, state and staff availability.
- [ ] Invalid actions do not consume tasks; one Waiter action / Chef unit / payment per turn.
- [ ] No same-turn READY->serve or READY_TO_PAY->payment; two tables stay occupied until pay/leave.
- [ ] All 18 turns; paid >=4 AND cents >=4500 AND average >=60 for victory; failure of any target loses.
- [ ] Satisfaction recorded at payment; no paid customer => N/A; abandonment cancels work and releases table.
- [ ] All four owner suites contain real assertions; no pending exceptions, empty tests or tests that reward TODO behavior.
- [ ] Normal test.sh runs all completed suites; a failing suite returns nonzero.
- [ ] Stage 2 independently builds/runs offline with Java 17+.
- [ ] Save reproducible victory and defeat inputs/results at the tested Stage 2 commit.
- [ ] A teammate reproduces the runs and reviews the PR; record reviewer and commit.
- [ ] Stage 2 class diagram matches this code; sequence diagram covers a Stage 2 or 3 behavior.
- [ ] README records added/changed/deleted files, reasons and immutable compare link for stage-1...stage-2.
- [ ] No Host, ComboMeal or Happy Hour code in this stage.
- [ ] Create stage-2 tag only after acceptance; never move stage-1.

## Actual evidence (fill after implementation)
| Item | Commit / command / actual result | Reviewer |
| --- | --- | --- |
| A customer behavior | Pending | Pending |
| B quoted/locked bills | Pending | Pending |
| C employee workflow | Pending | Pending |
| D complete gameplay | Pending | Pending |
| Victory replay | Pending | Pending |
| Defeat replay | Pending | Pending |
