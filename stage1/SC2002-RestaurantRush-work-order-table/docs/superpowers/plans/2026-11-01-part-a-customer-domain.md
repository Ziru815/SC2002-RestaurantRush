# Part A Customer Domain Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Complete the Stage 1 Part A customer and menu behavior so the updated Part D controller can run deterministic games.

**Architecture:** Keep all customer state transitions inside `Customer`; `GameController` only sequences calls and `Waiter`/`Cashier` validate their own operations. Keep the existing deterministic three-item menu and use customer ID modulo three for food selection. Do not introduce Stage 2 or Stage 3 types.

**Tech Stack:** Java 17+, repository shell build/test scripts, plain Java assertion-style test mains.

**Spec:** `docs/superpowers/specs/2026-10-29-stage1-skeleton-design.md` and `docs/STAGE1_ACCEPTANCE.md`

## Global Constraints

- Only `Customer`, `RegularCustomer`, `CustomerState`, `Menu`, and `MenuItem` belong to Part A.
- Stage 1 contains Regular customers only; waiting loss is exactly 8 and satisfaction never drops below 0.
- Customer IDs deterministically choose Salad, Burger, Pasta, then repeat.
- A customer served on turn N may finish eating no earlier than turn N+1 and may pay no earlier than one turn after becoming READY_TO_PAY.
- Part A performs no CLI input/output and does not mutate `Restaurant` collections.
- Preserve Part D's public method signatures and exception-driven retry contract.

## Review Focus

- Null menu passed to `chooseMeal` must be rejected without changing customer state.
- Seating with null table, seating twice, or seating outside QUEUED must be rejected.
- Orders for another customer and repeated orders must be rejected.
- Same-turn eating/payment transitions must remain blocked.
- Leaving or paying must clear the customer-side table reference without hiding the order history.

---

### Task 1: Waiting satisfaction and deterministic meal choice

**Files:**
- Create: `src/test/java/restaurantrush/CustomerTests.java`
- Modify: `src/main/java/restaurantrush/Customer.java`

**Interfaces:**
- Consumes: `Menu.find(String)`, `MenuItem`, `CustomerState`
- Produces: `loseWaitingSatisfaction()` and `chooseMeal(Menu)` used by Part D and Part C

- [ ] Write tests for Regular waiting loss, floor at zero, no loss after service, ID-to-menu cycle, and null menu rejection.
- [ ] Run `sh scripts/build.sh && java -cp out/main:out/test restaurantrush.CustomerTests`; expect failure from `TODO[A]`.
- [ ] Implement only `loseWaitingSatisfaction()` and `chooseMeal(Menu)`.
- [ ] Re-run the focused tests and the repository test script; expect all implemented cases to pass.

### Task 2: Seating and order attachment

**Files:**
- Modify: `src/test/java/restaurantrush/CustomerTests.java`
- Modify: `src/main/java/restaurantrush/Customer.java`

**Interfaces:**
- Consumes: `Table`, `Order.getCustomer()`
- Produces: `seat(Table)` and `placeOrder(Order)` used by `Waiter`

- [ ] Add tests for valid seating, null/repeated seating, valid order attachment, foreign order, and repeated order.
- [ ] Run the focused tests; expect failure from the seating/order TODOs.
- [ ] Implement `seat(Table)` and `placeOrder(Order)` with validation before mutation.
- [ ] Re-run focused and repository tests.

### Task 3: Service, eating, payment, and departure lifecycle

**Files:**
- Modify: `src/test/java/restaurantrush/CustomerTests.java`
- Modify: `src/main/java/restaurantrush/Customer.java`

**Interfaces:**
- Consumes: `Order.getStatus()`, `CustomerState`, turn numbers
- Produces: `markServed(int)`, `finishEating(int)`, `canPay(int)`, `markPaid()`, and `leave()` used by Parts C and D

- [ ] Add tests for invalid and valid service, same-turn eating block, later-turn readiness, same-turn payment block, paid transition, legal departure, and illegal departure.
- [ ] Run focused tests; expect failure from lifecycle TODOs.
- [ ] Implement the five lifecycle methods without mutating `Table`, `Order`, or `Restaurant` beyond checking the required order status.
- [ ] Re-run focused tests, `sh scripts/test.sh`, and `java -cp out/main:out/test restaurantrush.ControllerTests`.

### Task 4: Review and GitHub handoff

**Files:**
- Review: all five Part A production files
- Review: `src/test/java/restaurantrush/CustomerTests.java`

**Interfaces:**
- Produces: a Part A branch based on `work/controller`, ready for group review

- [ ] Confirm no `TODO[A]` remains and no Part B/C/D file changed.
- [ ] Run clean build and all available tests.
- [ ] Inspect the diff, commit Part A and tests, and push the branch without modifying `main` or `work/controller`.
