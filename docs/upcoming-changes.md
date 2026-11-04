# Upcoming change log (draft)

A list of changes the user is putting together (started 2026-11-03 on the Windows PC). **Nothing here is built or decided yet.** Don't action anything until the user goes through the list with Claude, one item at a time (planned on the MacBook). Items are written as the user gave them; Claude's notes, if any, are marked "Claude:".

When an item is worked through, mark it rather than deleting it: `(done 2026-11-05)`, `(moved to Parking Lot)` or `(dropped 2026-11-05: why)`.

## Changes

1. **Dorgeshuun dungeon bring down to 30mins.**
   - Claude: today it's 2 hours (`Expedition.DORGESHUUN_CAVES`). Its XP and rewards were set for 2 hours, so check whether they stay the same.
2. **Currently we have an expedition that if you add more skillers, it increases by 30mins. Lets remove that and flag any other skiller-expedition interactions we have.**
   - Claude: that's Secure and Fortify the Encampment. Each extra Skiller makes it 30 min *shorter* (1.5 h, 1 h, 30 min), through `shorterPerExtra` in `Expedition.java`. To flag when working through it: which expeditions take Skillers (only Fortify today), the per-member `partySize` save, the "Skillers only" rule (`Expedition.allows`), and Fortify's unlock (Guild level 2 plus a Skiller). Item 3 replaces this rule.
3. **Lets give Skillers a flat 25% increase in expedition speed when they are in a party.**
   - Claude: matches the brainstorm idea of "a flat % off per Skiller" (`brainstorm-2026-10-31-items-bundles-followers.md`). To decide: does it stack per Skiller (2 Skillers = 50%?), with a cap? Does it count on every expedition or only skilling ones? And does "25% faster" mean 25% less time, or 1.25× speed (20% less time)?

