# Future design: expeditions, classes and gear

**Status:** planning only, started 2026-10-26 while the Phase 5–6 playtest runs. Nothing here is scheduled yet. Numbers marked *draft* are guesses to check against playtesting.

Each idea is tagged:
- **Content:** built from parts the code already has. Cheap to add.
- **System:** needs a new rule in the code first. Each one is its own build step.

## 1. Expeditions

### Existing expeditions (for comparison)

| Expedition | Party | Length | Threat | Rewards | XP per hour, per character |
|---|---|---|---|---|---|
| Explore Lumbridge Swamp Caves | 1 | 30 min | none | 1,000 XP, 10–50 coins | 2,000 |
| Looting the Dorgeshuun Caves | exactly 3 | 2 h | 8 | 3,000 XP each, 40–100 coins, 20% Boss Bundle | 1,500 (if it succeeds) |

### The expedition ladder (v1, started 2026-10-26)

Threat is matched to gear: power by rarity is 1 / 3 / 6 / 10 / 15, plus class bonuses later, so a top character reaches roughly 20–25. Each tier should pay more than the last, so the risk is worth it.

The early game has a small story: the Fellowship has set up camp. First you scout the **Encampment**, then your Skillers fortify it.

| Tier | Threat | Expedition | Status |
|---|---|---|---|
| Safe | 0 | Scout the Encampment Perimeter | New (decided 2026-10-26), replaces Swamp Caves as the first expedition |
| Safe (Skillers) | 0 | Securing and Fortifying the Encampment | Designed |
| 1 | ~3 | Explore Lumbridge Swamp Caves | Built as a safe trip; moves up to tier 1 (decided 2026-10-26) |
| 2 | 8 | Looting the Dorgeshuun Caves | Built |
| 3 | ~13 | **needed** | |
| 4 | ~20 | Plunder Tombs of the Barrow Brothers | Idea, end-game, needs a Vyre |

Draft numbers for the changed ones:
- **Scout the Encampment Perimeter:** solo, 30 min, safe, 1,000 XP and 10–50 coins (what the Swamp Caves give today).
- **Explore Lumbridge Swamp Caves (tier 1):** *draft* party of 1–2, threat 3, 1 hour, 1,500 XP each, 20–60 coins, small Generic Bundle chance.

**Unlocks (decided 2026-10-26):**
- **Scout the Encampment Perimeter is locked at the start** and unlocks at **Guild level 2**.
- **New Guild curve (proposed by the user, 2026-10-26; apply after the playtest):** each level costs 10, 15, 25 Guild XP for levels 2, 3, 4 (totals 10, 25, 50). After that, each step grows by 5 more than the last: 40 then 60 for levels 5 and 6 (totals 90, 150), confirmed by the user. Today's totals are 20, 50, 90, 140, 200. Side effects: hires come sooner (one per Guild level), and storage upgrades (from Guild level 4) unlock at 50 total instead of 90.
- Encampment fortifying unlocks when you hire a Skiller (decided earlier).
- Swamp Caves, Dorgeshuun and above: Guild levels to decide.

Consequences to watch in playtesting (Claude suggested leaving the Perimeter open; the user chose to lock it):
- With no expeditions, Guild XP only comes from level-ups. 10 Guild XP is about the Adventurer reaching level 10 plus one more level after its class advancement. For a low-level player that's 5+ hours with no expeditions.
- Coins only come from expeditions and bundles, so no hiring (including Skillers) until then.
- The panel should say clearly what to work toward, e.g. "Expeditions unlock at Guild level 2 (3 / 10 XP)", so a new player isn't confused about why there's nothing to do.

Coding note: keep the Swamp Caves' saved key (`LUMBRIDGE_SWAMP_CAVES`) and add the Perimeter scout as a new expedition. Changing a saved key would break any character who is away on it when the save loads.

### Securing and Fortifying the Encampment (idea, 2026-10-26)

- **Unlock (decided 2026-10-26):** hidden until you hire your first Skiller.
- **Who can go (decided 2026-10-26):** advanced Skillers only (Harvester, Artisan, Explorer). A plain Skiller can't be sent until it takes its class advancement.
- **Party:** 1 to 3 characters.
- **Length:** 1.5 hours, minus 30 minutes for each extra Skiller. 1 Skiller = 1.5 h, 2 = 1 h, 3 = 30 min.
- **Threat:** none. It's a safe expedition, which suits Skillers since they can only wear trinkets for now.
- **Rewards:** XP only, more than other expeditions. No coins or bundles (for now). *Draft:* 1,500 XP each, whatever the party size.

