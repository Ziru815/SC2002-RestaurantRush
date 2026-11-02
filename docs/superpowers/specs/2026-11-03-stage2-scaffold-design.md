# Stage 2 scaffold design

User authorization: preserve Stage 1, choose a practical branch structure, and prepare a four-person skeleton with TODOs. The user delegated routine design choices. This change does not implement the students' Stage 2 features.

## Approach
Keep one main branch and the immutable stage-1 tag at 8d7fa09ddae3053f97f9a7ebe9738f3335740133. Propose the scaffold through work/stage2-scaffold, reviewed by a teammate. Prepare four feature branches from that exact scaffold commit; merge the scaffold first, then target feature PRs at main. Use merge commits, never squash/rebase the shared foundation.

A second main or a nested copy of the project would duplicate maintenance and obscure real stage differences. Implementing all Stage 2 now would defeat the requested learning handoff. Choose a compilable scaffold with explicit unimplemented methods and separate failing Stage 2 test entry points.

## Interfaces and boundaries
- A: Customer.priceFor(MenuItem) returns integer cents; default full price. VIP overrides for 10% off. Customer.getTypeLabel() is display-only metadata. VIP/Critic extend Customer and override waitingLoss(); Critic overrides chooseMeal(Menu).
- B: Order(Customer, MenuItem) will call customer.priceFor(item) exactly once and retain the immutable result. No controller/cashier discount checks.
- C: Staff remain generic. Write Stage2StaffTests to exercise the real seat/order/cook/serve/pay workflow for each subtype and preserve task/timing guards. Modify production staff only if a demonstrated defect requires it.
- D: Stage2Arrivals.create(int id) will construct the fixed sequence Regular, VIP, Critic, Regular, Regular, Regular. Wire a --stage2 launch path while retaining the default Stage 1 run for regression; existing two-argument GameController construction remains compatible. Use a configurable arrival constructor/function rather than customer type checks in business rules.

## Team-selected defaults (not extra assignment requirements)
Critic always chooses Pasta; IDs 2 and 3 are VIP and Critic. These deterministic choices satisfy the documented preference/reproducibility requirement and must be explained by the team. Use cents and nearest-cent half-up rounding for VIP prices if needed; base menu discounts are exact cents.

## Scope and status
Preserve all Stage 1 behavior. Supply extension signatures, subtype/factory stubs, owned test entry points, branch instructions, acceptance criteria and evidence. Keep new subtypes disconnected until D integrates completed dependencies. Do not add Host, combos or Happy Hour before Stage 2 is tagged. Do not present AI-generated scaffolding as each student's completed work.

## Completion criteria
Scaffold: compiles, all existing 40 tests pass, extension-default tests pass, Stage 1 victory/defeat unchanged, Stage 2 tests clearly fail as pending. Stage 2 completion is separately defined in STAGE2_ACCEPTANCE.md and requires implemented behavior, real tests and independent review before stage-2 is created.
