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
**Current phase: Phase 1, in progress. 1.1 and 1.2 done (tested in-game 2026-10-25). 1.3 done (unit tests only; nothing to see in-game until 1.4). Next step: 1.4.**

Phase 1 steps (each one ends with the user testing it in the dev client):
1. **1.1 Empty sidebar panel.** A Fellowship icon in the sidebar that opens a panel with a title. No game logic.
2. **1.2 Count XP gains.** Listen for XP changes, skip the login snapshot, and show "XP gained this session" in the panel.
3. **1.3 Level curve.** Decide the numbers, then write a small class that turns total XP into level + progress, with unit tests.
4. **1.4 Show the Adventurer.** The panel shows the Adventurer's level and an XP bar, fed by 1.2 and 1.3.
5. **1.5 Save per account.** Store the Adventurer's XP per OSRS account so it survives restarts, switching accounts and turning the plugin off and on. Progress must never be lost; this is a long-term game. (The 1.2 session counter is temporary and gets replaced.)

## Running & environment notes
- Run the dev client with `.\gradlew.bat run`, or IntelliJ → Gradle panel → fellowship → Tasks → other → `run`.
- The dev client shares the user's `.runelite` profile, so their other hub plugins load too. Warnings from those plugins aren't ours.
- Log in through the Jagex icon in the dev client sidebar (built-in, no setup).
- Fixed 2026-10-25: an incomplete `~/.m2/repository/org/lwjgl` cache broke `run` (missing `natives-linux-arm64`). It was moved to the Recycle Bin. If it happens again, check `~/.m2` first rather than editing `build.gradle`.
- Git is at `C:\Program Files\Git\cmd\git.exe` (it may not be on PATH in older shells). The code isn't pushed to GitHub yet.

| Phase | Scope | Status |
|---|---|---|
| 1 | Sidebar panel. One "Adventurer" earning Fellowship XP 1:1 from all OSRS XP. Fellowship's own level curve. Show level + XP bar. Save progress per OSRS account. | In progress |
| 2 | Level cap 10. At 10, evolve Adventurer into Knight, Archer or Wizard. | Not started |
| 3a | One timed expedition. A character who is away earns no OSRS-based XP. On completion: XP + 100 coins. | Not started |
| 3b | Recruit Adventurers for 100 coins each. Team cap of 3. Active character gets 100% XP, inactive characters get 10%. | Not started |
| 4–5 | Parked, to be redesigned after 3b ships. | Parked |

## Decisions
- **XP tracking:** count only XP gained while RuneLite is running. On login RuneLite reports *total* XP, so only count gains after that first snapshot.
- **Level curve:** Fellowship uses its own curve, not the OSRS table (at 1:1, the OSRS table hits level 10 at 1,154 XP). Starting point for playtesting (picked 2026-10-25): level 2 costs 6,000 XP, and each level after costs 25% more, so level 10 needs 154,814 XP total (roughly 8h for a low-level player, 1.5h for a fast endgame player). Max level is 10 for now. Must stay fair to low-level players. The numbers are constants in `LevelCurve.java`.
- **Expedition timers:** real time. Store the start time, so expeditions finish even while logged out.
- **Active character on an expedition:** allowed. Until a new active character is picked, nobody gets 100% XP and everyone else stays at 10%.
- **Class bonuses (2x XP):** Archer gets Ranged. Wizard gets Magic. Knight gets Attack + Strength. Defence stays 1x because Magic and Ranged also give Defence XP.

## Open questions
- What happens after evolving at level 10? Is there a new level cap? (Needed for Phase 2.)
- Hitpoints XP: confirm it's 1x for every class.
- Expedition length and XP reward. (Needed for 3a.)

## Ideas Parking Lot (not now)
- Characters with different XP rules: combat-only, skilling-only, or skill multipliers.
- Area-based XP, e.g. a vampire who only gains XP in Morytania.
- More expeditions, item rewards, and RNG-based unlocks.
- Second class advancement, MapleStory-style (e.g. Wizard → Battle Mage / Cleric / Arcane Wizard). Possible answer to "what happens after level 10".
- Many unlockable characters, some hard or rare, each with its own unique way to unlock.
- Gear (weapons etc.) from expeditions and other sources. Design not figured out yet.
- Fellowship Guild level, built from all characters' levels, so switching characters still feels like progress. It could gate expeditions and characters.
- Phases 4–5 redesign.
- Online features (leaderboards, trading). Risky under the rules, so maybe never.
