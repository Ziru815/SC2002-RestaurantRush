# Stage 1 Acceptance Checklist

All gameplay items below are **pending** in this skeleton. Record actual evidence before checking an item.

## Build and reproducibility
- [ ] Fresh checkout compiles and runs offline on Java 17+.
- [ ] The completed game is reachable through the documented launch command.
- [ ] Menu uses 900/1500/1800 cents and preparation units 1/1/2.
- [ ] Customers choose their own dishes using the documented ID cycle, shown after ordering.

## Turn flow and input
- [ ] Exactly six arrivals, at turns 1/4/7/10/13/16, initial satisfaction 100.
- [ ] Two tables, one Waiter/Chef/Cashier; no Stage 2/3 classes or behavior.
- [ ] Manager can choose which queued customer to seat, which seated customer orders, which ready order is served, and which order is cooked.
- [ ] Waiter acts once before Chef; Chef may prepare the order taken that turn.
- [ ] Invalid ID, occupied table, repeated order, unready service, completed/cancelled order preparation, bad syntax and nonnumeric IDs explain failure and retry without consuming a task.
- [ ] Blank input and end-of-input do not crash or loop forever. If gameplay input ends, report interruption rather than invent a completed run.
- [ ] Staff flags reset once per turn; Wait consumes that staff member's opportunity.
- [ ] All 18 turns run even if the player already meets or can no longer meet the targets.

## Temporal fixture
Use a controlled Regular customer who orders Pasta, with full attention. This fixture may construct the dish directly in a domain test; it does not change the main game's documented ID food rule.

| Turn | Required event | End-of-turn satisfaction |
| --- | --- | --- |
| 1 | Arrive and seat | 92 |
| 2 | Take Pasta order, Chef prepares unit 1 | 84 |
| 3 | Chef prepares unit 2, READY; cannot serve yet | 76 |
| 4 | Serve, SERVED | 76 |
| 5 | Finish eating, READY_TO_PAY; cannot pay yet | 76 |
| 6 | Pay $18.00, both PAID, free table | Record 76 |

- [ ] All six fixture steps match; no same-turn preparation completion/service or readiness/payment.
- [ ] Table remains occupied during service/eating/readiness, until actual payment.
- [ ] Cashier chooses oldest READY_TO_PAY eligible customer, at most one per turn. Use customer ID only to break equal-readiness ties.
- [ ] Repeat payment is rejected; only paid bills contribute revenue.

## Satisfaction and cleanup
- [ ] Only QUEUED/SEATED lose 8 at turn end; served/eating/ready-to-pay customers lose nothing.
- [ ] Waiting from arrival on turn 1 without service gives 4 after turn 12 and 0 after turn 13; leave at turn 13.
- [ ] At abandonment cancel any PLACED/PREPARING/READY order, remove it from active work, free occupied table, remove from queue, and count departure exactly once.
- [ ] Removing customers/orders during end-of-turn processing causes no skipped update or ConcurrentModificationException.

## Display and final result
- [ ] Start-of-turn display shows turn, revenue, paid average, queue, tables, each customer's satisfaction/order status and available staff tasks.
- [ ] End-of-turn display shows served count, unhappy departures, revenue, paid average and turns used.
- [ ] Paid average is the mean of satisfaction snapshots at payment, not of all customers. Before any payment display N/A.
- [ ] Victory only if paid >= 4 AND revenue >= 4500 cents AND paid mean >= 60, after turn 18.
- [ ] Each individual failed target produces defeat; threshold equality counts as success.
- [ ] Customer arriving on turn 16 cannot pay by turn 18 under legal timing.

## Required real run evidence
| Run | Input script/path | Actual paid/revenue/average | Actual result | Verified commit |
| --- | --- | --- | --- | --- |
| Victory | Pending | Pending | Pending | Pending |
| Defeat | Pending | Pending | Pending | Pending |

- [ ] Save exact inputs and actual outputs for both runs; another group member reproduces them.
- [ ] Update class diagram to match the final code.
- [ ] Tag only the verified, independently playable completed commit as stage-1.
