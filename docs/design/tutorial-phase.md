# The tutorial phase (internal note, 2026-11-01)

**The tutorial phase = the opening path** (CLAUDE.md, `docs/future-design.md` "The opening path"). It's how a new game starts, and it ends with the first Fortify the Encampment run and the second Adventurer.

1. Start with one Adventurer, with no expeditions and no hiring.
2. Its first class advancement (t2: Knight, Archer or Wizard) unlocks **Explore the Lumbridge Swamp Caves**.
3. The first Swamp Caves run (success or failure) **rescues someone**, and Skillers can start joining by chance.
4. Guild levels cost coins (a "Level up" button).
5. **Guild level 2 plus a Skiller** unlocks **Fortify the Encampment**.
6. The first Fortify run lets a **second Adventurer** join from combat XP.

## Bundles before t2 (user, 2026-11-01)
- Finding only about 1 bundle on the way to the first class advancement is probably too low. Finding bundles is fun, so players should have **received more bundles on average by the time the Adventurer reaches t2**. Revisit in the playtest, once bundles are balanced.

## Swamp Caves as the tutorial's dangerous expedition (user, 2026-11-01)
The first dangerous expedition should teach two things right away:
- **Completing an expedition gives rewards** (the rescue, which unlocks Skillers).
- **Success and failure, and injuries.** It shouldn't be a cakewalk: a potential injury is part of the lesson, while the run still gives the unlock.

**Decided (user, 2026-11-01): Swamp Caves goes to threat 3** (from 1). Not perfectly balanced, but good enough for now; tune after playtesting. Build it together with class base stats (roadmap Stage C step 2), since threat 3 assumes those stats exist.

Today Swamp Caves is threat 1, solo, 15 minutes (an injury rests a third of that, 5 minutes), with a Generic Bundle on every success.

### How outcomes work (from the code)
- **Success:** XP, coins and loot. Members can still be injured (chance = how far party DEF is below the threat). An injured member keeps the rewards, then rests.
- **Failure:** no XP, no coins, no loot, plus a high injury chance (or 10% if party DEF is at or above the threat).
- So a run ends one of four ways: success, success but injured, fail, or fail and injured.

### What a fresh t2 brings (Claude, 2026-11-01)
- Class stats: Knight 1 ATK 3 DEF, Archer 3 ATK 1 DEF, Wizard 2 ATK 2 DEF (from 4 MAG).
- Bundles before t2: reaching level 10 takes about 112k XP, so about 1 Generic Bundle at the planned launch rate (one per ~100k XP), about 3 at today's playtest rate (and likely more after the user's note above). Each gives one random item, often not one the class can use. So a fresh t2 has **0–1 usable Common items** today.
- Common class gear is worth 2 (e.g. +2 ATK from a weapon). **Generic Common gear is worth 1** (user, 2026-11-01).

### Threat 3, all four outcomes
| Follower | Success | Success, injured | Fail | Fail, injured |
|---|---|---|---|---|
| Knight, no gear (1 ATK, 3 DEF) | 33% | 0% | 60% | 7% |
| Archer, no gear (3 ATK, 1 DEF) | 33% | 67% | 0% | 0% |
| Wizard, no gear (2 ATK, 2 DEF) | 44% | 22% | 11% | 22% |
| Knight + Common sword (3 ATK) | 100% | 0% | 0% | 0% |
| Archer + Common bow (5 ATK) | 33% | 67% | 0% | 0% |
| Wizard + Common staff (4 ATK) | 67% | 33% | 0% | 0% |

- Each class learns its role: the Knight (tank) fails without a weapon but almost never gets hurt; the Archer (damage) always succeeds but often gets hurt; the Wizard sits in between. One Common weapon makes success certain. The rescue happens on the first run either way, and the injury only costs 5 minutes.
- Threat 4 was the alternative, but harsher: everyone without gear fails often and gets hurt a lot (Knight 75% fail, Archer 78% injured, Wizard 50% fail).
- Watch: an ungeared Knight fails 2 in 3 runs, which slows farming Generic Bundles from Swamp Caves until it finds a weapon. More bundles before t2 (see above) would help.
- To test in-game once class stats exist: what a real fresh t2 has when it reaches Swamp Caves, then tune the threat.
