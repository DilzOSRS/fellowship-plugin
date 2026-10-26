# Playtest notes

Notes from playing, to sort into fixes and tuning after each playtest.

## Playtest 1 (2026-10-26, fresh save)

- **Bug, fixed:** logging out and back into the same account stopped all XP gains (committed `5ab7ca4`). Some XP from the playtest was lost to it.
- **Levelling felt slow.** The Adventurer reached level 9 (82,151 XP). Still to find out: did the whole climb feel slow, or mostly the last stretch?
  - Why the end drags: each early level costs 50% more than the last, so level 9 → 10 alone (38,444 XP) is a third of all the XP to level 10.
  - Fix options: flatten the early growth (e.g. 30% instead of 50%) so levels come more evenly, lower the total, or both.
  - **Decided 2026-10-26: keep the curve as it is for now.** The user will test more before changing it (the logout bug made this run feel slower than it is).
- **Design direction (user, 2026-10-26):** high levels should slow down a lot. Once you have a team, slower levels are fine, because you're spreading XP across more characters. Early levels should feel quick.
