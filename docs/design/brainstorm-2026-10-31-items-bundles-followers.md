# Brainstorm: items, bundles and followers (2026-10-31)

Ideas from chat sessions, not yet in the repo's CLAUDE.md or docs. Move what's decided into `docs/future-design.md` / `docs/v1-plan.md` when ready. The step-by-step plan for items, stats and bundles is in `roadmap-items-bundles-stats.md` (class stats and level requirements live there). Parts of this first brainstorm were refined later (see the roadmap, item list and bundles docs, which win where they disagree).

## Inspirations
- **MapleStory Classic:** each monster has its own drop list, mostly equipment. Where you get loot matters.
- **OSRS:** drop tables with lots of ordinary drops and a small, exciting unique table.
- **Flesh and Blood:** cards tagged with a class, a talent, both (e.g. "Shadow Brute"), a specific hero, or Generic. Very close to the v1 tag rule.
- **Destiny (user, 2026-11-01):** the weapons, gear and Light (Power) level system, likely a subconscious influence. Parallels: gear power vs an activity's recommended level is like party power vs threat; Destiny's rarities (Common to Exotic) match the 5 tiers. Ideas it could inspire later: Destiny allows only one Exotic equipped at a time (a "one Legendary per follower" rule?), and infusing old gear into new to raise its power.

## Bundles (decided in principle)
- **Wide vs deep.** Frequent bundles (Basic) have big pools of low-tier gear; rare bundles (Raid, Barrows) have small pools of high-tier gear. Rule of thumb: the rarer the bundle, the smaller and better its pool.
- **No materials.** Low-tier gear fills the tables MapleStory-style, and salvage turns it into coins.
- **A bundle = a tag filter + a tier range + a small unique list,** plus coins. A new item only needs tags and a tier to appear in the right bundles.
- Some bundles may be tied to specific expeditions (to think about; easier once the expedition ladder is sketched).
- Watch: wide bundles + 10-slot storage = lots of salvaging. Something like "salvage all Common" may be needed.
- The full bundle design is in `bundles-v1.md`.

## Tags (FaB-style)

### The power ladder (user, 2026-11-01)
The fewer followers can use an item, the stronger it is (budget numbers are in the roadmap):

| Item tag | Who can use it | Power |
|---|---|---|
| Generic (no tags) | Anyone | Weakest, but versatile. High-tier Generic gear should feel valuable. |
| *dark* | Any *dark* follower | Decent, versatile |
| *dark-leather* (combo) | Only followers with both | Stronger |
| *Death Knight* (class tag) | Only that class | Strongest / unique |

- A single talent tag like *dark* is really "dark-generic", but it just shows as *dark*.

### Visible and hidden tags (user, 2026-11-01)
- **Hidden tags** are internal and never shown to players, because they're obvious from the item: the slot (*weapon*, *armour*, *trinket*) and the style (*melee*, *ranged*, *magic*).
- **A follower's tag can be limited by hidden tags.** E.g. Bandit: *dark*, but only on melee weapons. Assassin: *dark*, but only on ranged weapons. Vyre: *dark* with no limit.
- This keeps Bandits and Assassins out of dark armour, and keeps the Assassin off dark swords.
- The rule in one line: **an item fits if the follower matches any one of its tags (both parts for a combo), and that follower tag's limit (if any) fits the item's hidden tags.**
- Code note: slot already exists on every item (`GearType.Slot`), so only the style needs adding.

Examples:
| Item | Visible | Hidden | Bandit | Assassin | Vyre |
|---|---|---|---|---|---|
| Dark sword | *dark* | weapon, melee | Yes | No | Yes |
| Dark bow | *dark* | weapon, ranged | No | Yes | Yes |
| Dark leather body | *dark-leather* | armour | No | No | Yes |

