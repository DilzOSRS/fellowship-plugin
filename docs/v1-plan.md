# v1 plan

**Started 2026-10-26.** v1 is the finish of the first build: a complete game, maybe small, with a clear route of progression and an end-game. Anything not listed here is "after v1". Details for each item are in `docs/future-design.md`.

## The plan (decided 2026-10-26)

1. **Build v1** so it feels like a proper game, even if small.
2. **Focus on the UI** once the v1 features are in.
3. **Outside playtesters** try it out.
4. **Then** add more after v1.

Notes:
- **Don't wait for "perfect" before outside testers.** Other players find problems you can't see yourself. Aim for "complete and playable", not "perfect".
- **How testers would get it:** either through the Plugin Hub (needs a review by RuneLite, which takes time, and a fresh check of the rules) or by running it from source (needs a dev setup, so only for technical friends). Decide closer to the time.

## The route of progression

```
Hire / unlock a character
  → level it from OSRS XP
  → first class advancement (Knight, Archer, Wizard, Harvester, ...)
  → second class advancement (Paladin, Berserker, ...)

Expeditions get harder along the way:
  safe solo trips → Skiller trips → party trips with threat → end-game raid (Barrows)

Gear gets better along the way:
  bundles drop gear → higher levels let you wear better rarities → stronger parties
```

## In v1

**Classes** (the ones designed so far):
- Built: Adventurer, Knight, Archer, Wizard, Skiller, Harvester, Artisan, Explorer.
- Second advancements: Paladin, Rune Knight, Berserker / Scout, Hunter, Trickster / Battle Mage, Cleric, Arcane Wizard.
- Special trees with perks: Thief → Bandit / Assassin → Warlord / Night Blade, and Vyre → Blood Mage / Death Knight / Shadow Lord.
- **Acolyte (added 2026-10-27):** a rare unlock linked to Prayer. Maybe: choose a god as the first advancement, then 3 classes per god (e.g. Guthix → Guardian of Guthix / Guthixian Dryad / Void Knight). See `docs/future-design.md`.

**Systems:**
- Second class advancement.
- Class stat bonuses (stat splits, same budget per tier).
- Gear level requirements.
- Perks (special classes only).
- Rare unlocks (Thief from Thieving/Agility XP, Vyre from XP in Morytania).
- Armour types (Plate / Leather / Robe): each class lists what it can wear. Replaces gear borrowing (decided 2026-10-27).
- Expedition rules: unlocks, class requirements, party size ranges.
- UI: card tabs, Expedition Board, compact roster.

**Expeditions:**
- **The first four (decided 2026-10-27, numbers in `docs/future-design.md`):** Scout the Encampment Perimeter, Explore Lumbridge Swamp Caves (threat 3), Looting the Dorgeshuun Caves (threat 8, first clear gives a free Skiller and unlocks hiring more with coins), Securing and Fortifying the Encampment (Skillers only). Each one has a bundle chance.
- Built so far: Swamp Caves (as a safe trip) and Dorgeshuun, both with older numbers.
- End-game: Plunder Tombs of the Barrow Brothers (needs a Vyre).
- **Gap:** more expeditions between Dorgeshuun (threat 8) and Barrows, so there's a ladder to climb. To design.

**Gear:**
- **System 1 (decided 2026-10-28):** 7 OSRS tiers per type (3 Common, 2 Uncommon, 2 Rare; e.g. Bronze to Dragon, Normal to Redwood bows, Sapphire to Zenyte trinkets) plus named uniques for Epic and Legendary (e.g. Abyssal whip, Scythe of Vitur). The full table is in `docs/future-design.md`.
- **To design:** the list of uniques, confirming the leather ladder, and the numbers (power, level requirements).
- **Not in v1:** System 2 (any rarity on any item, plus random perks). It comes after v1.

## Not in v1

- Beast Master and other new classes (keep a one-line list of new class ideas, don't design them).
- Rotating Expedition Board.
- Online features.
- Everything else in the Ideas Parking Lot in CLAUDE.md, unless moved here.

## Watch the size

This is a big v1: about 25 classes, 8 perks and around 8 new systems. That's fine, but build it in the same one-step-at-a-time way as Phases 1–6. If it starts to drag, the first thing to cut is one of the two special trees (Thief or Vyre), saving it for v1.1.

## Proposal: fewer advancements, more characters (2026-10-28, saved, not decided)

**The user's point:** v1 has about 39 classes on paper, but it feels like 5, because players feel each new *character* they get, not each advancement. Better to have more characters than deep advancement trees.

**Why not just add 30 more characters:** the code is cheap (a class is one line, and armour types made gear cheap). The cost is unlock methods (some need new detection code), perks (each is its own build step) and design time (XP skills, stats, gear and a reason to pick each one). A single player would also only see maybe 6–8 characters in v1.

**Claude's suggestion: every character gets exactly one class advancement in v1.**
- Adventurer → Knight / Archer / Wizard (the 9 second advancements wait)
- Skiller → Harvester / Artisan / Explorer
- Thief → Bandit / Assassin (Warlord and Night Blade wait)
- Vyre → Blood Mage / Death Knight / Shadow Lord
- Acolyte → choose a god: Guthix / Saradomin / Zamorak Acolyte (the 9 god classes wait)

That's about 20 classes and drops the "second class advancement" system from v1. The saved time goes into more starting characters, built with a cheap recipe: XP-based rare unlock, XP skills, armour and weapon types, stat split, and a perk only if it's special.

**User's response:** might be good, but saved for now. The user definitely wants a second advancement for *some* characters, and really likes **Warlord and Night Blade**. One option to consider: keep the second advancement only for the Thief tree in v1 (still builds the system, but with 2 classes instead of 20).

## Idea: every character comes from OSRS activity (user, 2026-10-28, not decided)

- **All characters are unlocked by doing things in OSRS**, not bought. E.g. Adventurers from combat XP, Skillers from skilling XP, Acolytes from Prayer, Thieves from Thieving and Agility (being built).
- **Coins pay for class advancements** instead of hires.
- **The Guild costs coins to level up:** when it has enough Guild XP it waits until you pay (e.g. 100 coins for level 2).
- **To work out before deciding:**
  - Guild levels mostly give hires today; without hiring, what does a Guild level give? (Storage, expedition unlocks, maybe a raised cap on characters.)
  - The first character: does the game still start you with one Adventurer?
  - The Dorgeshuun free Skiller and the 3-Adventurer cap were decided on 2026-10-27/28 assuming hiring. They'd need rethinking.
  - Coin sinks need to stay balanced with coin income (178 coins in two days of playtesting).

## Still to design before building

1. The expedition ladder: the first four are decided (2026-10-27). Still open: a tier between Dorgeshuun (threat 8) and Barrows, Guild unlock levels, and a few draft numbers.
2. The gear list and its numbers: System 1 is decided (2026-10-28). The tier ladders are decided too. Still open: the uniques, the leather ladder, and the numbers.
3. Stat bonuses for every class.
4. Level caps for each advancement.
5. Build order for all of the above.