What it would take to build:
- **System:** an unlock rule ("you've hired a Skiller"). The first expedition unlock of any kind.
- **System:** class requirements on expeditions (only Harvester, Artisan and Explorer can be picked).
- **System:** a party size range (1–3). Dorgeshuun only supports an exact size.
- **System:** length that depends on party size.
- **Content:** XP-only rewards and no threat level.

Things to check in playtesting:
- **Bigger parties win by a lot.** With a fixed 1,500 XP each, the XP per hour per character is 1,000 solo, 1,500 for two and 3,000 for three. That pushes players to hire all 3 Skillers, which may be the point.
- **Shorter only helps if you're around.** If a player logs off for 8 hours, a 30-minute trip and a 1.5-hour trip both give one collection. The short version is a reward for checking in often.

### Ideas for later (systems)

- **Unlocking expeditions.** Some expeditions start locked. Guild level will unlock some; other unlock rules to be decided.
- **Level requirements.** E.g. every party member must be level 20+.
- **More party sizes.** Solo, small and large parties, set per expedition.

### Showing many expeditions (clutter, noted 2026-10-26)

Today every character card lists every expedition (`CharacterCard`), so 10 expeditions on 5 cards would be 50 rows in a narrow sidebar. Plan for the UI redesign pass:
- **One Expedition Board instead of a list on every card.** A single "Expeditions" button opens a window that lists the expeditions; you pick one, then pick who goes (like the Dorgeshuun party picker already does). Cards just show "Home", "Away, back in 29m" or "Resting".
- **Group the board by type**, e.g. Solo, Party, Skilling, with sections you can collapse.
- **Only show what's unlocked** (plus, maybe, the next few locked ones as goals). Hidden unlocks keep the list short on their own.
- **Grey out what nobody at home can do**, instead of hiding it.

Bigger idea, maybe later (system): a **rotating board** that offers a handful of expeditions at a time and refreshes daily. Less clutter and more variety, but it's a whole new system, so only if the board above isn't enough.

## 2. Classes

Template for each class idea: **name**, **how you unlock it**, **XP rules**, **gear**, **what makes it special**.

Note: each new class with its own weapon and armour needs about 10 new items (2 slots × 5 rarities). Decide the gear plan (section 3) before adding many classes.

### All class ideas at a glance (from the user's ideas doc, 2026-10-26)

About 27 classes in total. Built ones are marked ✅.

```
Adventurer ✅ ─┬─ Knight ✅ ─── Paladin / Rune Knight / Berserker
               ├─ Archer ✅ ─── Scout / Hunter / Trickster
               └─ Wizard ✅ ─── Battle Mage / Cleric / Arcane Wizard

Skiller ✅ ──── Harvester ✅ / Artisan ✅ / Explorer ✅

Thief (rare) ─┬─ Bandit ──── Warlord
              └─ Assassin ── Night Blade

Vyre (rare) ── Blood Mage / Death Knight / Shadow Lord

Acolyte (rare) ─┬─ Guthix Acolyte ──── Guardian of Guthix / Guthixian Dryad / Void Knight
                ├─ (other gods, e.g. Saradomin, Zamorak) ── 3 classes each
                └─ ...

Beast Master (rare, from a pet drop)
```

### Shared systems these ideas need

Most classes reuse the same few systems, so each one only has to be built once:

