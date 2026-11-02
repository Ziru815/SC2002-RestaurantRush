# Part B Order and Table Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement and verify the Stage 1 table and order lifecycle required by the existing customer, staff, restaurant, and controller contracts.

**Architecture:** `Table` owns only its occupant reference and `Order` owns only preparation/billing state. Parts C and D remain responsible for coordinating updates across Table, Customer, Order, and Restaurant, so Part B methods validate first and mutate one object only.

**Tech Stack:** Java 17, repository shell build scripts, plain Java assertion-style test runners, no external dependencies.

**Spec:** `docs/superpowers/specs/2026-11-01-part-b-order-table-design.md`

## Global Constraints

- Keep `package restaurantrush` and all existing public signatures and enum constants unchanged.
- Work only on Part B production files and a dedicated Part B test file.
- Validate before mutation; illegal actions must preserve previous state.
- Money remains integer cents; an Order locks the item price at construction.
- A READY order cannot be served until a later turn.

## Review Focus

- Null, occupied-table, already-seated, and non-QUEUED seating attempts preserve both objects.
- Invalid or repeated preparation never changes units, status, or `readyTurn`.
- READY on turn N is not servable until a turn greater than N.
- Terminal and cancelled orders reject further lifecycle actions without mutation.
- A table is not released during service/eating/readiness; only PAID or LEFT permits release.

## File Structure

- Modify `src/main/java/restaurantrush/Table.java`: table-side seating and release rules.
- Modify `src/main/java/restaurantrush/Order.java`: preparation, serving, payment, and cancellation rules.
- Keep `src/main/java/restaurantrush/OrderStatus.java`: its existing Stage 1 enum is already complete.
- Add `src/test/java/restaurantrush/OrderTableTests.java`: focused Part B lifecycle scenarios.

## Task 1: Table lifecycle

**Interfaces:** consumes `Customer.getState()` and `Customer.getTable()`; produces `Table.seat(Customer)` and `Table.release()` for Waiter, Cashier, and Restaurant.

- [ ] Add failing tests for successful table-side seating, null/occupied/invalid-customer rejection without mutation, free-table release, blocked premature release, and PAID/LEFT release.
- [ ] Run the focused test runner and confirm failure comes from `TODO[B] Table.seat` or `TODO[B] Table.release`.
- [ ] Implement both methods with all validation before mutation.
- [ ] Re-run focused tests and confirm the table scenarios pass.
- [ ] Commit the table behavior and tests.

## Task 2: Preparation and service timing

**Interfaces:** consumes immutable `MenuItem.getPrepUnits()` and produces `Order.advance(int)`, `Order.isServable(int)`, and `Order.markServed()` for Chef and Waiter.

- [ ] Add failing tests for Salad/Burger one-unit readiness, Pasta two-unit progression, positive turns, one-unit-per-call behavior, ready-turn preservation, later-turn service eligibility, and READY-only service.
- [ ] Run the focused tests and confirm failure comes from the matching `TODO[B]` methods.
- [ ] Implement minimal preparation and service transitions.
- [ ] Re-run focused tests and confirm these scenarios pass.
- [ ] Commit preparation and service behavior.

## Task 3: Payment, cancellation, and locked price

**Interfaces:** produces `Order.markPaid()` and `Order.cancel()` for Cashier and Restaurant; preserves `getLockedPriceCents()`.

- [ ] Add failing tests for SERVED-only payment, cancellation from each unfinished status, cancellation rejection from SERVED/PAID/CANCELLED, terminal-state protection, and price locking after construction.
- [ ] Run the focused tests and confirm failure comes from the matching `TODO[B]` methods.
- [ ] Implement payment and cancellation transitions.
- [ ] Re-run focused tests and confirm all Part B scenarios pass.
- [ ] Commit payment/cancellation behavior.

## Task 4: Cross-part verification and handoff

- [ ] Run `sh scripts/build.sh` and require a warning-free Java 17 build.
- [ ] Run `java -cp out/main:out/test restaurantrush.OrderTableTests` and require all Part B cases to pass.
- [ ] Run `java -cp out/main:out/test restaurantrush.CustomerTests` and require all Part A cases to pass without reflection being necessary for Part B transitions.
- [ ] Run `sh scripts/test.sh` and report any scaffold expectations that are obsolete because Part B is now implemented.
- [ ] Run `java -cp out/main:out/test restaurantrush.ControllerTests`; report the expected Part C TODO failure separately rather than claiming full Stage 1 completion.
- [ ] Check the final diff contains only intended Part B code, tests, and design/plan documentation; then push `work/order-table`.
