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
- Before launch, all test saves get wiped. The one-time migration from the old single-character save (in `FellowshipPlugin.moveOldSaveIntoTeam`) can be deleted then, since no real player will have the old format.

## Roadmap
**Current phase: none. Phases 1–4 are all done (every step tested in-game 2026-10-25). Next: playtest, then design Phase 5 (gear and loot) before building it. The compact roster / UI redesign is also waiting.**

Phase 4 steps, all done (for in-game testing, Guild levels and hires were temporarily made cheap):
1. **4.1 Guild XP and level.** +1 Guild XP whenever any character levels up, and +1 per expedition collected. The panel shows "Fellowship Guild: Level 1 (5 / 20 XP)". Saved per account. Unit tests for the Guild curve.
2. **4.2 Hire through the Guild.** Remove the team cap of 3. Each Guild level above 1 gives one hire, and each hire still costs 100 coins. The Recruit button becomes "Hire Adventurer (100 coins)" and shows how many hires are available. Hires can be banked.

Before teams grow past about 4, the panel needs a compact roster (big cards won't fit in the narrow sidebar). Do it in the UI redesign pass, or as its own step when needed.

Phase 3b steps, all done (for in-game testing, recruits were temporarily made cheap):
1. **3b.1 Team save format.** Save the team as a list of characters (still just one) instead of single-character keys, and move the existing save into it once. Nothing visible changes; the current character must keep all its progress.
2. **3b.2 Recruit.** A "Recruit Adventurer (100 coins)" button, disabled without enough coins or when the team has 3. Recruits are numbered ("Adventurer 2", "Adventurer 3") and get their own card with their own expedition and evolve buttons. For now the first character is active (100% XP) and recruits get 10%.
3. **3b.3 Switch active.** A "Make active" button on each card, and the active card is clearly marked. If the active character is away on an expedition, nobody gets 100% until another is made active.

Phase 3a steps, all done (for in-game testing, the expedition was temporarily made 1 minute long):
1. **3a.1 Coins.** A coin balance, saved per account and shown in the panel. Starts at 0.
2. **3a.2 Send on an expedition.** A button sends the character on "Explore Lumbridge Swamp Caves". The start time is saved per account, the card shows "Away, back in 29m", and the character earns no OSRS-based XP while away. Survives restarts.
3. **3a.3 Collect.** After 30 minutes the card says the character is back, with a Collect button that gives 1,000 XP and 10–50 coins.

Phase 2 steps, all done (for in-game testing, levels were temporarily made cheap so level 10 was quick to reach):
1. **2.1 Curve to 99.** Extend `LevelCurve` past level 10 with gentler growth, up to 99. Unit tests only.
2. **2.2 Level 10 cap.** The Adventurer stops at level 10. XP earned at the cap is wasted, and the panel says it's ready to evolve.
3. **2.3 Evolve.** At level 10 the panel offers Knight, Archer or Wizard, with a confirm step since the choice is permanent. The class is saved per account and shown on the card, the character resets to level 1, and the cap goes up to 99.
4. **2.4 Class XP bonuses.** 2x XP in the class's skills (see Decisions).

Phase 1 steps, all done (each one ended with the user testing it in the dev client):
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
- Builds can crash with "Native memory allocation failed" when Windows is low on memory (e.g. during a Premiere export). It's not a code problem; close big programs and retry.
- Git is at `C:\Program Files\Git\cmd\git.exe` (it may not be on PATH in older shells). The code is on GitHub at https://github.com/DilzOSRS/fellowship-plugin (public, `origin`, first pushed 2026-10-25).

| Phase | Scope | Status |
|---|---|---|
| 1 | Sidebar panel. One "Adventurer" earning Fellowship XP 1:1 from all OSRS XP. Fellowship's own level curve. Show level + XP bar. Save progress per OSRS account. | Done |
| 2 | Level cap 10. At 10, evolve Adventurer into Knight, Archer or Wizard. | Done |
| 3a | One timed expedition. A character who is away earns no OSRS-based XP. On completion: 1,000 XP + 10–50 coins. | Done |
| 3b | Recruit Adventurers for 100 coins each. Team cap of 3. Active character gets 100% XP, inactive characters get 10%. | Done |
| 4 | Fellowship Guild leveling and an unlimited roster. Guild levels give hires; hires still cost coins. | Done |
| 5 | Gear and loot, designed together before any building. Loot comes from varied OSRS achievements (e.g. finishing a Slayer task: 10% chance of a loot box), not from grinding XP. Gear should make playing one character for a long time worthwhile. | Needs design |
| Ongoing | More characters (some rare, with unique ways to unlock) and more expeditions, added once the systems exist. | Later |

## Decisions
- **XP tracking:** count only XP gained while RuneLite is running. On login RuneLite reports *total* XP, so only count gains after that first snapshot.
- **Level curve:** Fellowship uses its own curve, not the OSRS table (at 1:1, the OSRS table hits level 10 at 1,154 XP). Current playtest values (2026-10-25): level 2 costs 1,500 XP, and each level after costs 50% more, so level 10 needs 112,331 XP total (roughly 5.5h for a low-level player, 1h for a fast endgame player). The first try (6,000 XP, +25%) felt too slow at level 1. Max level is 10 for now. Must stay fair to low-level players. The numbers are constants in `LevelCurve.java`. (Level caps: see below.)
- **Expedition timers:** real time, same speed online and offline. Store the start time, so expeditions finish even while logged out.
- **Expedition 1 (decided 2026-10-25):** "Explore Lumbridge Swamp Caves". 30 minutes. Rewards 1,000 XP (fixed for now; the level cap still applies, and class bonuses don't) plus a random 10–50 coins, about 3–4 runs per 100-coin recruit. The player clicks Collect when it's done. Early expeditions should be short.
- **Coins:** shown as "coins", not "GP", so nobody mistakes them for real gold.
- **Team (decided 2026-10-25, Phase 3b):** a cap of 3 in total. Recruits are numbered ("Adventurer 2"); custom names can come later. Coins belong to the account and are shared. Phase 4 replaces the cap (see Roster below).
- **Inactive XP:** 10% of OSRS XP for every inactive character, however big the team (the user's choice for Phase 4; watch in playtesting whether big teams level too fast). Class bonuses stack on top (an inactive Wizard gets 20% of Magic XP). Fractions of an XP are carried over in memory so small drops still count; at most under 1 XP per character is lost on a restart.
- **Roster (Phase 4, decided 2026-10-25):** no slot limit, but getting characters is hard: building a big team should be a big grind, and every new character should feel meaningful (which also makes choosing classes matter). Each Guild level above 1 gives one hire; hiring still costs coins (100 for now), so coins stay useful. Special unlocks later (e.g. Beast Master from a pet drop) are extra and don't use hires.
- **Fellowship Guild (Phase 4, decided 2026-10-25):** +1 Guild XP per character level-up (re-leveling after evolving counts again, which rewards job advancing) and +1 per expedition collected. Level 2 needs 20 XP and level 3 needs 50; each level after costs 10 more XP than the one before (level 4 at 90, level 5 at 140). Tune after playtesting.
- **Active character on an expedition:** allowed. Until a new active character is picked, nobody gets 100% XP and everyone else stays at 10%.
- **Evolving (Phase 2), decided 2026-10-25:** a job advancement resets the character to level 1 (XP back to 0), with a higher level cap. Why: a fresh start feels exciting, and with many characters it keeps an even spread of levels instead of a pile of high-level ones. (This replaced an earlier Pokémon-style "keep your level" plan. Don't switch again until it has been playtested.)
- **Level cap:** the Adventurer is capped at 10. XP earned at the cap is wasted until it evolves (the user's choice, to push players to evolve). Evolved classes go up to 99. There's no reason for a lower cap while levels don't unlock anything yet.
- **Curve past 10:** each level costs 5% more than the last, so level 99 needs about 61.4M XP (roughly 3,000h for a low-level player, 600h for a fast endgame player). The user chose a steady, easy-to-track rate; hefty grinds are fine until levels unlock more. Playtest and adjust.
- **Class bonuses (2x XP):** Archer gets Ranged. Wizard gets Magic. Knight gets Attack + Strength. Defence stays 1x because Magic and Ranged also give Defence XP. Hitpoints is 1x for every class (confirmed 2026-10-25). The Adventurer has no bonuses.

## Open questions

## Ideas Parking Lot (not now)
- Characters with different XP rules: combat-only, skilling-only, or skill multipliers.
- Area-based XP, e.g. a vampire who only gains XP in Morytania.
- More expeditions, item rewards, and RNG-based unlocks. Expeditions should get RNG item loot at some point.
- Expedition speed: 100% while playing, 50% while logged out. Parked because it's complex and punishes sending characters off before logging out.
- Expedition XP that scales with level (e.g. half a level) instead of a fixed 1,000, if fixed XP stops mattering at high levels.
- Second class advancement, MapleStory-style (e.g. Wizard → Battle Mage / Cleric / Arcane Wizard). Possible answer to "what happens after level 10".
- Many unlockable characters, some hard or rare, each with its own unique way to unlock.
- Gear (weapons etc.) from expeditions and other sources. Now Phase 5.
- Guild levels could also gate expeditions and rare characters, not just hires.
- Character art: a picture or model for each class instead of text only.
- UI redesign pass: the panel works but looks plain. Do it once the core concepts settle, so it isn't redone for every new feature. Game logic is already separate from the panel code (`Team`/`FellowshipCharacter` vs `FellowshipPanel`/`CharacterCard`), so a redesign shouldn't touch the rules. Limits: the sidebar is narrow (about 225px) and built with Swing.
- Custom names for recruits (typed in when recruiting).
- Beast Master: a 10% chance to unlock when you get a pet drop in OSRS (read the drop message only). Example of unlocks from rare game events, like an advancement from a rare raid item.
- Online features (leaderboards, trading). Risky under the rules, so maybe never.
