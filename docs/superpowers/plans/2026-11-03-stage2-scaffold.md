# Stage 2 Scaffold Implementation Plan

> For agentic workers: use superpowers:executing-plans for execution. This scaffold was directly authorized; the four feature implementations are assigned to the team, not to this setup task.

**Goal:** Preserve a runnable Stage 1 snapshot and prepare four bounded Stage 2 work areas.
**Architecture:** Extend Customer polymorphically; Order locks the quoted price; employees use existing generic operations; D controls arrival setup and CLI wiring.
**Tech Stack:** Java 17+, POSIX shell, no third-party runtime dependencies.
**Spec:** ../specs/2026-11-03-stage2-scaffold-design.md

## Global constraints
18 turns; 2 tables; 6 arrivals at 1/4/7/10/13/16; win requires paid >=4 AND revenue >=4500 cents AND paid satisfaction mean >=60. All three stages independently runnable. Stage 2 waiting losses: Regular 8, VIP 5, Critic 12. VIP pays 90%; others full price. No type-dispatch chains in turn logic. No Stage 3 features here.

## Review focus
- Distinguish passing Stage 1 regression from pending Stage 2 implementation.
- Keep an invalid action from consuming any employee's task.
- Charge the locked quote once, never discount twice or record unpaid revenue.
- Keep service/eating/payment on their legal turns for every subtype.
- Keep exactly six arrivals and deterministic Critic behavior.

## Scaffold setup (this task)
- [x] Verify main, run 40 baseline tests and both full replay inputs; preserve stage-1.
- [x] Add tests for default customer pricing/label contracts; observe compile failure before adding methods.
- [x] Add default extension methods and VIP/Critic/factory stubs; leave business TODOs explicit.
- [x] Add independent A/B/C/D test entry points and a Stage 2 runner that returns nonzero while pending.
- [x] Add team guide, acceptance checks, truthful current status, Stage 1 replay evidence and submission checklist.
- [x] Verify baseline, extension contracts, replay outputs and deliberate pending Stage 2 status; inspect diff.
- [ ] Publish scaffold PR and four owner branches, preserving main until another person reviews/merges.

## Team handoff
A/B/C/D implement the individual checklists in STAGE2_TEAM_GUIDE.md. Each writes a behavioral test, observes the relevant failure, implements the smallest change and reruns their suite plus regression tests. Add new commits with the real author's identity. No one merges their own PR. Each person records their actual AI assistance and writes their own reflection.

## Scaffold verification record

3 November 2026: 40 existing tests and 5 default-interface checks pass; both Stage 1 replay summaries match the frozen baseline. All four Stage 2 suites report pending and the Stage 2 runner exits 1, as intended. Independent review found no important actionable issues; this is approval of scaffold scope only.
