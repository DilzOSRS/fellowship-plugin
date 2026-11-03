# v1 bundles (B3, 2026-11-02)

Roadmap step B3. Sources, drop chances and rarity odds decided by the user on 2026-11-02 (after comparing with Claude's suggestions), then a first high-level split of item pools. Epic and Legendary items don't exist yet; the user plans to add the rare high-end items before v1 launches.

Odds are Common / Uncommon / Rare / Epic / Legendary, in percent.

## How a bundle opens (2026-11-02)
1. **Coins**, as today.
2. **Item chance (new, user):** the Basic Bundle has a **50/50 chance of containing an item** at all (playtest value). Other bundles may get a similar treatment. If it gets an item:
3. **Roll the tier** with the bundle's odds.
4. **If that tier is empty in this bundle's pool, drop to the nearest lower tier** (decided). So odds and pools don't have to match exactly, and Epic/Legendary items can be added later without touching the odds.
5. **Pick one item from that tier**, using that bundle's **weights** (see below).

## Sources, chances and odds (decided 2026-11-02)

| Bundle | Where it comes from | Chance | Odds (C / U / R / E / L) |
|---|---|---|---|
| **Basic Bundle** (renamed from Generic Bundle) | Every 10,000 OSRS XP; Swamp Caves success; Fortify | 25% per 10k XP; 100% per Swamp Caves success; 25% Fortify. **50% chance it holds an item** | 80 / 15 / 5 / 0 / 0 |
| **Slayer** | Each Slayer task kill, plus one on the final kill | 1% per kill + guaranteed on the final kill | 50 / 30 / 15 / 4 / 1 |
| **Clue** | Any finished clue scroll | 100% | 30 / 45 / 20 / 4 / 1 |
| **Boss** | Any boss kill; Dorgeshuun | **5% per boss kill**; 100% per Dorgeshuun run | 20 / 45 / 30 / ~~5~~ 4 / 1 (changed 2026-11-02 when built: it added up to 101, so Epic went to 4 as suggested) |
| **Raid** | Any in-game raid | 100% | 5 / 40 / 40 / 10 / 5 |
| **Trade** | The Merchant's GE trip (5 per trip) | Always | 40 / 50 / 10 / 0 / 0 |
| **Trade+** (new) | A new "Trade+" expedition (to design) | ? | 0 / 40 / 50 / 7 / 3 (user may revise) |
| **Barrows** (planned) | The Barrows Raid: 1 per real Barrows chest opened while it runs | 1 per chest | 10 / 60 / 25 / 4 / 1 |
| **Sailing 1** (planned) | The Sailing Dungeon | ? | 50 / 30 / 15 / 4 / 1 |

- **The user's rarity odds are used everywhere for now** (2026-11-02). Claude's alternatives are kept at the bottom for reference.
- **Clue Bundles, later (user):** maybe several kinds of Clue Bundle, or more bundles for higher clue tiers (e.g. an elite clue gives more than a beginner one). That would also stop quick beginner clues being the best way to farm them.

## Item pools: first high-level split (user, 2026-11-02)

"Adventurer gear" = Sword, Bow, Staff, Plate, Leather, Robe. "Generic" = Dagger and Clothes (Trinkets listed separately). Tiers in brackets.

| Bundle | Pool |
|---|---|
| **Basic** | Generic (C, U, R); Adventurer gear (C, U, R). **No Trinkets\*** |
| **Slayer** | Generic (C, U, R, E); Adventurer gear (C, U, R, E); Trinkets (C, U); *slayer* items (later; maybe some new *nautical* and *dark* items too) |
| **Clue** | Should lean towards rarer items. Generic (C, U, R, E); Adventurer gear (C, U, R, E); Trinkets (C, U, R); maybe **Generic+** items |
| **Boss** | Generic (**U, R**; no Commons); Adventurer gear (C, U, R, E); Trinkets (C, R, E); *dark* and *nautical* weapons (R), **rarer than other Rare items** (weighted) |
| **Raid** | Same as Boss for now, but *dark* and *nautical* weapons at R and E (still weighted rarer). More will be added to the pool before v1 (e.g. "the best weapons", Legendary uniques) |
| **Trade** | Adventurer gear (C, U) |
| **Trade+** | Generic (C, U, R, E); Adventurer gear (C, U, R, E); Trinkets (C, U, R); maybe Generic+ items |
| **Barrows** | Dark Generic (C, U, R); Dark melee, Dark magic, Dark ranged weapons (C, U, R, E); Dark armour (C, U, R, E); Generic (C, U); Adventurer weapons and armour (U, R, E) |
| **Sailing 1** | Generic (C, U); *nautical* weapons (U, R); Adventurer armour (C, U, R) |

\* **Basic and Trinkets:** the user is unsure; revisit in a later round of tweaking before v1.

### Definitions
- **Dark Generic** (user, 2026-11-02): plain *dark* items with no style limit, so every Vyre can use them; if it's a weapon, the Thief tree's t2s (Bandit, Assassin) may be able to use it too, depending on its hidden style. (This is the "dark-generic" rung of the tag ladder.)
- **Generic+** (idea): special high-tier Generic items that break the "Generic is 1 point under" rule on purpose. Needs designing.
- **Dark gear grows:** Barrows lists Common and Epic *dark* weapons, *dark* armour and Dark Generic items. B1/B2 had *dark* weapons at U and R only, with *dark* armour parked. Update the item list when looping back to dark items.

### Weights (decided 2026-11-02)
Inside a tier, every item normally has the same chance. A **weight** makes some items come up more or less often (an item with weight 4 gets 4 "tickets" in the draw, one with weight 1 gets 1).

- **Each bundle sets its own weights** (user, 2026-11-02). The same item can be common in one bundle and rare in another: e.g. *dark* weapons are a rare surprise in Boss and Raid (low weight), but the main event in Barrows (normal or high weight).
- Example, the Boss Bundle's Rare pool: 8 normal items (6 Adventurer, the Rune dagger, the Gold necklace) and 7 special ones (3 *dark* weapons, 4 *nautical* weapons). Boss rolls Rare 30% of the time.

| Weights (normal / special) | Special items' share of Rare drops | One normal item, per Boss Bundle | One special item, per Boss Bundle |
|---|---|---|---|
| 1 / 1 (no weights) | 47% | 2.0% | 2.0% |
| 2 / 1 | 30% | 2.6% | 1.3% |
| 4 / 1 | 18% | 3.1% | 0.8% |

- Without weights, the "rare" special weapons would be almost half of all Rare drops, because there are 7 of them.
- The exact weights are a tuning choice (Claude's suggestion: start Boss and Raid at 4 / 1). **Built at 4 / 1 (2026-11-02)**, to tune in playtesting.

## Notes
- **Drop chances went up a lot overall** (Clue 25% → 100%, Boss 1% → 5%, Raid 10% → 100%). That fits "finding bundles is fun" (tutorial note). **Storage** (10 slots) will fill fast, so "salvage all Common" or a bigger starting Bank moves up the list. The Basic Bundle's 50/50 item chance helps a little.
- **Dorgeshuun gives a Boss Bundle every run**, so a threat-8 expedition has about a 6% chance of Epic/Legendary each run. Level requirements (Epic 20–25, Legendary 30+) stop those being worn early, so they'd sit in the Bank as goals.
- **Basic at 80/15/5** is the clear "wide and low" bundle with no Epic/Legendary, which matches the wide-vs-deep idea.
- "Sailing 1" suggests more sailing bundles later.
- **More bundles to consider (Claude, 2026-11-02):** a Wilderness Bundle (a *dark* source before Barrows), a Dorgeshuun Bundle (its own instead of sharing Boss), a Minigame Bundle, Clue tiers, and a bundle for the missing threat ~10–12 expedition. The user wants to see how the item split settles first.
- **Trade+:** what it costs and who can run it.

## Claude's earlier suggestions (for reference, not used)
| Bundle | Suggestion | Reason |
|---|---|---|
| Clue | Beginner/easy give a Basic Bundle; medium and up a Clue Bundle | Quick clues could be farmed |
| Barrows | 0 / 60 / 30 / 8 / 2 | Commons are salvage for an end-game party; many chests per raid keeps odds modest |
| Sailing 1 | 0 / 60 / 35 / 4 / 1 | No Common nautical items exist |

## History
- 2026-11-02: Sources, odds and pools decided in claude.ai.
- 2026-11-02: Built in code (Stage C step 4, not tested in-game yet) for the bundles that exist: Basic (renamed from Generic, 50% item chance), Slayer, Clue, Boss, Raid and Trade. Trade+, Barrows and Sailing 1 wait for their expeditions. Boss Epic 5 → 4 so the odds add up to 100. Weights 4 / 1 for Boss and Raid. Found: Uncommon nautical weapons (Anchor, Cutlass, Harpoon, Trident) are in no pool until Sailing 1 exists, so the Sailor tree only gets Rare ones from Boss and Raid Bundles. Boss and raid kill triggers (5.4) still aren't built, so only Dorgeshuun gives Boss Bundles today.
- 2026-11-02: Boss and raid kill triggers built (boss kill count message 5%, raid completion message 100%), so Boss Bundles no longer come only from Dorgeshuun. Uncommon nautical: the user will change it later (in the CLAUDE.md Parking Lot).