| System | Needed by |
|---|---|
| **Second class advancement** (a class cap below 99, then another advancement) | Knight, Archer and Wizard lines, Thief tree |
| **Perks** (each class does one special thing) | Thief tree, Vyre tree, probably the new Knight/Archer/Wizard classes |
| **Rare unlocks** (a chance to get a character from OSRS activity) | Thief, Vyre, Beast Master |
| **Location in OSRS** (knowing the player is in Morytania) | Vyre unlock |
| **Expeditions locked behind a character** | Vyre's Barrows expedition |
| **Gear borrowing** (using another class's gear) | Bandit, Assassin |

Possible build order (not decided): second class advancement with the Knight/Archer/Wizard classes first (mostly content once the system exists), then perks, then rare unlocks with the Thief tree, then Vyre.

**Perks are for special classes (decided 2026-10-26).** Only rare-unlock classes (Thief and Vyre trees for now) get perks, because they're hard to get and should feel unique. Normal classes still need to feel different, through their XP skills, gear and role (see the roles below).

### Skiller line (built; XP skills changed 2026-10-26, apply after the playtest)

New 2x XP skills, replacing the first guess in CLAUDE.md:

| Class | 2x XP |
|---|---|
| Harvester | Mining, Woodcutting, Fishing, Farming, Runecraft |
| Artisan | Smithing, Cooking, Fletching, Crafting, Herblore, Construction |
| Explorer | Hunter, Sailing, Thieving, Agility, Firemaking |

- Hunter moves from Harvester to Explorer, Runecraft from Explorer to Harvester, and Firemaking from Artisan to Explorer. Sailing is new (RuneLite already knows the skill).
- **Prayer and Slayer** are left out of the Skiller line on purpose, saved for other classes later (e.g. Prayer for the Cleric).
- **Overlap:** Explorer and Thief both get 2x Thieving and Agility. Fine, since the Thief has perks.

### Second advancements for Knight, Archer and Wizard (names only, 2026-10-26)

| From | Choices |
|---|---|
| Knight | Paladin, Rune Knight, Berserker |
| Archer | Scout, Hunter, Trickster |
| Wizard | Battle Mage, Cleric, Arcane Wizard |

**Roles (decided 2026-10-26):**

| Role | Knight | Archer | Wizard |
|---|---|---|---|
| Support | Paladin | Scout | Cleric |
| Raw damage | Berserker | Hunter | (Battle Mage?) |
| Magic damage | Rune Knight? | Trickster | Arcane Wizard? |

(The question marks are guesses to confirm.)

No perks for these, so the role has to come from somewhere else. Suggestion: a **stat tilt** per class. Party power is an average, so a Support with high DEF raises the whole party's DEF and means fewer injuries. A Raw damage class leans into ATK. That's a small system (a multiplier on ATK/DEF/MAG per class) and fits the gear plan in section 3.

Still to decide for each: level cap before the advancement, 2x XP skills, stat tilt, gear (suggest: keeps its parent's gear).

Naming note: "Hunter" is also an OSRS skill, now an Explorer skill. Kept for now (decided 2026-10-26); rename later if it gets confusing.

### Vyre tree (idea, 2026-10-26)

| Class | Unlock | Perk |
|---|---|---|
| Vyre | Rare chance from any activity in Morytania | Unlocks the expedition "Plunder Tombs of the Barrow Brothers" |
| Blood Mage | Vyre class advancement | Higher chance to succeed on the Barrows expedition |
| Death Knight | Vyre class advancement | Finishes the Barrows expedition faster |
| Shadow Lord | Vyre class advancement | Better chance at rare loot |

Decided 2026-10-26:
- **The Barrows expedition is end-game content for v1:** an intense Morytania raid that needs a Vyre to unlock. All three Vyre advancements have perks for it.
- **Unlock:** XP gained while standing in Morytania has a chance to unlock a Vyre (the plugin can read the player's location, which is read-only and allowed). Suggest rolling per amount of XP, like the Generic Bundle, so training methods with many small XP drops don't get more rolls.

Notes:
- Perks that only work on one expedition do nothing the rest of the time. That's accepted for v1. If more Morytania expeditions come later, the perks could cover those too.
- **Overlaps:** Death Knight's speed perk is like the Assassin's, and Blood Mage's success perk is like a stat boost. Fine, as long as each class has something of its own.
- **Shadow Lord's rare loot** needs expedition loot tables, which don't exist yet (expeditions give a set bundle chance today).

### Acolyte tree (in v1 from 2026-10-27; the god layer is "maybe")

- **Unlock:** linked to Prayer, e.g. a rare chance per amount of Prayer XP (the shared rare-unlock system, like Thief and Vyre). Prayer was kept free from the Skiller paths for a class like this.
- **First advancement: choose a god to worship** instead of a class. Same system as a normal class advancement, with in-world wording ("Choose your god"). E.g. Acolyte → Guthix Acolyte.
- **Second advancement:** each god has 3 unique classes. Guthix: **Guardian of Guthix, Guthixian Dryad, Void Knight**. Other gods to design (Saradomin and Zamorak are the obvious ones; Armadyl, Bandos, Zaros, Seren could come later).
- **Size:** with 3 gods, it's 1 + 3 + 9 = 13 classes to build, each needing stats. First suggested for v1.1; **the user added the Acolyte to v1 on 2026-10-27, with the god layer as a "maybe"**. Gear is no longer a cost: Acolytes wear Robes (see "Armour types" in section 3).
  - **User's view (2026-10-27):** players won't see 13 classes. It's a rare unlock and they'll be happy to get one, so it doesn't flood the game. (The build cost is still all 13, since any of them can be picked.)
  - **Making it cheap to build (suggested):** one gear set per god, shared by its 3 classes. The classes then differ only by XP skills and stat split, so each is mostly a line of data, and the real work is 3 god gear sets instead of 9 class sets.
- **Systems needed:** rare unlocks, second class advancement (both already planned for v1). Perks would be up to the user (it's a rare class, so by the current rule it would get them).
- Still to decide: which gods, the XP skills (Prayer for sure), gear (one god set, or per class?), and roles for the 9 final classes.

### Beast Master (idea, already in the Parking Lot)

10% chance to unlock when you get a pet drop in OSRS (read from the game's drop message). Still to decide: class line, XP rules, perk.

### Thief tree (idea, 2026-10-26)

A rare, special character with a two-step advancement. Theme: Bandit is the melee branch, Assassin the ranged branch. Warlord stays at base, Night Blade goes into combat.

```
Thief ──┬── Bandit ──── Warlord
        └── Assassin ── Night Blade
```

| Class | Unlock | 2x XP (*draft*) | Gear (*suggestion*) | Perk |
|---|---|---|---|---|
| Thief | Very rare chance from Thieving and Agility XP | Thieving, Agility | Generic only (like Skillers) | More coins from expeditions it goes on |
| Bandit | Thief class advancement | Thieving, Agility, Attack, Strength | Borrows Knight gear | Chance to double coins from bundles and expeditions (see below) |
| Warlord | Bandit at high level | Same as Bandit | Knight gear | Earns coins over time while it stays home |
| Assassin | Thief class advancement | Thieving, Agility, Ranged | Borrows Archer gear | Chance to finish a dangerous expedition in half the time |
| Night Blade | Assassin at high level | Same as Assassin | Archer gear | Can go on any dangerous expedition alone (decided 2026-10-26, see below) |

What it would take to build:
- **System: rare unlocks.** A chance to get a character from OSRS XP. Suggest rolling per amount of XP (e.g. a chance per 10,000 Thieving or Agility XP), like the Generic Bundle, so fast and slow training methods are treated the same. Rare characters don't use a Guild hire.
- **System: second class advancement.** Today a class advancement happens once, at level 10. Thief needs caps per class and a second advancement.
- **System: perks.** None exist yet, and each perk is its own bit of code. Five perks = five small build steps.
- **System: gear borrowing.** Letting a class use another class's gear (Bandit uses Knight gear). Small change to the equip rule.
- **Content:** the 2x XP skills.

Things to watch:
- **Advancement resets to level 1.** With a reset at each step, a deep tree takes a long time. On today's curve, level 20 is about 840k XP, level 30 about 2M and level 50 about 7M. A Thief at 30 plus a Bandit at 30 is about 4M XP before a Warlord.
- **Night Blade solo (decided 2026-10-26):** it may ignore an expedition's party size and go alone. Everything else stays normal: the "party" power is just its own ATK and DEF, so it must be geared well enough by itself, and it can still fail and be injured. Going alone also means only one character earns the XP.
- **Bandit coins (suggestion, not decided):** a *chance* (e.g. 25%) to double the coins from bundles and expeditions, rather than always doubling, so a Bandit is nice to have but not required. Salvage was dropped as not fitting the theme. Since this builds on the Thief's coin bonus, the tree could upgrade one perk as it goes: Thief = a bit more expedition coins, Bandit = a chance to double them (bundles too), Warlord = coins over time.

Open questions:
- Name: Thief or Rogue? (Rogue fits OSRS's Rogues' Den.)
- Thief perk: does it make expeditions **shorter** ("get in, get out")? It was written as "increase the time". And does that overlap with the Assassin's speed perk?
- Level caps for Thief, Bandit and Assassin (somewhere between 20 and 50).
- Do perks work for the whole team (Bandit's bundle perk) or only when the character is involved (on the expedition, or at home for Warlord)?
- Can you get more than one Thief, or does the rare chance stop once you have one?

## 3. Gear

Wait for the Phase 5–6 playtest notes before redesigning gear.

### Armour types instead of class gear (decided 2026-10-27, for v1)

Armour comes in three types, **Plate, Leather and Robe** (heavy, medium, light), and each class lists which types it can wear. Draft from the user:

| Armour | Who can wear it |
|---|---|
| Plate | Knights (and their second advancements) |
| Leather | Thieves, Archers, Vyres |
| Robe | Acolytes, Vyres, Wizards |

- **Why:** the number of armour types stays at 3 however many classes are added, so bundle drops stay useful (no loot dilution), and a new class only needs a line saying what it wears. It also solves second advancements (a Paladin can wear plate) without a separate "gear family" system.
- **A class can wear more than one type** (the Vyre wears Leather and Robe).
- **No save migration needed:** the saved names `PLATE`, `LEATHER` and `ROBE` already exist. In code, each `CharacterClass` gets a list of allowed armour types instead of each `GearType` naming one class.
- **Still to decide:**
  - **Weapons:** suggested the same idea (Sword / Bow / Staff, or Melee / Ranged / Magic, with each class listing what it can wield).
  - Adventurers and Skillers: still trinkets only, or Light armour?
  - Bandit and Assassin "borrowing" gear becomes just "can wear Plate" / "can wear Leather", so the gear-borrowing system is no longer needed.

### Class stat bonuses (needed before v1, noted 2026-10-26)

Classes get flat power as they advance, e.g. a Paladin (tank) comes with extra DEF. This replaces the "stat tilt" idea in section 2 and is how normal classes (no perks) get their role.

- **Changes an earlier rule:** Phase 5 said "power comes from gear only". This adds a second source: gear + class.
- **Flat or multiplier?** Suggest flat (e.g. Paladin +3 DEF). It's easy to show on the card ("+3 DEF from class"), and it helps characters in weak gear. A multiplier (e.g. +20% DEF) does nothing without gear and helps rich players most.
- **Scale with advancement:** a second advancement should give more than a first one (e.g. Knight +1 DEF, Paladin +4 DEF).
- **Balance against threat levels:** Dorgeshuun is threat 8, and Rare gear gives 6. A class bonus of a few points matters a lot at these numbers, so pick them together with expedition threats.

**Stat splits (example from the user, 2026-10-26):**

| Class | Bonus |
|---|---|
| Paladin | +4 DEF |
| Berserker | +4 ATK |
| Rune Knight | +2 ATK, +2 MAG |

Suggested rule: a **stat budget** per tier. Every class at the same advancement gets the same total (here 4), split differently. That keeps classes fair, so the choice is about role, not about which one is strongest.

### Gear level requirements (idea, 2026-10-26)

Gear needs a character level to wear it. This answers "what do character levels do?": higher levels let you wear better gear. Example (*draft*): Common 1, Uncommon 10, Rare 25, Epic 50, Legendary 75.

**Problem: class advancement resets the level to 1.** A level 60 Knight wearing Epic gear would become a level 1 Paladin and could no longer wear it. Options:
1. **Use total level** (*suggested*): all the levels a character has ever earned, added up across advancements. Adventurer 10 + Knight 60 + Paladin 3 = total level 73. Nothing is lost on advancing, and the card can show "Total level 73".
2. Gear requirements by advancement instead of level (e.g. Epic needs a second advancement).
3. Equipped gear stays on after advancing, but the character can't put on new gear above its level.

A nice side effect: a Legendary that drops early sits in storage as a goal to work toward.

**User's leaning (2026-10-26): keep the reset, and let it lock you out of some gear.** That makes advancing a real decision instead of an obvious upgrade. To make it feel fair, not punishing:
- **Keep the lockout short.** Getting back to level 25 is about 1.4M XP (roughly 70 hours for a low-level player at 100%). Low requirements (e.g. Rare at 15, Legendary at 50) keep it to days, not weeks. Tune with the level curve.
- **Caps must fit requirements.** If the Knight advances at 30 and Epic needs 50, a Knight can never wear Epic. Fine if on purpose.
- **Still to decide: what happens to gear you can no longer wear.** Suggested: it stays on, but you can't put on new gear above your level. The harsher option (it goes back to storage) could overflow the 10-slot storage.

### Bundle loot pools (idea, 2026-10-26)

Today every bundle can roll any item; only the rarity odds and coins differ. Once there are more items, each bundle type gets its own **loot pool**, so where you got a bundle matters. Examples from the user:
- **Raid Bundle:** the best weapons.
- **Slayer Bundle:** a good mix of everything.
- **Clue Bundle:** only magic armour.

Notes:
- **Wait for more items.** With 35 items (one per rarity per type), narrow pools would leave each bundle only a handful of possible drops. Pools make sense once each type has several items per rarity.
- **Keep one "anything" bundle** (the Generic Bundle), so every item can still drop from normal play and nobody is forced into raids for one item.
- Easy to build when the time comes: each `BundleType` gets a list of allowed gear types (and maybe a minimum rarity), and `rollItem` only picks from those.
- Each bundle already has its own icon: a clue reward casket, fancier for better bundles (Generic = easy, Slayer = medium, Clue = hard, Boss = elite, Raid = master).
