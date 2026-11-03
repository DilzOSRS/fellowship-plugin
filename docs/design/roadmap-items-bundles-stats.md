# Roadmap: items, bundle pools and item stats (2026-11-01)

This is step 2–4 of the v1 build order ("design the item list on paper", "build tags and items", "bundles with their pools"), broken into smaller steps. **Paper first, code after.** Each step ends with a decision written down before the next one starts. Ideas that don't belong to the current step go to the Parking Lot.

**What levels do (user, 2026-11-01):** levels open expeditions and gear. They don't add stats.

**No level caps (user, 2026-11-01):** every follower keeps levelling. An Adventurer can stay t1 past level 10 and choose when to take its class advancement (assumed: 10 is still the minimum level to advance). Advancing still resets to level 1. The code's current cap (XP wasted at 10) and CLAUDE.md's "Level cap" decision need updating when this is built.

**Shorthand (user, 2026-11-01):**
- **Followers:** t1 = the base follower (Adventurer, Vyre, Sailor, ...), t2 = its first class advancement (Knight, Blood Mage, Harpooner, ...), t3 = a second advancement.
- **Items:** t1 = Common, t2 = Uncommon, t3 = Rare, t4 = Epic, t5 = Legendary.

**Soft rules, not hard rules (user, 2026-11-01):** the budgets, ladders and level numbers here are guides for balancing, and still being playtested. Exceptions can always be made.

## Stage A: the rules (paper)

Order (decided 2026-11-01): tags, then class stats, then the item budget, then check both against the threat levels. Class stats come before items because they're small (a few numbers per class) and set the yardstick. A4 can still nudge both.

**A1. Tag rules: done (2026-11-01).** The system is settled (see the brainstorm doc): single, combo and class tags; Generic; visible and hidden tags; follower tags limited by hidden tags; the power ladder. Weapon types (*sword*, *bow*, *staff*) are visible tags and the style (*melee*, ...) is hidden. Each follower's tags are in B1.

**A2. Class base stats: done for now (2026-11-01).** See the draft below. **The FaB-style baseline of 4 is for followers, not items.**
- The Adventurer (t1) has **no base stats**; base stats come with its t2 jobs (Knight, Archer, Wizard: 4 points each). Some other t1 followers have stats, so they're stronger t1 combat followers than the Adventurer.
- **Thief tree:** Thief 0 (coin perk), Bandit 3 ATK, Assassin 3 ATK. t3 (Warlord, Night Blade) later.
- **Sailor tree:** Sailor 1 ATK 1 DEF; Swashbuckler 4 ATK 1 DEF; Harpooner 5 ATK; Navigator 1 ATK 1 DEF 3 MAG (t2s net 5).
- **Vyre tree:** Vyre 1 ATK -1 DEF; Death Knight 6 ATK -2 DEF 2 MAG; Shadow Stalker (renamed from Shadow Lord) 8 ATK -4 DEF 2 MAG; Blood Mage 4 ATK -4 DEF 6 MAG (t2s net 6). Glass cannons. End-game mostly because of the Barrows Raid unlock and its gear.
- **Slayer Master (single tier, no advancement):** net 4, split still to decide.
- **MAG stays as it is** (see "MAG" below).
- Waiting on purpose: the Slayer Master's split, t3 classes, the Vyre t2 perks.

**A3. The item stat budget (scoring) system: done for now (2026-11-01).** Items have their own budgets per tier, not the follower baseline. See the draft below.
- **5 tiers** (Common to Legendary). The October 7-metal-tier plan is dropped for now.
- **Balance and plan Common, Uncommon and Rare (t1–t3) first.** Epic and Legendary are the end-game items and get designed later.
- **Not every item type has all 5 tiers.** E.g. a *dark* bow might only exist at Epic and Legendary. Those high-only types wait with Epic/Legendary.
- **Tier budgets (draft): 2 / 4 / 7** for a single-tag item at Common / Uncommon / Rare.
- **Tags change the budget:** no tags (Generic) -1, single tag 0, combo +1, class tag +2. A special Generic item can still reach the full budget (an exception).
- **1 point = 1 ATK, 1 DEF or 1 MAG** (decided). MAG stays half ATK, half DEF.
- **Item perks: parked, low priority** (user, 2026-11-01).
- **Level requirements (decided for playtesting, 2026-11-01):** Common 1 (none), Uncommon 10, Rare 15, Epic 20–25, Legendary 30+. See below.

