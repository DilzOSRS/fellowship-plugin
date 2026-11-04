# Upcoming change log (draft)

A list of changes the user is putting together (started 2026-11-03 on the Windows PC). **Nothing here is built or decided yet.** Don't action anything until the user goes through the list with Claude, one item at a time (planned on the MacBook). Items are written as the user gave them; Claude's notes, if any, are marked "Claude:".

When an item is worked through, mark it rather than deleting it: `(done 2026-11-05)`, `(moved to Parking Lot)` or `(dropped 2026-11-05: why)`.

## Changes

1. **Dorgeshuun dungeon bring down to 30mins.** (done 2026-11-03: rewards kept the same, the user's choice)
   - Claude: today it's 2 hours (`Expedition.DORGESHUUN_CAVES`). Its XP and rewards were set for 2 hours, so check whether they stay the same.
2. **Currently we have an expedition that if you add more skillers, it increases by 30mins. Lets remove that and flag any other skiller-expedition interactions we have.** (done 2026-11-03: Fortify is always 1.5 h, then item 3 added the Skiller speed-up on top)
   - Claude: that's Secure and Fortify the Encampment. Each extra Skiller makes it 30 min *shorter* (1.5 h, 1 h, 30 min), through `shorterPerExtra` in `Expedition.java`. To flag when working through it: which expeditions take Skillers (only Fortify today), the per-member `partySize` save, the "Skillers only" rule (`Expedition.allows`), and Fortify's unlock (Guild level 2 plus a Skiller). Item 3 replaces this rule.
   - Claude (2026-11-03), every other place Skillers and expeditions meet today:
     - Fortify takes the whole Skiller tree and nobody else (`Expedition.allows`), 1 to 3 of them. More Skillers now just means more followers earning its 5,000 XP.
     - Fortify unlocks at Guild level 2 plus a Skiller, and its first run lets the 2nd Adventurer join.
     - Skillers can only start joining after the Swamp Caves' first run (the rescue).
     - **Skillers can go on Swamp Caves and Dorgeshuun** (anyone can). The v1 plan says dangerous trips take Combat followers, but the code doesn't stop a Skiller filling a Dorgeshuun slot. Worth deciding.
     - Each member still saves its `partySize`, which set the trip length. It does nothing now; item 3 will likely replace it.
3. **Lets give Skillers a flat 25% increase in expedition speed when they are in a party.** (done 2026-11-03: 25% less time per Skiller-tree follower, stacking, on every expedition; only 3 count)
   - Claude: matches the brainstorm idea of "a flat % off per Skiller" (`brainstorm-2026-10-31-items-bundles-followers.md`). To decide: does it stack per Skiller (2 Skillers = 50%?), with a cap? Does it count on every expedition or only skilling ones? And does "25% faster" mean 25% less time, or 1.25× speed (20% less time)?
4. **When you get a Sailor the first time, you unlock a new expedition called "Prep for voyage".** A safe expedition that requires a Sailor. You can add others as well; each additional follower increases speed by 10%. (Added 2026-11-03 on the MacBook.)
   - The idea is you're building a ship and other things, so more people make prepping faster.
   - Claude: this is "Prepping for Sailing Dungeon" from the brainstorm doc and `expedition-chart.md`. It changes one thing from there: the design said "faster with Skillers", this says any follower adds 10%. To decide: its length, XP and rewards; the biggest party; whether a Skiller in the party gets item 3's 25% on top of the 10%; and whether it has to be the base Sailor or any of the Sailor tree.
5. **Once "Prep for voyage" is completed, it's replaced by a new dangerous dungeon expedition called "Sailing Voyage".** Requires a Sailor and 2 others. (Added 2026-11-03 on the MacBook.)
   - Claude: this is the "Sailing Dungeon Voyage" in the design docs. The threat was **never decided**: the notes say "15–17, not decided" (pushed up from 12, which felt too low), with simulated fail and injury odds at 15, 17 and 18 in `expedition-chart.md`. Also still open there: the 3 stages of 30 min each, Sailing 1 Bundles (not built yet), and whether "a Sailor" means the whole Sailor tree.

