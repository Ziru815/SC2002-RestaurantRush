# Stage 1

**Student:** Ziru · **Course:** SC2002 · **Tool:** OpenAI Codex

The questions and replies below are condensed English paraphrases, grouped by topic and dated by discussion.

**2–3 November 2026 · Integration and testing**

**Ziru:** The game runs now, but I want to understand the code itself. Can you walk me through what happens to one customer and show me where my part fits?

**Codex:** Follow the customer and the order separately. The customer queues, takes a seat and places an order. The Chef advances the order until the dish is ready. The Waiter can serve it on a later turn. The customer then finishes eating and becomes eligible to pay. Keeping those two states separate explains something you can see on screen: an order can already be `READY` while its customer is still waiting for food.

**Ziru:** My teammate says the seating test is stale. What was it checking, and what should we check now?

**Codex:** The old test expected `Waiter.seat` to throw `UnsupportedOperationException` because seating was originally unfinished. Now try seating someone at an occupied table. Check that the table keeps its original occupant and that the rejected command leaves the Waiter’s action available. The second check matters because the player should still be able to enter a valid command that turn.

**14 November 2026 · Revisiting Stage 1 for the presentation**

**Ziru:** Starting the demo with customers 4 and 5 feels abrupt. Can I use customers 1, 2 and 3 to explain who chooses the meal?

**Codex:** The Waiter takes the order, but calls the customer’s `chooseMeal(menu)` method to get the dish. For a regular customer, `(id - 1) % 3` produces indices 0, 1 and 2 for the first three IDs, giving Salad, Burger and Pasta. Customer 4 brings the index back to 0. Once the audience expects Salad, the Stage 2 Critic’s choice of Pasta has a clear meaning.