**A4. Check against the threat levels: first pass done (2026-11-01). Revisit once more expeditions are designed (after bundles), and when Epic/Legendary budgets exist.** See "A4 results" below.
- Swamp Caves (tutorial, threat 3): worked out in `tutorial-phase.md`.
- Dorgeshuun (threat 8): fits a party in Uncommon gear (6% fail). Keep threat 8 (user: fine as is).
- Sailing Dungeon: **15–17, not decided** (pushed up from 12, which felt too low). Numbers in `expedition-chart.md`.
- Barrows Raid (threat 20): waits for Epic/Legendary numbers.
- **Found a gap: who can fill Dorgeshuun's 3 slots** (see below).
- **Things feel too easy** (fails rare, injuries cheap): parked, notes in `expedition-chart.md`.

## Stage B: the content (paper)

**B1. Item types for v1, and class tags: done (2026-11-01).**
- **Part 1, follower tags:** see the table below.
- **Part 2, item types:** see the table below. 40 items from Common to Rare. Dark armour is parked.

**B2. The full item list: done except dark items (2026-11-01).** See `item-list-v1.md`.

**B3. Bundles and their pools: first pass done (2026-11-02).** See `bundles-v1.md`: sources, chances, odds, pools, the empty-tier rule and weights. Loose ends are listed there.

## Stage C: the build (code, one step at a time, each tested in-game)