### Kinds of tag
1. **Single tags** (*sword*, *leather*, *dark*, ...): anyone with that tag can use it.
2. **Combo tags** (*dark-leather*, *nautical-ranged*): only followers with **both** parts.
3. **Class tags** (user, 2026-11-01): the item names one class, e.g. *Death Knight*, and only that class can use it (like FaB's hero-specific cards).
- **Generic** = no tags, anyone can use it (today's trinkets already work this way).

- **Talents:** *nautical* (whole Sailor tree, from t1), *dark* (whole Vyre tree from t1, Bandit tree, maybe a Wilderness follower later), *slayer* (Slayer Master).
- ***dark*, not *shadow* (decided 2026-11-01).** A shared umbrella tag for evil, forbidden, dangerous followers. *Shadow* suggests stealth, so it could become its own tag later for sneaky classes. Sharing means every *dark* follower wants the same drops, so a Barrows Bundle helps several followers. Idea: the real OSRS Dark bow could be a *dark* unique.
- **Weapon tags by type** (*sword*, *bow*, *staff*), with the style (*melee* etc.) as a hidden tag. Type tags make it easy to split later (daggers, scimitars), but a follower then needs a tag per weapon type it can use.
- **Item hover tooltip (user's idea, 2026-11-01):** hovering a gear square (card or Bank) shows a small item card: name in rarity colour, stat, visible tags, who can use it, level requirement, and why not if it can't be used. The squares already have a tooltip (`GearCell.show`), so it's a small job. Build it in the same step as tags.

## Levels and gear requirements (2026-11-01)
- **No level caps at all (user, 2026-11-01).** Every follower can keep levelling. An Adventurer can stay t1 past level 10 and **choose when to take its class advancement** (which resets it to level 1). This replaces the old caps (t1 at 10, XP wasted at the cap) and the Thief tree's planned caps (about 20, then higher); those become minimum levels to advance instead. Repo CLAUDE.md "Level cap" decision needs updating when this is built.
- **Gear needs a follower level to equip.** Advancing still resets to level 1, so a follower may be too low for its best gear for a while. That's fine, it gives something to work towards.
- Level numbers per tier: see the roadmap (A3).
- **Still open:** what happens to gear the follower is already wearing when it advances: stays on (only new gear is blocked; Claude's pick) or goes back to storage (harsher, and storage is small).

## Followers
**Combat:** Adventurer, Sailor, Thief, Vyre, **Slayer Master (new)**.
**Non-combat:** Skiller, Merchant, Alchemist, Acolyte (might become combat, or fit a "support" role since Zealots help through blessings).

### Sailor tree (user, 2026-11-01)
The Vyre works because it unlocks one piece of content (the Barrows Raid) that drops its own gear. The Sailor gets the same kind of anchor. Stats for the whole tree are in the roadmap (Sailor 1/1; Swashbuckler 4 ATK 1 DEF; Harpooner 5 ATK; Navigator 1 ATK 1 DEF 3 MAG). The whole tree has the *nautical* tag.
- **Step 1: "Prepping for Sailing Dungeon"** (working name; the user called it "Build a Ship/Boat"). A safe skilling expedition that the Sailor unlocks. ~~Like Fortifying the Encampment, it gets faster with Skillers in the party, up to a max party size.~~ (built 2026-11-03 as **"Prep for voyage"**: any follower helps, 10% off each, then Skillers' 25%; up to 5; one-time.)
- **Step 2: once the boat is built, it becomes the "Sailing Dungeon Voyage"** (working name; real name to come). A **medium-tier dangerous expedition** (threat 15–17, see `expedition-chart.md`) that drops **Sailing Bundles** with *nautical* gear.

**The voyage has stages (user, 2026-11-01).** One expedition, shown in three parts:
| Stage | Time | t2 perk (idea) |
|---|---|---|
| Sail to the dungeon | 30 min | **Navigator** speeds up the sailing stages |
| Run the dungeon | 30 min | **Swashbuckler** speeds up the dungeon stage |
| Sail back | 30 min | (Navigator again) |
| On return | | **Harpooner** brings back extra loot |

Claude's notes:
- Simple to build as one timer split into stages: the Events tab shows which stage the party is on ("Sailing to…", "In the dungeon", "Sailing home"). The success/injury roll stays one roll, as today.
- Perks that only work on voyages do nothing elsewhere. That's accepted (same as the Vyre's Barrows perks), and the stage idea could spread to other multi-part expeditions later.
- If a party has a Navigator and a Swashbuckler, the trip gets much shorter. Decide whether two of the same class stack, and maybe cap the total, like the Skiller speed-up.
- "Extra loot" is easiest as an extra Sailing Bundle roll, since expeditions don't have loot tables yet.

Still to decide: the real names; whether the dungeon needs a Sailor-tree follower in the party (like Barrows needs a Vyre; Claude says yes); its party size and stage lengths; whether building the boat is one-time (then replaced by the dungeon) or repeatable.

### Skiller speed-up rule (user, 2026-11-01): revisit the Skiller
- **Each Skiller in a skilling expedition cuts its time by a flat %** (e.g. 10%), instead of a fixed number of minutes set per expedition. One rule for every safe expedition, however long it is.
- This becomes the **Skiller's perk**, and t2 Skillers or other non-combat followers can have better ones (e.g. 20%).
- Claude's suggestions: add the percentages up (3 Skillers at 10% = 30% off), and cap the total cut (e.g. 50–75%) so stacked perks can't make safe expeditions nearly instant.
- Everyone is sent at the start. **Joining a running expedition is parked:** complex to build and balance, and every future safe expedition would inherit it.
- Fortify the Encampment currently cuts fixed time (1.5 h, 1 h, 30 min). Switch it to the % rule later.

### Slayer Master (user, 2026-11-01; replaces "Monster Slayer")
The user likes the idea but isn't fully bought in yet; it may grow once it's played with.
- **A single-tier combat follower, "Slayer Master",** with no class advancement (at least for now), like the Merchant and Alchemist.
- No level cap (no follower has one now).
- **Stats: net 4** (a t2-level budget from day one); the split is still to decide. Options floated: 2 ATK 2 DEF, 3 ATK 1 DEF, or 2 ATK 1 DEF 1 MAG.
- **Goes on task with you:** when you get a real Slayer task, it heads out on a matching hunt, stays away as long as your task lasts, and your kills add to what it brings back. No timer; your real play decides the length. Reuses the "do X while they're away" mechanic planned for the Barrows Raid (could be its small test).
- **The choice it creates (user):** send it on normal expeditions, since it's a strong t1-level combat follower, or keep it for hunts while you do Slayer on your account.
- **Slayer gear:** a *slayer* tag, with Slayer Bundles as the natural source.
- **Better Slayer Bundles** while it's on the team, with uniques for other classes, so it helps the whole Fellowship.
- **Unlock (suggestion):** a chance per Slayer task completed (the plugin already reads Slayer tasks for the bundle trigger).
- Open: is the hunt safe or dangerous? If dangerous, a flat threat is simplest (a threat per Slayer monster would need a big table).

### Ideas held back or parked
- **Region followers** (Fremennik, Desert, Prifddinas, Keldagrim, Wilderness, Kourend): liked, but held for later, maybe tied to a new raid. A Wilderness follower could share the *dark* tag.
- **Beast Master:** good, but pet drops are too rare. Idea: Hunter XP at the usual rare odds, with a pet drop as a guaranteed or big-chance shortcut.
- **Hero / Champion:** interesting, but needs a repeatable unlock, not one-off quests. Ideas so far (boss kills, minigame wins, fame from dangerous expeditions) haven't clicked.
- **Wanderer:** a chance to join from moving around (e.g. per new map region visited). Unlock is fun, but no job for it has clicked yet.
- Other ideas floated: Treasure Hunter (clues), Bard (music tracks), Cook, Builder, Angler, Pyromancer, Druid.
- **Support followers** as a third kind (alongside Combat and Skiller): an idea, only if it excites.
- **Joining a running expedition** (see the Skiller speed-up rule).
- **Item perks** (user, 2026-11-01): parked, low priority.

## History
- 2026-11-03: Sailor step 1 built as "Prep for voyage"; any follower helps (10% each), not only Skillers.
