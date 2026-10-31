# v1 plan

**Rewritten 2026-10-31.** v1 is the version we launch on the Plugin Hub: public, enjoyable, and something a player can feel they've **completed**. It ends with an end-game raid. Anything not listed here is "after v1". Details for each part go in `docs/future-design.md`.

## The route of progression

```
A follower joins from OSRS activity (combat, skilling or a rare unlock)
  → level it from OSRS XP → class advancement
  → better gear from bundles
  → expeditions get harder: safe trips → Skiller trips → dangerous party trips
  → the end-game: the Barrows Raid
```

## What v1 needs (the user's list, 2026-10-31)

### 1. Tags for followers and items (the foundation)
- **Every follower is Combat or Skiller.** Expeditions say which they take (dangerous ones take Combat, safe ones can take Skillers).
- **Items get tags**, e.g. a sword tagged *melee* and *shadow*.
- **Each class lists the tags it can use.** Knights can use *melee*; Vyres can use *shadow*.
- **The rule (the user's proposal, 2026-10-31, replacing "must match every tag"):** a follower can use an item if it matches **any one** of its tags. Some tags are **combo tags** like *shadow-melee*, which only count if the follower has **both** parts.
  - A sword tagged *shadow* and *melee*: anyone with shadow **or** melee can use it (versatile).
  - A sword tagged *shadow-melee*: only followers with shadow **and** melee (exclusive). A Vyre with shadow but not melee can't use it.
  - Claude's suggestion: keep combos to 2 parts, and use them for rare and unique items so most gear stays easy to share.
- This replaces today's "which classes can wear this gear type" lists (`GearType`), and is built together with the item list (see the build order).

### 2. Items, tiers and stats
- Sort out the weapon and item list, the tiers (Bronze to Dragon, etc., plus named uniques) and the numbers.
- The numbers must reach the end-game: the Barrows Raid is threat 20, and today's best gear gives 15 power. Set the stat scale with threat 20 in mind.

### 3. Bundles
- Many kinds of bundle. **What makes each one unique: its item pool** (likely by tags, e.g. a Barrows Bundle drops shadow gear).
- For v1, aim for one bundle per source (each expedition and each OSRS trigger), not dozens.

### 4. Fellowship levels
- More Fellowship (Guild) levels, each one unlocking something (expeditions, storage, followers).
- **Constructing buildings** alongside the Fellowship level is the user's idea for later; v1 focuses on a plain level system first.

### 5. The expedition ladder and the Barrows Raid
- Expeditions that bridge the gap from today's trips (Dorgeshuun is threat 8) up to the raid (threat 20).
- **The Barrows Raid (working title):**
  - A party of 3 **Combat** followers, threat 20, **needs a Vyre**.
  - 30 minutes long.
  - **While it's running, every Barrows chest the player opens in OSRS = 1 Barrows Bundle** for the party when it comes back. Read from the game's own chat message (read-only, allowed).
  - This is a new kind of expedition: **the player's real play while the party is away decides the loot.**
- **A quest to unlock the raid (maybe):** a series of expeditions, each asking the player to do something in OSRS while it runs. It reuses the raid's "do X while they're away" mechanic, so build the raid first and the quest after.

### 6. More followers
- Many more ways to stumble on Combat and Skiller followers.
- Today there are 8 starting followers: Adventurer, Skiller, Thief, Vyre, Acolyte, Merchant, Sailor and Alchemist.

### Also needed to launch
- The UI pass (compact roster, cleaner cards).
- Wipe test saves and delete the old-save migration (see CLAUDE.md).
- A fresh rules check against the rejected-features wiki.

## Suggested build order (Claude, 2026-10-31)

1. Finish and test the opening path (steps 6 and 7).
2. **Design the item list on paper** (the user's call, 2026-10-31): every item with its tier, stats and a tags column. Writing the list is what shows which tags are needed.
3. **Build tags and items together** from that list, with the numbers set for threat 20. Adding a tag later is one line of code, so new items needing new tags is fine; only changing the tag *rule* would be costly.
4. **Bundles** with their item pools.
5. **The "do X while they're away" mechanic**, tested on a small expedition first.
6. **The expedition ladder**, then the **Barrows Raid**.
7. **Fellowship levels** and what each one unlocks (can be slotted in anywhere, since it's mostly numbers).
8. **More followers**, added along the way (each one is cheap once tags exist).
9. Launch prep: UI, save wipe, rules check.

## Open questions

1. **How long should v1 take** a normal player to finish (reach and beat the raid)? A week, a month? This sets every number.
2. **The raid with no chests:** if the player doesn't do Barrows during the 30 minutes, does the party come back with nothing, or a small base reward?
3. **Raid failure:** threat 20 can fail. Does a failed raid lose the bundles from the chests the player opened?
4. **How many followers for v1?** A number to aim for, e.g. 12 starting followers.
5. **How many tags?** A short list to start (e.g. *melee*, *ranged*, *magic*, *shadow*, *holy*, *skilling*).
6. **Second class advancements:** still undecided. The user really likes Warlord and Night Blade; one option is a second advancement for the Thief tree only.

## Not in v1

- Constructing buildings (after the plain Fellowship levels).
- Online features.
- Everything else in the Ideas Parking Lot in CLAUDE.md, unless moved here.

## Older notes (kept for reference)

- **2026-10-26 plan:** build v1, then the UI, then outside playtesters. Don't wait for "perfect" before outside testers.
- **Gear System 1 (decided 2026-10-28):** 7 OSRS tiers per type (3 Common, 2 Uncommon, 2 Rare) plus named uniques for Epic and Legendary. System 2 (any rarity on any item, random perks) is after v1. Full table in `docs/future-design.md`.
- **Armour types** (Plate / Leather / Robe, decided 2026-10-27) will become tags.
- **Fewer advancements, more followers** (proposal 2026-10-28, not decided): one class advancement per follower in v1, and spend the saved time on more followers.
- **The encampment route** (2026-10-28) became the opening path, now built (see CLAUDE.md).
