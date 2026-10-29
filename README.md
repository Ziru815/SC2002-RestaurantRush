# SC2002 Restaurant Rush

Stage 1 **skeleton**, prepared for a four-person group. Java 17, command line, no third-party libraries or network access required at runtime.

**Current status:** compiles and launches an integrated preview. The actual turn simulation is not implemented. `start` reports the controller TODO without advancing time or producing a game result. This is not the finished `stage-1` submission.

## Quick start

Install a JDK 17 or newer. On macOS/Linux, open Terminal in this project folder:

```sh
sh scripts/run.sh
```

Try `menu`, `status`, `tasks`, `help`, then `quit`. Use `sh scripts/run.sh --check` for a non-interactive wiring check.

```sh
sh scripts/test.sh
```

This runs scaffold smoke tests, not the completed-game acceptance tests. No Maven/Gradle setup is needed. The scripts also support runtimes with the compiler module but no separate `javac` executable.

In IntelliJ IDEA or VS Code, use JDK 17, mark `src/main/java` as the source root, and run `restaurantrush.Main`. Test source root: `src/test/java`. On Windows, an IDE is the easiest option; alternatively run the shell scripts in Git Bash with a JDK installed.

## Start here

- [四人分工与接口约定 / Team guide](docs/TEAM_GUIDE.md)
- [完成情况与待做事项 / Implementation status](docs/IMPLEMENTATION_STATUS.md)
- [Stage 1 acceptance checklist](docs/STAGE1_ACCEPTANCE.md)
- [Class diagram source](docs/class-diagram-stage1.mmd)

All Java classes use the package `restaurantrush`. Search for `TODO[A]`, `TODO[B]`, `TODO[C]` or `TODO[D]` to find your work. Unfinished business methods deliberately throw `UnsupportedOperationException`; do not replace them with dummy values or empty bodies just to make a test green.

## Base setting to preserve

18 full turns, two tables, one Waiter/Chef/Cashier. Six arrivals on turns 1, 4, 7, 10, 13 and 16. Salad $9.00 / 1 preparation unit, Burger $15.00 / 1, Pasta $18.00 / 2. Regular waiting loss is 8, starting from 100 and clamped at 0. Customer food choice will cycle by ID: Salad, Burger, Pasta, repeat. The manager controls priorities, not food choice.

Victory is checked only after turn 18 and requires all three: at least four paid customers, paid revenue at least $45.00, and average satisfaction recorded on payment at least 60. The payment flow and timing are not implemented yet; see the checklist before claiming compliance.

## Repository and stage evidence

Repository: https://github.com/Ziru815/SC2002-RestaurantRush

Work on feature branches, run tests and merge reviewed changes. Once the complete Stage 1 rules and reproducible victory/defeat runs are verified, tag that runnable commit `stage-1`. Do not tag this scaffold as the completed stage. The remaining course work also includes a report, final Stage 3 class diagram, sequence diagram, recorded video and course declarations.

| Transition | Current evidence |
| --- | --- |
| Stage 1 | Skeleton only; no completion tag or victory/defeat run yet |
| 1 → 2 | Not started. Later record new/changed/deleted files, reasons and compare link |
| 2 → 3 | Not started. Later record new/changed/deleted files, reasons and compare link |

This initial scaffold was prepared with GAI assistance. All members should review, implement and understand their own contributions, and complete the required course declarations accurately. No test results or development history should be presented as student work that has not occurred.
