# Stage 3

**Student:** Ziru · **Course:** SC2002 · **Tool:** OpenAI Codex

The questions and replies below are condensed English paraphrases, grouped by topic and dated by discussion.

**9–11 November 2026 · Shared interfaces, code drafts and pricing checks**

**Ziru:** Set up the shared interfaces so the four parts fit together. Help me draft the customer and combo code for Part A, then the pricing code for Part B. Can you check how the two parts work together?

**Codex:** I prepared the shared interfaces, drafted the customer and pricing changes, and checked the resulting meal choices and bills. For Part A, customer 5 selects a combo when the menu offers it. For Part B, `HappyHourPolicy` starts with the customer’s quote and applies the promotion to a new order. A VIP Burger goes from $15.00 to $13.50 and then to $10.80. `Order` stores that final price, so the Cashier still collects $10.80 after Happy Hour ends.

**13–14 November 2026 · Tracing a live order through the code**

**Ziru:** In the terminal I only type `order 5`. I never type the dish name. Why does customer 5 get a Burger in Stage 2 and a combo in Stage 3?

**Codex:** The command tells the Waiter whose order to take. The customer’s `chooseMeal(menu)` method decides what to order. For customer 5 it checks `menu.hasCombo()`. That check succeeds with the Stage 3 menu. With the Stage 2 menu, the method falls back to the repeating meal cycle, which gives customer 5 a Burger.

**Ziru:** Where should I show the class diagram so it answers a real question? The terminal displays one order, but the combo contains two dishes.

**Codex:** Show the diagram after the terminal displays `Combo ($24.30)`. The inheritance arrow from `ComboMeal` to `MenuItem` explains how an order can accept the combo as one item. The combo’s references to Salad and Pasta explain the work inside that order. Their prices add to $27.00, and the combo’s 10% discount gives $24.30. Their preparation units add to three, so after the first `cook 5`, the terminal shows `1/3`.
