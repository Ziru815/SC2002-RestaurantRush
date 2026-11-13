# Stage 3 code acceptance

Tested code: tag `stage-3`, commit `51721224e62392ad4e6ee115d5bbe8b932b9f9e1` (merge of PR #18), checked on 13 November 2026. `sh scripts/test.sh` runs 13 suites once each: 111/111 checks pass.

| Suite | Checks |
| --- | --- |
| Stage 1 suites (smoke, customer, order/table, controller) | 9 + 11 + 14 + 6 = 40 |
| Customer extension and Stage 2 suites | 5 + 6 + 8 + 3 + 10 = 32 |
| Stage3CustomerMenuTests | 7 |
| Stage3PricingTests | 7 |
| Stage3HostStaffTests | 13 |
| Stage3ControllerTests | 12 |

- [x] Combo = Salad + Pasta, 2430 cents, 3 preparation units; only on the Stage 3 menu; Regular ID 5 orders it in Stage 3.
- [x] Happy Hour can be started once, lasts the turn it starts and the next, and is not offered again after it is used.
- [x] Price order: menu price, then VIP 90%, then Happy Hour 80%, rounding half up at each step (VIP Burger 1500, 1350, 1080 cents).
- [x] The bill is locked when the order is taken; an order taken during Happy Hour keeps its discount after Happy Hour ends, and one taken before or after pays full price.
- [x] While Happy Hour is active, QUEUED and SEATED customers recover 15 satisfaction once per turn, capped at 100. Typing status, help or a wrong command does not repeat it.
- [x] The Host seats one queued customer per turn; the Waiter can also seat, so two customers can be seated in the same turn.
- [x] Invalid Manager, Host, Waiter and Chef commands do not use that staff member's task.
- [x] Stage 3 reuses the Stage 2 arrivals (VIP ID 2, Critic ID 4) and the same win targets.
- [x] `--stage3` launches Stage 3; `--stage2 --stage3` is rejected; default and `--stage2` are unchanged.
- [x] Stage 1 and Stage 2 show no Host, Manager or Happy Hour, and their suites and replays still pass.

## Recorded replays

```sh
sh scripts/build.sh
java -cp out/main restaurantrush.Main --stage3 < src/test/resources/stage3-victory.input.txt
java -cp out/main restaurantrush.Main --stage3 < src/test/resources/stage3-defeat.input.txt
```

| Run | Turns | Paid | Revenue | Paid satisfaction mean | Departures | Result |
| --- | --- | --- | --- | --- | --- | --- |
| Victory | 18 | 5 | $80.10 | 85.6 | 0 | VICTORY |
| Defeat | 18 | 0 | $0.00 | N/A | 2 | DEFEAT |

In the victory run the Manager waits on turns 1 to 3 and starts Happy Hour on turn 4. Customer 2 (VIP) orders a Burger during Happy Hour and pays $10.80. Customer 5 orders the Combo after Happy Hour and pays the full $24.30. The locked bills are $9.00, $10.80, $18.00, $18.00 and $24.30.

The full CLI output of both runs is saved in `evidence/stage3/`. `Stage3ControllerTests` checks these results against the Restaurant and Cashier state.

## Known limitation

If Happy Hour is started on turn 18, the screen says "2 turns left including this one" although the game ends after that turn. Only the text is affected: the discount and recovery apply on turn 18 as expected. Left unchanged so the tagged code matches the report and video.
