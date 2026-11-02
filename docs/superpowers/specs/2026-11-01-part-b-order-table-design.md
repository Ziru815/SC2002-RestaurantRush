# Part B Order and Table Design

## Intent

Complete the Stage 1 Part B domain so tables and orders enforce their own legal state transitions while Parts C and D coordinate cross-object updates.

## Goals

- Seat only a valid queued customer at a free table.
- Keep a table occupied until its customer is PAID or LEFT.
- Advance an order by exactly one preparation unit per successful cook action.
- Prevent service on the same turn an order becomes READY.
- Permit only the documented SERVED, PAID, and CANCELLED transitions.
- Preserve the price captured when the order is created.

## Non-goals

- Do not implement Waiter, Chef, Cashier, Restaurant, or controller behavior.
- Do not rename existing classes, methods, parameters, packages, or enum values.
- Do not make Table and Customer call each other; Part C coordinates both sides.

## Approach

Use the existing `Table`, `Order`, and `OrderStatus` interfaces and implement only their `TODO[B]` methods. Each method validates all preconditions before mutation and throws `IllegalArgumentException` for missing/invalid arguments or `IllegalStateException` for illegal lifecycle transitions. This keeps Part B independently testable and avoids coupling or recursive updates.

Alternatives rejected: moving two-sided synchronization into Part B would conflict with the documented Waiter/Cashier contracts, while a generic state-machine abstraction would add unnecessary Stage 1 complexity.

## Required behavior

- `Table.seat(Customer)`: reject null, occupied tables, non-QUEUED customers, and customers already linked to a table; otherwise set only the table occupant.
- `Table.release()`: free-table no-op; otherwise require occupant state PAID or LEFT before clearing the occupant.
- `Order.advance(int)`: require a positive turn and PLACED/PREPARING status, add exactly one unit, move to PREPARING until complete, then READY and record `readyTurn` once.
- `Order.isServable(int)`: true only when READY and `turn > readyTurn`.
- `Order.markServed()`: READY to SERVED only.
- `Order.markPaid()`: SERVED to PAID only.
- `Order.cancel()`: PLACED/PREPARING/READY to CANCELLED; reject SERVED, PAID, and repeated cancellation.

## Verification

Add focused real-domain tests for table validation/release, one- and two-unit preparation, turn timing, terminal-state protection, cancellation, and locked pricing. Run the Part B tests, Part A tests, scaffold tests, compilation with warnings as errors, and finally the controller suite to identify only the expected remaining Part C dependency.