1. **Tags in code**, plus the hover tooltip (shows visible tags, who can use it, level requirement, and why not, e.g. "Bandits only use dark weapons"). (built 2026-11-02, tested in-game 2026-11-02: the level requirement and combo items wait for step 3, since neither exists before the new item list. Warlord and Night Blade borrow the Bandit's and Assassin's tags until t3 is designed.)
2. **Class base stats** (small: a few numbers per class, shown on the card as "+3 DEF from class"; negative stats too, e.g. "-2 DEF"). The card also shows the **worked-out ATK and DEF** including MAG (e.g. "ATK 7, DEF -1"), so players don't do the sum themselves. Includes renaming Shadow Lord to Shadow Stalker (display name only; keep the saved name or add a migration), Swamp Caves to threat 3, and removing level caps. (built 2026-11-02, tested in-game 2026-11-02. Warlord and Night Blade borrow the Bandit's and Assassin's 3 ATK until t3 is designed.)
3. **The new item list** with budgets, stats and level requirements (fine to change the save format; test saves get wiped). (built 2026-11-02, tested in-game 2026-11-02. The save format didn't need to change: old Epic/Legendary gear becomes the Rare item of its type. Combo items and level requirements from step 1 came with it. Gear above the follower's level stays on, faded, with no stats: the user's choice, 2026-11-02.)
4. **Bundle pools.** (built 2026-11-02, not tested in-game yet; see the History in `bundles-v1.md`.)
5. **Playtest and tune** the budget tables, level requirements, then the bundle odds.

## Not in this roadmap (Parking Lot)
- Gear System 2: any rarity on any item, random perks (after v1).
- Item perks (parked, low priority).
- Stats that grow with level (levels open expeditions and gear instead).
- Region followers and their gear.
- The Skiller's % speed-up perk, the Sailor's expeditions and the Slayer Master's hunt are follower/expedition work, tracked in the brainstorm doc.
- **Fun uses for MAG** (user, 2026-11-01): gear and expeditions that do interesting things with the MAG stat, e.g. expeditions weak or resistant to magic. Down the road.
- In-between tiers (e.g. the old Iron/Steel/Adamant steps), only if progression feels too jumpy after playtesting.
- Destiny-inspired ideas: one Legendary/Exotic per follower, infusing old gear into new (see the brainstorm doc).
- Injury rework and a tougher fail rule (see `expedition-chart.md`).
- **Dark armour** (user, 2026-11-01): parked, the user needs to think about dark gear.
- *Slayer* gear: later.

---

## B1 part 1: follower tags (decided 2026-11-01)

Followers have visible tags (weapon types, armour weights, talents) and a **hidden style** (melee, ranged, magic). Every weapon has a hidden style too. The style is what combo items like *nautical-melee* and limits like "dark melee weapons only" check against. Players never see it.

| Follower | Tier | Weapons | Armour | Style (hidden) |
|---|---|---|---|---|
| Adventurer | t1 | Generic only | Generic only | |
| Knight | t2 | *sword* | *plate* | melee |
| Archer | t2 | *bow* | *leather* | ranged |
| Wizard | t2 | *staff* | *robe* | magic |
| Thief | t1 | *sword*, *bow* | *leather* | melee, ranged |
| Bandit | t2 | *sword*, *dark* (melee weapons only) | *leather* | melee |
| Assassin | t2 | *bow*, *dark* (ranged weapons only) | *leather* | ranged |
| Sailor | t1 | *nautical* | *leather* | |
| Swashbuckler | t2 | *sword*, *nautical* | *leather* | melee |
| Harpooner | t2 | *bow*, *nautical* | *leather* | ranged |
| Navigator | t2 | *staff*, *nautical* | *leather* | magic |
| Vyre | t1 | *sword*, *staff*, *dark* | *leather*, *robe*, *dark* | melee, magic |
| Death Knight | t2 | *sword*, *dark* | *leather*, *robe*, *dark* | melee |
| Shadow Stalker | t2 | *bow*, *dark* | *leather*, *robe*, *dark* | ranged |
| Blood Mage | t2 | *staff*, *dark* | *leather*, *robe*, *dark* | magic |
| Slayer Master | single | *sword*, *bow*, *staff*, *slayer* | *leather* | melee, ranged, magic |

- **Sailor tree (user):** the whole tree wears *leather* and has *nautical*; each t2 adds its own weapon tag.
- **Vyre tree (user):** *leather* and *robe* (plus *dark*) for the whole tree. Footnote: the Death Knight may get *plate* later.
- **Slayer Master (user):** every weapon style, since Slayer in OSRS uses them all, and *leather*. *Slayer* gear comes later.
- **Plain talent items work for any style:** e.g. the Anchor (*nautical*, melee) can be used by the Harpooner and Navigator too. The user is fine with that for these three. **Note for later:** if a future *nautical* ranged class shouldn't use melee weapons, give it "*nautical*, ranged weapons only" (the same limit the Bandit and Assassin use).
- Generic items (no tags) fit everyone.

## B1 part 2: item types for v1 (decided 2026-11-01)

Common (C), Uncommon (U) and Rare (R) only; Epic and Legendary come later.

| # | Type | Slot | Tags | Hidden style | Tiers | Who can use it |
|---|---|---|---|---|---|---|
| 1 | Sword | Weapon | *sword* | melee | C, U, R | Knight, Thief, Bandit, Swashbuckler, Vyre, Death Knight, Slayer Master |
| 2 | Bow | Weapon | *bow* | ranged | C, U, R | Archer, Thief, Assassin, Harpooner, Shadow Stalker, Slayer Master |
| 3 | Staff | Weapon | *staff* | magic | C, U, R | Wizard, Navigator, Vyre, Blood Mage, Slayer Master |
| 4 | **Dagger** | Weapon | none (Generic) | melee | C, U, R | Anyone, including the Adventurer |
| 5 | **Anchor** | Weapon | *nautical* | melee | U, R | The whole Sailor tree |
| 6 | Cutlass | Weapon | *nautical-melee* | melee | U, R | Swashbuckler |
| 7 | Harpoon | Weapon | *nautical-ranged* | ranged | U, R | Harpooner |
| 8 | Trident | Weapon | *nautical-magic* | magic | U, R | Navigator |
| 9 | Dark weapons (one each: melee, ranged, magic) | Weapon | *dark* | melee / ranged / magic | U, R | Vyre tree; Bandit (melee only), Assassin (ranged only) |
| 10 | Plate | Armour | *plate* | | C, U, R | Knight |
| 11 | Leather | Armour | *leather* | | C, U, R | Archer, Thief tree, Sailor tree, Vyre tree, Slayer Master |
| 12 | Robe | Armour | *robe* | | C, U, R | Wizard, Vyre tree |
| 13 | **Clothes** | Armour | none (Generic) | | C, U | Anyone |
| 14 | Dark armour | Armour | *dark* | | **Parked** | (Vyre tree) |
| 15 | Trinket | Trinket | none (Generic) | | C, U, R | Anyone |

- **40 items:** Sword, Bow, Staff, Dagger, Plate, Leather, Robe, Trinket (8 × 3 = 24); Anchor, Cutlass, Harpoon, Trident (4 × 2 = 8); dark weapons (3 × 2 = 6); Clothes (2).
- **A fresh t1 Sailor starts on the Dagger** until level 10, since the Anchor starts at Uncommon.
- **No Rare Generic armour:** Clothes stop at Uncommon. A special Rare Generic piece could still be an exception later.
- **Sailing weapons at Uncommon too:** the Sailing 1 Bundle covers them (see `bundles-v1.md`).

---

## A4 results (first pass, 2026-11-01)

Simulated with the real rules in `Danger.java` (fail = (threat − party ATK) ÷ threat; injuries from party DEF; a fail with DEF at or above the threat still has a 10% injury chance). Gear: a single-tag weapon and armour of the tier, plus a Generic trinket. Party ATK and DEF are the average of the members.

| Expedition (threat) | Party | Party ATK | Party DEF | Fail | Someone injured |
|---|---|---|---|---|---|
| Dorgeshuun (8) | Knight, Archer, Wizard in **Uncommon** | 7.5 | 7.5 | 6% | 11% |
| Dorgeshuun (8) | Knight, Archer, Wizard in **Common** | 4.5 | 4.5 | 44% | 67% |
| Dorgeshuun (8) | Knight + Archer (Uncommon) + Sailor t1 (Common trinket) | 5.5 | 5.5 | 31% | 51% |
| Dorgeshuun (8) | Knight (Common) + a 2nd Adventurer t1 + Vyre t1 (trinkets only) | 1.8 | 1.8 | 77% | 95% |

Sailing Dungeon results at 12, 15, 17 and 18 are in `expedition-chart.md`.

What it shows:
- **The ladder works:** Common gear struggles at Dorgeshuun, Uncommon clears it, Rare handles the Sailing Dungeon. Each tier of gear opens the next rung.
- **The jump from 8 to 15+ is big** (Uncommon to Rare gear). The expedition ladder will want one or two rungs in between (around 10–12), designed after bundles.

### The gap: filling Dorgeshuun's 3 slots
- Dorgeshuun needs exactly 3 followers. The v1 plan says dangerous expeditions take **Combat** followers only.
- After the tutorial, a new player has the first Adventurer and the second one from Fortify. **A third Adventurer never joins**, so the third Combat slot needs a rare follower (Thief, Vyre, Sailor, Slayer Master). That could make Dorgeshuun a long wall right after the tutorial.
- And a t1 in the party drags it down hard (the last Dorgeshuun row above).
- Options to think about (not decided):
  1. Let a **third Adventurer** join some way (e.g. a later Guild level or a later expedition).
  2. Make Dorgeshuun a **2-follower** expedition, and save 3-follower parties for later rungs.
  3. Add an expedition rung between Fortify and Dorgeshuun that unlocks the third follower.
  4. Accept it: the rare third follower is the goal after the tutorial.

---

## Draft: class base stats (2026-11-01)

Already noted in `docs/future-design.md` ("Class stat bonuses", 2026-10-26); this ties it to the budget system.

- **Classes get flat base stats**, so a class advancement makes a follower stronger even with no gear. Power = gear + class.
- **The FaB-style baseline: 4 points** (user, 2026-11-01), split by role. Every class at the same step gets about the same total; a perk can make up for fewer points, and rare followers can have a bit more.
- **MAG and half points:** MAG counts half to ATK and half to DEF, and the code keeps the decimals (no rounding). Odd MAG values are fine.
- **Negative stats are a drawback that pays for more offence** (like FaB's extra costs): -2 DEF frees 2 points to spend elsewhere, so the total ("net") stays on budget. The code already handles negative DEF: it pulls the party's average DEF down, and the injury roll picks the lowest-DEF member more often.

| Follower | Tier | ATK | DEF | MAG | On expeditions | Net total | Perk (planned, not built) |
|---|---|---|---|---|---|---|---|
| Adventurer | t1 | 0 | 0 | 0 | No base stats | 0 | |
| Knight | t2 | 1 | 3 | | 1 ATK, 3 DEF (tank) | 4 | |
| Archer | t2 | 3 | 1 | | 3 ATK, 1 DEF (damage) | 4 | |
| Wizard | t2 | | | 4 | 2 ATK, 2 DEF (balanced) | 4 | |
| Thief | t1 | 0 | 0 | 0 | No base stats | 0 | More coins from expeditions |
| Bandit | t2 | 3 | | | 3 ATK | 3 | Chance to double coins |
| Assassin | t2 | 3 | | | 3 ATK | 3 | Chance to finish a dangerous expedition in half the time |
| Warlord | t3 | ? | ? | ? | | | Coins over time at home (user is iffy, see below) |
| Night Blade | t3 | ? | ? | ? | | | Can go on any dangerous expedition alone, so it needs real stats |
| Sailor | t1 | 1 | 1 | | 1 ATK, 1 DEF | 2 | Unlocks boat building, then the Sailing Dungeon |
| Swashbuckler | t2 | 4 | 1 | | 4 ATK, 1 DEF | 5 | Speeds up the dungeon part of a voyage (idea) |
| Harpooner | t2 | 5 | | | 5 ATK | 5 | Extra loot from voyages (idea) |
| Navigator | t2 | 1 | 1 | 3 | 2.5 ATK, 2.5 DEF | 5 | Speeds up the sailing parts of a voyage (idea) |
| Vyre | t1 | 1 | -1 | | 1 ATK, -1 DEF | 0 | Unlocks the Barrows Raid |
| Death Knight (melee) | t2 | 6 | -2 | 2 | 7 ATK, -1 DEF | 6 | To redo |
| Shadow Stalker (ranged) | t2 | 8 | -4 | 2 | 9 ATK, -3 DEF | 6 | To redo |
| Blood Mage (magic) | t2 | 4 | -4 | 6 | 7 ATK, -1 DEF | 6 | To redo |
| Slayer Master | single tier | ? | ? | ? | | 4 | Goes on task with you; better Slayer Bundles |

- Bandit and Assassin have 3 points plus a perk, so a perk costs about 1 point at t2.
- **Vyre t1:** 1 ATK, -1 DEF nets 0, the same total as the Adventurer, but with the glass-cannon shape from day one. Its value at t1 is the Barrows Raid unlock and the *dark* tag.
- **Vyre t2:** net 6. MAG softens the negative DEF (Blood Mage shows -4 DEF but works out to -1), so the Shadow Stalker is the true glass cannon at -3.
- **Vyre t2 perks: the user no longer likes the October ones** (Barrows success, speed, rare loot). Come back to them.
- **Slayer Master:** net 4 because it never advances, keeping it level with the Adventurer t2s. Split options floated: 2 ATK 2 DEF, 3 ATK 1 DEF, or 2 ATK 1 DEF 1 MAG. The choice it creates: send it on expeditions, or keep it for hunts while you do Slayer.
- **Warlord perk (user, 2026-11-01):** "coins at home" feels better suited to a non-combat follower. Idea for when t3 is refined: a group-focused perk, e.g. the Thief tree gets bonuses on expeditions, with a bigger bonus if the Warlord stays at base.
- **Flat, not a percentage:** a flat bonus helps followers in weak gear and is easy to show on the card; a percentage does nothing without gear.
- **Not tied to level:** levels reset to 1 on advancing, so stats that grow with level would make a follower *weaker* right after advancing.
- A Knight gets only 1 ATK from its class, so it relies on its weapon for threat checks. Fine for a tank; an attack-focused t3 (e.g. Berserker) can fix that later.

### MAG (decided 2026-11-01)
- **Keep MAG as it is:** 1 MAG = 0.5 ATK + 0.5 DEF, no rounding. It can't break the maths, since it's a fixed conversion and budget totals stay fair.
- **Show the worked-out ATK and DEF on the card** so players don't have to do the half-and-half sum, and so it's visible when MAG softens negative DEF.
- **Later (user):** gear and expeditions that do fun things with MAG, giving magic its own identity (e.g. magic weaknesses or resistances).

## Draft: the item stat budget system (2026-11-01)

Items don't use the follower baseline of 4; each tier has its own budget. Not PvP, so rare and hard-to-get items can simply have a bigger budget.

**Budget = tier budget + tag ladder adjustment**, then spent on stats (and perks later).

### Tier budgets and level requirements
| Tier | Budget (single tag) | Level to equip | Total XP to that level | Today's power |
|---|---|---|---|---|
| t1 Common | 2 | 1 (none) | 0 | 1 |
| t2 Uncommon | 4 | 10 | 112k | 3 |
| t3 Rare | 7 | 15 | 335k | 6 |
| t4 Epic | ~10–11 (later) | 20–25 | 620k–983k | 10 |
| t5 Legendary | ~14–15 (later) | 30+ | 1.45M+ | 15 |

- **Common at level 1** so a fresh class advancement (back at level 1) can wear any Common item it finds. This keeps the tutorial working: one Common weapon makes Swamp Caves safe.
- **Uncommon at 10:** a maxed-out t1 can wear Uncommon before advancing; a fresh t2 drops back to Common and works back up. That's the lockout the user wanted.
- **Rare at 15:** out of reach for a t1 that advances at 10, so Rare gear effectively means "after a class advancement" (a t1 that keeps levelling past 15 can still wear Rare Generic or tagged gear it qualifies for).
- **Epic and Legendary use ranges:** each item sits somewhere in its range (a weaker Epic needs 20, a stronger one 25).
- XP guide: a low-level player earns roughly 20k XP an hour, a fast one about 110k (at 100% XP).

### Tag ladder (user's soft rule, 2026-11-01)
No tags costs a point; each restriction adds one back.

| Item | Tags | Adjustment | Common | Uncommon | Rare |
|---|---|---|---|---|---|
| Generic | none | -1 | 1 | 3 | 6 |
| Single tag (*sword*, *dark*) | 1 | 0 | 2 | 4 | 7 |
| Combo (*dark-leather*, *nautical-ranged*) | 2 | +1 | 3 | 5 | 8 |
| Class tag (*Death Knight*) | 1 class | +2 | 4 | 6 | 9 |

- **Exceptions are fine:** e.g. a special Generic item from a deep bundle can hit the full tier budget (or more), so a "better" Generic version of something exists.
- Flat steps (not percentages), so it's easy to work out. The steps matter more at Common (a class-tagged item is 4× a Generic one) than at Rare (9 vs 6); acceptable, since Common gear is replaced quickly.

### Spending
- 1 point = 1 ATK, 1 DEF or 1 MAG (MAG counts half to ATK and half to DEF on expeditions, so 1 MAG is worth the same total as 1 ATK or 1 DEF).
- The slot sets a default: weapons mostly ATK, armour mostly DEF, trinkets mostly MAG, but an item can mix (e.g. a sword with 5 ATK and 2 DEF).
- A drawback gives points back (e.g. "can't be worn on safe expeditions" or "-1 DEF"), like FaB's extra costs.
- Perks would cost points, but item perks are parked for now.

Why it helps: every item's power can be explained by one line of maths, new items are quick to make, and balance changes happen in one table instead of item by item.

## History
- 2026-11-02: Stage C step 1 (tags in code) built; level requirement and combo items moved to step 3.
- 2026-11-02: Step 1 tested in-game. Works; the tooltip looks plain, fine as a placeholder.
- 2026-11-02: Step 2 (class base stats, no level caps, Swamp Caves threat 3, Shadow Stalker) built.
- 2026-11-02: Step 2 tested in-game, all good.
- 2026-11-02: Step 3 (the item list) built. Decided: worn gear above the level stays on with no stats.
- 2026-11-02: Step 3 tested in-game, all good.
- 2026-11-02: Step 4 (bundle pools) built.
