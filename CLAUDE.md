# Fellowship — RuneLite Plugin

A side game that runs inside RuneLite. You build a team of characters who level up from your real OSRS XP, then send them on expeditions for Fellowship XP, coins and items. Similar in spirit to the TCG plugin.

RuneLite's official coding rules for AI helpers (from the plugin template), followed on every change:
@AGENTS.md

## How to work with me (the developer)
- I'm new to coding and new to Claude. I'm an ideas person with ADHD.
- Go **one step at a time**. Don't let me jump ahead to later phases.
- **Call me out** when an idea is overcomplicated or doesn't make sense.
- Teach Claude and coding best practices as we go, briefly.
- New ideas go into the **Ideas Parking Lot** below, not into the current phase.

## Rules we must follow (checked 2026-10-25)
Sources: Plugin Hub README, RuneLite "Rejected or Rolled Back Features" wiki, Jagex third-party client guidelines.
- **All rewards are fictional.** Fellowship XP, coins and items exist only in the plugin and never touch the real game.
- **Read-only.** The plugin never sends actions to the game server and adds no menu entries that do.
- **Local only.** No servers, leaderboards or sharing player data over the internet. Online features are "someday" and would need a fresh rules check.
- **RNG unlocks are OK** as an exception (it's OSRS). Most progress comes from effort, and nothing ever costs real money.
- **Tech rules:** Java only (no Kotlin/Scala). Public GitHub repo. BSD 2-Clause license. Avoid extra dependencies. No reflection, JNI, `Thread.sleep`, `Runtime` or `HttpURLConnection`. Don't create your own `Gson`/`OkHttp` instances. Don't use the `net.runelite` package name. Load resources with `getResourceAsStream`.
- Re-check the rejected-features wiki before submitting, since it changes.

## Roadmap
**Current phase: not started. Tools are set up (Java 11, IntelliJ, Git; GitHub user `DilzOSRS`). Next step is creating the project from the RuneLite plugin template.**

| Phase | Scope | Status |
|---|---|---|
| 1 | Sidebar panel. One "Adventurer" earning Fellowship XP 1:1 from all OSRS XP. Fellowship's own level curve. Show level + XP bar. Save progress per OSRS account. | Not started |
| 2 | Level cap 10. At 10, evolve Adventurer into Knight, Archer or Wizard. | Not started |
| 3a | One timed expedition. A character who is away earns no OSRS-based XP. On completion: XP + 100 coins. | Not started |
| 3b | Recruit Adventurers for 100 coins each. Team cap of 3. Active character gets 100% XP, inactive characters get 10%. | Not started |
| 4–5 | Parked, to be redesigned after 3b ships. | Parked |

## Decisions
- **XP tracking:** count only XP gained while RuneLite is running. On login RuneLite reports *total* XP, so only count gains after that first snapshot.
- **Level curve:** Fellowship uses its own curve, not the OSRS table (at 1:1, the OSRS table hits level 10 at 1,154 XP).
- **Expedition timers:** real time. Store the start time, so expeditions finish even while logged out.
- **Active character on an expedition:** allowed. Until a new active character is picked, nobody gets 100% XP and everyone else stays at 10%.
- **Class bonuses (2x XP):** Archer gets Ranged. Wizard gets Magic. Knight gets Attack + Strength. Defence stays 1x because Magic and Ranged also give Defence XP.

## Open questions
- What happens after evolving at level 10? Is there a new level cap? (Needed for Phase 2.)
- Hitpoints XP: confirm it's 1x for every class.
- Level curve numbers: how much XP per level? (Needed for Phase 1.)
- Expedition length and XP reward. (Needed for 3a.)

## Ideas Parking Lot (not now)
- Characters with different XP rules: combat-only, skilling-only, or skill multipliers.
- Area-based XP, e.g. a vampire who only gains XP in Morytania.
- More expeditions, item rewards, and RNG-based unlocks.
- Phases 4–5 redesign.
- Online features (leaderboards, trading). Risky under the rules, so maybe never.
