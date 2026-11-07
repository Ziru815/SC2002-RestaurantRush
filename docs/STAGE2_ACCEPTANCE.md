# Stage 2 code acceptance

Tested code baseline: `2ece569a68a1ed92d3aaa1b05bc14c60cc851b79`. Run both `sh scripts/test.sh` and `sh scripts/test-stage2.sh`: 72/72 checks, nine suites, no duplicate execution.

- [x] All three types use the same seat/order/cook/serve/eat/pay flow.
- [x] Waiting loss Regular 8 / VIP 5 / Critic 12; clamp 0; loss only QUEUED/SEATED.
- [x] VIP bills use 90% (Salad 810, Burger 1350, Pasta 1620 cents); others full price.
- [x] Bill quoted once on placement, locked, and collected once; no unpaid revenue or double discount.
- [x] Critic preference is documented and observed; team choice: always Pasta. Demonstrate that Regular ID 4 would choose Salad but Critic ID 4 chooses Pasta.
- [x] Six arrivals at 1/4/7/10/13/16 with at least one VIP and Critic; team choice: VIP ID 2 and Critic ID 4.
- [x] Customer behavior is polymorphic, without type-check chains in controller/UI/staff.
- [x] Default Stage 1 regression path remains functional; --stage2 launches the complete mixed game.
- [x] Stage 2 screen identifies stage/customer types, chosen meals, bills, state and staff availability.
- [x] Invalid actions do not consume tasks; one Waiter action / Chef unit / payment per turn.
- [x] No same-turn READY->serve or READY_TO_PAY->payment; two tables stay occupied until pay/leave.
- [x] All 18 turns; paid >=4 AND cents >=4500 AND average >=60 for victory; failure of any target loses.
- [x] Satisfaction recorded at payment; no paid customer => N/A; abandonment cancels work and releases table.
- [x] All four owner suites contain real assertions; no pending exceptions, empty tests or tests that reward TODO behavior.
- [x] Normal test.sh runs all completed suites; a failing suite returns nonzero.
- [x] Stage 2 independently builds/runs offline with Java 17+.
- [x] Save reproducible victory and defeat inputs/results at the tested Stage 2 commit.

- [x] No Host, ComboMeal or Happy Hour code in Stage 2.
- [x] Stage 2 class diagram shows implemented names and relationships.
- [x] README records changed/new code and an immutable Stage 1-to-2 comparison.

## Recorded replays

```sh
sh scripts/build.sh
java -cp out/main restaurantrush.Main --stage2 < src/test/resources/stage2-victory.input.txt
java -cp out/main restaurantrush.Main --stage2 < src/test/resources/stage2-defeat.input.txt
```

| Run | Turns | Paid | Revenue | Paid satisfaction mean | Result |
| --- | --- | --- | --- | --- | --- |
| Victory | 18 | 4 | $58.50 | 75.5 | VICTORY |
| Defeat | 18 | 0 | $0.00 | N/A | DEFEAT |

Actual CLI outputs are saved in `evidence/stage2/`. Controller tests assert these results against Restaurant/Cashier state.
A/B tests cover half-up rounding, overflow, null inputs, immutable bills and quote counts. C tests cover all types and later-turn duplicate payments. D tests cover arrival times, CLI flags, full games and Stage 1 compatibility.

## Review and release

Integration was checked with automated tests and independent assistant code review on Ziru's behalf; no separate human replay is claimed. PR conversations record the GitHub review and merge actors. Preserve `stage-1`; create `stage-2` only on the merged, retested main commit.
The group walkthrough, report, sequence diagram and other submission material remain separate coursework tasks.
