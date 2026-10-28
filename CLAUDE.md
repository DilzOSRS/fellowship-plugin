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
**Current phase: Phase 6, starting (the user chose to move on before finishing Phase 5). Phases 1–4b are all done (tested in-game 2026-10-25; the last wording and colour tweaks to 4b's messages weren't re-tested in-game). 5.1 and 5.2 done (tested in-game 2026-10-26). 5.3 built but NOT tested in-game: the user will test Slayer on 2026-10-27 (turn the Slayer chance up to 100% temporarily for it) and clues later. 5.4 not started. 6.1 and 6.2 done (tested in-game 2026-10-26). 6.3 (injuries), 6.4 (Skillers) and bundle pop-ups are built but NOT tested in-game: the user will playtest everything passively on 2026-10-27 (about 6 hours), from a fresh save (progress was reset 2026-10-26), including the Slayer trigger. Next: collect the playtest notes, then fix and tune.**

Phase 6 steps (each one ends with the user testing it; for testing, temporarily make the expedition short):
1. **6.1 Party expedition.** Add "Looting the Dorgeshuun Caves" (threat 8, exactly 3 characters, 2 hours). A dialog picks 3 characters who are home; they all go away together and come back together. On return: 3,000 XP each, coins and the Boss Bundle chance once. No success/fail yet.
2. **6.2 Success or failure.** Party ATK (average of ATK + half MAG) vs threat; failing gives no XP and no loot.
3. **6.3 Injuries.** Party DEF vs threat decides injuries (see the design doc); injured characters rest for 5× the expedition length, earn no XP and can't be sent. Saved.
4. **6.4 Skiller class (built 2026-10-26; rules changed by the user before building).** Skillers can be hired **from the start**: **up to 3**, **no Guild hire needed**, costing **500, then 1,000, then 2,000 coins** (doubling; saved as `skillersHired`). The Skiller works like the Adventurer: no XP bonus, level cap 10, then a class advancement into **Harvester**, **Artisan** or **Explorer** (cap 99). Their 2x XP skills are a first guess to tune after playtesting: Harvester = Woodcutting, Mining, Fishing, Farming, Hunter; Artisan = Smithing, Crafting, Fletching, Cooking, Herblore, Construction, Firemaking; Explorer = Agility, Thieving, Runecraft, Prayer, Slayer. No gear is made for them yet, so they can only wear Generic trinkets.
5. **Bundle pop-ups (built 2026-10-26).** A small overlay at the top of the game screen for 6 seconds when a bundle drops (`PopupOverlay`), alongside the chat message.

The compact roster / UI redesign is also waiting.

