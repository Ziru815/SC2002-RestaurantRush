# Restaurant Rush Stage 1 Skeleton Design

## Intent
Create a Java 17 command-line starting point for four students to implement Stage 1 together. This implements the class structure agreed in the conversation on 15 October, not a completed game. The user has now requested that structure as code in their existing GitHub repository.

## Approach and scope
Use plain Java and one package, `restaurantrush`, with one class per source file. This keeps the agreed class names recognizable and works offline without Maven, Gradle or downloaded dependencies. A framework would add setup without helping the required command-line game. An empty collection of files would not demonstrate integration; a completed game would remove the intended implementation work from the four students. The chosen middle ground is a compiling, integrated preview with explicit, fail-fast TODO methods.

Use the approved ConsoleUI / GameController / Restaurant structure, Customer and Staff base classes, their Stage 1 subclasses, Menu, MenuItem, Table, Order and two enums. Add Main for launching and GameConfig for the fixed base settings. Add a customer registry in Restaurant so the controller can update every customer without losing customers removed from the queue. This is an implementation choice, not a new game feature.

## Completed scaffold behavior
Launch, show fixed configuration, menu, empty pre-game state at turn 0, help and team tasks. Accept status/menu/help/tasks/start/quit. Unknown input and EOF are safe. `start` reaches an explicit TODO before changing state; it does not fabricate a playable turn or result. Models have real constructors, validated menu values, read-only collections and accessors. Staff skip/reset are usable. All unfinished transitions throw UnsupportedOperationException naming their owner.

## Required game behavior still to implement
Exactly 18 turns, two tables, one waiter/chef/cashier, arrivals at 1/4/7/10/13/16. Regular satisfaction starts at 100 and loses 8 only while waiting or seated before service. Customer selects food, using documented ID cycling Salad/Burger/Pasta. Menu: 900/1500/1800 cents; preparation: 1/1/2. One task per staff per turn; waiter before chef. Same-turn order preparation allowed, same-turn ready-and-serve forbidden. One full eating turn; payment no earlier than the turn after READY_TO_PAY; cashier chooses oldest eligible customer. Only paid revenue and satisfaction count; tables free on pay/leave. Invalid actions allow retry without consuming an action. Final victory requires paid count >= 4, revenue >= 4500 cents and paid average >= 60. N/A before any payment.

## Four owners
A: Customer, RegularCustomer, CustomerState, Menu, MenuItem. B: Table, Order, OrderStatus. C: Staff, Waiter, Chef, Cashier. D: Restaurant, GameController, ConsoleUI, Main, GameConfig. Owner D coordinates integration and everyone tests their own rules.

## Evidence and limits
Ship a smoke-test command for the scaffold and a separate, unchecked Stage 1 acceptance checklist. No VIP, Critic, Host, ComboMeal or Happy Hour. No stage-1 completion tag until the game is playable and reproducible victory and defeat runs exist. Publish to a feature branch and draft PR. Preserve main and the existing repository identity. Provide a ZIP of the same sources for download.
