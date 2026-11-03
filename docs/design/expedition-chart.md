# Expedition chart (2026-11-01)

Every expedition, built and planned. The built ones are copied from the code (`Expedition.java`). **XP rates and bundle chances are all going to change soon** (user, 2026-11-01), so treat those columns as placeholders. Threat checks for these are in `roadmap-items-bundles-stats.md` ("A4 results"); revisit that once more expeditions are designed (after bundles).

## In the game today

| Expedition | Who can go | Party | Length | Threat | XP each | Coins | Bundle | Unlocks when |
|---|---|---|---|---|---|---|---|---|
| Explore the Lumbridge Swamp Caves | Anyone | 1 | 15 min | 1 (→ 3 planned) | 1,000 | 10–50 | Generic, every success | First class advancement. First run rescues someone (Skillers can join) |
| Secure and Fortify the Encampment | Skiller tree | 1–3 | 1.5 h, −30 min per extra Skiller | Safe | 5,000 | None | 25% Generic | Guild level 2 + a Skiller. First run lets a 2nd Adventurer join |
| Loot the Dorgeshuun Caves | Anyone | Exactly 3 | 2 h | 8 (user: fine as is) | 3,000 | 40–100 | 20% Boss | You have 3 followers |
| Traveling to trade at the Grand Exchange | Merchant only | 1 | 30 min | Safe | 1,000 | Costs 100, gives none | 5 Trade Bundles | First Merchant |
| Transmute metals | Alchemist only | 1 | 10 min | Safe | 500 | 2× the salvage value of the item it carries | None | First Alchemist |

## Planned

| Expedition | Who can go | Threat | Length | Rewards | Status |
|---|---|---|---|---|---|
| Prepping for Sailing Dungeon | Sailor + Skillers | Safe | Faster with Skillers | Builds the boat, unlocks the voyage | Designed, name TBD |
| Sailing Dungeon Voyage | Party with a Sailor (likely) | **15–17** (pushed up from ~12; user felt 12 too low) | 3 stages: 30 / 30 / 30 min | Sailing Bundles (*nautical* gear) | Designed, name TBD |
| Slayer Master's hunt | Slayer Master | TBD (safe or flat threat) | As long as your real Slayer task | Your kills decide the loot | Idea |
| Barrows Raid | 3 Combat, needs a Vyre | 20 | 30 min | 1 Barrows Bundle per real Barrows chest opened while it runs | v1 end-game |
| One or two rungs between 8 and 15 | ? | ~10–12 | ? | ? | **Needed** |

## How party power works (from the code)
- Party ATK and DEF are the **average** of the members, not the sum. So threat numbers are per follower: threat 15 means the average member needs about 15 ATK. A weak member drags everyone down, and bigger parties aren't stronger.

## Notes
- **Things feel too easy (user, 2026-11-01):** fails are rare and injuries are common, so a party almost always gets loot. Two reasons: the fail rule is forgiving (a party with half the needed ATK still wins half the time), and an injury costs little (rest is a third of the trip, and the injured follower keeps its rewards on a success). **Parked for now.** Injury ideas: rest that grows with threat (e.g. Swamp Caves 1/3, Dorgeshuun 1×, Sailing 2×, Barrows 3×), injured followers lose that run's XP, pay coins to heal, a DEF penalty instead of resting. Fixing injuries first may be enough before touching the fail rule.
- The ladder table in `docs/future-design.md` is out of date in places (it still lists "Scout the Encampment Perimeter" first, and older Swamp Caves and Dorgeshuun numbers from before the opening path). Update it when the expedition ladder is redesigned.
- Open question from A4: who fills Dorgeshuun's 3 slots after the tutorial, if dangerous expeditions take Combat followers only and a third Adventurer never joins (options in the roadmap).
- Expedition details live in the brainstorm doc (Sailing Dungeon stages and perks, the Slayer Master's hunt) and `tutorial-phase.md` (Swamp Caves).

## Sailing Dungeon at higher threats (simulated, 2026-11-01)
| Party | Threat 15: fail / injured | Threat 17 | Threat 18 |
|---|---|---|---|
| Mixed Rare (Swashbuckler, Knight, Archer) | 13% / 32% | 24% / 47% | 28% / 52% |
| Sailor team, Rare, own *nautical* weapons | 1% / 26% | 13% / 42% | 18% / 48% |
| Swashbuckler + 2 Vyres, Rare | 0% / 40% | 0% / 47% | 6% / 53% |
| Mixed Uncommon | 43% / 72% | 50% / 78% | 53% / 81% |
| Mixed Epic | 0% / 0% | 0% / 0% | 0% / 6% |
- 15 = a real Rare-gear challenge, specialist gear rewarded. 17 = the last big step before the raid (Claude's sweet spot if it should be that). 18 crowds the raid at 20.
