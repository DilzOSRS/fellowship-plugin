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

(Nothing yet.)

## 3. Gear

Wait for the Phase 5–6 playtest notes before redesigning gear.

(Nothing yet.)