**UI work (decided 2026-10-26; the user chose to start it during the playtest, since UI changes don't affect the game rules):**
1. **New character card (done, looked at in-game 2026-10-26).** Built from the user's sketch: a header that's always visible (class icon, name, level, status, Make active and Collect buttons), then three `MaterialTabGroup` tabs: **Info** (XP bar, class advancement, stats), **Gear** (stats and slots) and **Expedition**. A full-height icon column was tried on paper but doesn't fit the ~189px inside a card. Class icons are in-game items drawn by RuneLite (`CharacterClass.getIconItemId`): fedora, rune full helm, coif, blue wizard hat, knife, rune axe, hammer, leather boots. Placeholders until custom art.
2. **Unlock pop-up (done, looked at in-game 2026-10-26 with a temporary test command).** `PopupOverlay` shows the class icon beside the text when hiring an Adventurer or Skiller ("Adventurer 2 joined your Fellowship!") and on a class advancement ("Unlocked Knight!"). Rare unlocks should use it too once they exist. Every pop-up slides down and fades in, plays the Grand Exchange "ding" (`SoundEffectID.GE_ADD_OFFER_DINGALING`, at the player's own sound effect volume; the Collection Log sound has no named constant), and fades out. Bundle pop-ups show a clue reward casket per bundle type (`BundleType.getIconItemId`: Generic easy, Slayer medium, Clue hard, Boss elite, Raid master).
3. **XP bar on every tab (done, looked at in-game 2026-10-26).** The Info tab keeps the big XP bar; a thin 4px bar in the header (under the status line) shows only on the Gear and Expedition tabs, and keeps its space when hidden so the card doesn't jump. Showing both at once looked bad, and text-only on Info was missed. Good for now, but the user wants to rethink how the thin bar is shown later.
4. **Later:** expeditions move off the cards into an Expedition Board under the Fellowship card (see `docs/future-design.md`), and the compact roster (one row per character, click to open its tabbed card).

**Future designs** (more expeditions, classes and gear, planned during the playtest but not scheduled) are in `docs/future-design.md`. One-line ideas still go in the Ideas Parking Lot.

**v1** (the finish of the first build: complete, maybe small, with an end-game) is scoped in `docs/v1-plan.md`. Anything not listed there is after v1. Playtest notes go in `docs/playtest-notes.md`.

Phase 4b, done: **chat messages** (only the player sees them; nothing is sent to the server) for level-ups and class advancement being ready, class advancement, a character coming back from an expedition, the rewards collected, the Guild levelling up, and a new hire. E.g. "Adventurer 2 reached level 5!"

Phase 5–6 plan (gear, loot and dangerous expeditions; structure decided 2026-10-25). Why: rare gear makes individual characters feel strong. **The full design, item table and draft numbers are in `docs/phase5-design.md`; keep it up to date.** In short:
- **Phase 5 (gear and loot):** 35 items (Knight, Archer and Wizard weapons and armour, plus Generic trinkets; one per rarity). Kept super simple for the first playtest: weapons give ATK only, armour DEF only, trinkets MAG only, and power by rarity is 1 / 3 / 6 / 10 / 15 (MAG counts half to ATK, half to DEF in Phase 6). Power comes from gear only. Class gear plus "Generic" gear anyone can use (for now only trinkets are Generic, so Adventurers can only wear trinkets). Bundles: Slayer task 10%, any raid 10%, boss kill 1%, any clue 25%, each with coins plus one item. Storage starts at 10; from Guild level 4, +5 slots per purchase for 400 coins, doubling each time. Salvage for coins. Pop-ups when bundles drop. Stats are shown but don't matter until Phase 6.
- **Phase 6 (dangerous expeditions):** Threat Level per expedition (separate from requirements). Each expedition sets its party size, and party power is the average of the members' effective ATK and DEF. ATK vs threat decides success (fail = no loot); DEF vs threat decides injuries (lower DEF more likely to be picked). Injured characters rest for 5× the expedition length with no XP. First one: Looting the Dorgeshuun Caves (threat 8, exactly 3 characters, 2 hours).
- **Phase 5 steps** (the user wants items and bundles playable for testing on 2026-10-26; for testing, temporarily make bundles drop often):
  1. **5.1 Bundles and storage.** The 35 items, the 5 bundle types and their odds, a Generic Bundle trigger (now 25% per 10,000 OSRS XP for playtesting; was 10% per 100,000 until 2026-10-26; the final balance should be about one bundle per 100,000 XP, see `docs/playtest-notes.md`), unopened bundles you can open for coins plus an item, 10-slot storage, salvage. Saved per account.
  2. **5.2 Equipping.** Weapon, Armour and Trinket slots on each card, equip from storage (class rules apply), unequip back to storage, and ATK/DEF/MAG shown on the card.
  3. **5.3 Slayer and clue triggers** (from game chat messages).
  4. **5.4 Boss and raid triggers** (check The Fractured Archive's message).
  5. **Later:** pop-ups when bundles drop, buying more storage.


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
| 4b | Chat messages for level-ups, class advancement, expeditions, Guild levels and hires. | Done |
| 5 | Gear and loot: 35 items with ATK/DEF/MAG, bundles from Slayer tasks, raids, bosses and clues, storage with salvage, pop-ups. | In progress |
| 6 | Dangerous expeditions: threat levels, parties, success or failure, injuries and resting. Looting the Dorgeshuun Caves first. Unique gear perks later. | Designed, not started |
| Ongoing | More characters (some rare, with unique ways to unlock) and more expeditions, added once the systems exist. | Later |

## Decisions
- **XP tracking:** count only XP gained while RuneLite is running. On login RuneLite reports *total* XP, so only count gains after that first snapshot.
- **Level curve:** Fellowship uses its own curve, not the OSRS table (at 1:1, the OSRS table hits level 10 at 1,154 XP). Current playtest values (2026-10-25): level 2 costs 1,500 XP, and each level after costs 50% more, so level 10 needs 112,331 XP total (roughly 5.5h for a low-level player, 1h for a fast endgame player). The first try (6,000 XP, +25%) felt too slow at level 1. Max level is 10 for now. Must stay fair to low-level players. The numbers are constants in `LevelCurve.java`. (Level caps: see below.)
- **Expedition timers:** real time, same speed online and offline. Store the start time, so expeditions finish even while logged out.
- **Expedition 1 (decided 2026-10-25):** "Explore Lumbridge Swamp Caves". 30 minutes. Rewards 1,000 XP (fixed for now; the level cap still applies, and class bonuses don't) plus a random 10–50 coins, about 3–4 runs per 100-coin recruit. The player clicks Collect when it's done. Early expeditions should be short.
- **Coins:** shown as "coins", not "GP", so nobody mistakes them for real gold.
- **Immersive wording (user preference):** player-facing text should feel in-world. Never say "evolve" to the player: use "class advancement", "Choose a path" and "has been trained in the ways of the Wizard". (Code names like `evolve()` are internal and fine.)
- **Chat messages:** start with a gold "[Fellowship]" tag; the rest uses the chatbox's normal colour so it reads on both the transparent and the solid chatbox.
- **Team (decided 2026-10-25, Phase 3b):** a cap of 3 in total. Custom names can come later.
- **Character names (decided 2026-10-26):** numbered within their class, in team order. The first Wizard is "Wizard", the second "Wizard 2". So when "Adventurer 2" becomes the first Wizard, it's just "Wizard". Names can shift when someone takes a class advancement. Coins belong to the account and are shared. Phase 4 replaces the cap (see Roster below).
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
- Every expedition should have some bundle chance (user, 2026-10-27). Today only Dorgeshuun has one (20% Boss Bundle); Swamp Caves has none. Decide each expedition's bundle type and chance as part of the expedition ladder.
- Expedition speed: 100% while playing, 50% while logged out. Parked because it's complex and punishes sending characters off before logging out.
- Expedition XP that scales with level (e.g. half a level) instead of a fixed 1,000, if fixed XP stops mattering at high levels.
- Second class advancement, MapleStory-style (e.g. Wizard → Battle Mage / Cleric / Arcane Wizard). Possible answer to "what happens after level 10".
- Many unlockable characters, some hard or rare, each with its own unique way to unlock.
- More loot triggers: level-ups (small chance, since low skills level fast), quest completions, collection log slots (only works if the player has the game's collection log chat notification turned on), and more.
- Guild levels could also gate expeditions and rare characters, not just hires.
- Character art: a picture for each class instead of text only. Possible (checked 2026-10-26): start with in-game item icons drawn by RuneLite at runtime as placeholders (could also give gear items icons), then custom pixel-art portraits (about 32–64px, PNGs in resources, art the user owns) once the class list settles. 3D models in the sidebar aren't practical.
- UI redesign pass: the panel works but looks plain. Do it once the core concepts settle, so it isn't redone for every new feature. Game logic is already separate from the panel code (`Team`/`FellowshipCharacter` vs `FellowshipPanel`/`CharacterCard`), so a redesign shouldn't touch the rules. Limits: the sidebar is narrow (about 225px) and built with Swing.
- Custom names for recruits (typed in when recruiting).
- Beast Master: a 10% chance to unlock when you get a pet drop in OSRS (read the drop message only). Example of unlocks from rare game events, like an advancement from a rare raid item.
- Online features (leaderboards, trading). Risky under the rules, so maybe never.
- HP and a combat system (user, 2026-10-28). Small version first: HP replaces the injury roll (expeditions cost HP, low HP means resting). A full combat system (rounds, abilities, enemies) is a big build, after v1. Keep it fictional; never simulate real OSRS boss mechanics (rules: no content simulation).
