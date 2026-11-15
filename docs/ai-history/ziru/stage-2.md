# Stage 2

**Student:** Ziru · **Course:** SC2002 · **Tool:** OpenAI Codex

The questions and replies below are condensed English paraphrases, grouped by topic and dated by discussion.

**3–7 November 2026 · Customer scaffold, behaviour and integration checks**

**Ziru:** A teammate pointed out that customer 3 already orders Pasta. Why does changing the Critic to customer 4 make a difference?

**Codex:** Customer 3 would order Pasta under either rule. Customer 4 normally orders Salad, so assigning the Critic role to customer 4 lets you see the overridden choice in an ordinary run. The Critic’s higher waiting loss is a separate behaviour.

**Ziru:** Help me set up the Stage 2 code skeleton, then draft the customer rules for Part A. Can you check how those rules work with Part B’s order and pricing code?

**Codex:** I drafted the VIP and Critic behaviour in the customer subclasses and checked the results against the agreed rules. `VIPCustomer.priceFor(item)` returns the VIP quote, while `CriticCustomer.chooseMeal(menu)` selects Pasta. Both subclasses override `waitingLoss()`. In Stage 2, `Order` obtains the customer’s quote once and stores it. A $15.00 Burger therefore becomes a $13.50 VIP bill. The Cashier later collects the stored amount.

**13–14 November 2026 · Explaining polymorphism for the presentation**

**Ziru:** When I show the Critic in GitHub, how do I explain inheritance, overriding and polymorphism using the actual order on screen?

**Codex:** Start at the Waiter’s call to `chooseMeal`. The Waiter has a `Customer` reference. A Critic inherits the ordinary customer lifecycle, including seating and payment, and overrides the meal-selection method. When the object at the table is a Critic, Java runs that override. Customer 4’s Pasta order is the visible result of that call.
