# Playtest notes

Notes from playing, to sort into fixes and tuning after each playtest.

## Playtest 1 (2026-10-26, fresh save)

- **Bug, fixed:** logging out and back into the same account stopped all XP gains (committed `5ab7ca4`). Some XP from the playtest was lost to it.
- **Levelling felt slow.** The Adventurer reached level 9 (82,151 XP). Still to find out: did the whole climb feel slow, or mostly the last stretch?
  - Why the end drags: each early level costs 50% more than the last, so level 9 → 10 alone (38,444 XP) is a third of all the XP to level 10.
  - Fix options: flatten the early growth (e.g. 30% instead of 50%) so levels come more evenly, lower the total, or both.
  - **Decided 2026-10-26: keep the curve as it is for now.** The user will test more before changing it (the logout bug made this run feel slower than it is).
- **Design direction (user, 2026-10-26):** high levels should slow down a lot. Once you have a team, slower levels are fine, because you're spreading XP across more characters. Early levels should feel quick.
- **Bundles feel too rare (user, 2026-10-26).** Not a single bundle all day; opening things should happen more often. Idea: make bundles drop more often, but make the good loot inside rarer.
  - Why it's dry: the Generic Bundle is the only one that drops from normal play (10% per 100,000 OSRS XP, so about one per million XP). Slayer, clue, boss and raid bundles only drop from those activities.
  - Fix options: raise the Generic chance (e.g. 1 per 50,000–100,000 XP), and shift the rarity odds towards Common so a full set of good gear still takes time. Salvaging the extra Commons for coins gives them a use.
  - Watch out: more bundles fill the 10-slot storage faster. Check storage size and salvage value when tuning.
  - **Changed 2026-10-26 to test it:** Generic Bundle is now a 25% chance every 10,000 OSRS XP (about one per 40,000 XP, roughly 25 times more often). The loot inside is unchanged for now. Next to check: is opening them fun, does storage fill too fast, and should the odds lean more towards Common?
  - **This is a playtest setting, not the final number (user, 2026-10-26).** The final balance should land around **one bundle per 100,000 OSRS XP** (e.g. 100% every 100,000 XP, or a chance per smaller step that averages out to it). One per 40,000 XP is too generous for real play.
  - **Launch plan to keep it fair (user, 2026-10-26): two triggers that balance each other.** Bundles from every 100,000 XP *and* from OSRS level-ups. High-level players earn XP fast but rarely level up; low-level players earn less XP but level up constantly. Each side gets bundles from the trigger that suits them.
    - Level-ups need a **chance**, not a guaranteed bundle: early skill levels take minutes, so a new account could get dozens in its first hours. Draft: a small chance per level-up (e.g. 5–10%), to tune.
    - Maxed (99) skills never level up, so endgame players lean on the XP trigger. That's the intended split.
    - Already a Parking Lot idea ("More loot triggers: level-ups"); this makes it part of the launch balance.
  - **How often depends on what's inside (user, 2026-10-26).** If the Generic Bundle has very low odds of anything rare, it's fine for it to drop often: it's expected to be mostly low-level items to salvage for coins. So drop rate and loot odds get tuned together, not separately.
    - It all depends on the bundle system, which the user pictures as a mix of **MapleStory Classic drop tables, OSRS drops and clue scrolls**. Design that before settling the final rates. See also "Bundle loot pools" in `docs/future-design.md`.
