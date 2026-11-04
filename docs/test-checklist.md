# Playtest checklist

Things built but not tested in-game yet. The user keeps building, then tests everything in one go. Tick each box when it works; write anything odd in `docs/playtest-notes.md`. When a whole section passes, mark it tested in `CLAUDE.md` (and the design doc's History, if it has one).

## From before 2026-11-02
- [ ] **Fortify with 2 and 3 Skillers.** ~~The trip takes 1 h with 2 Skillers and 30 min with 3~~ ~~1.5 h however many Skillers go~~ (changed again 2026-11-03, see the Skiller speed-up below).
- [ ] **Clue trigger.** Finishing any clue gives a Clue Bundle (now every time, see below).

## Stage C step 4: bundle pools (built 2026-11-02)
- [ ] The Bank says **"Basic Bundle"** instead of "Generic Bundle".
- [ ] **Opening Basic Bundles:** about half give coins only: **no pop-up**, just the chat message "You opened a Basic Bundle and find 16 coins but no items this time." (changed 2026-11-03 from playtest notes), and the Bank's last-opened line. The rest give an item.
- [ ] Basic Bundles never give trinkets or nautical weapons.
- [ ] **Clue:** finishing a clue always gives a Clue Bundle.
- [ ] **Dorgeshuun:** every successful run brings back a Boss Bundle.
- [ ] Bundles only give Common, Uncommon or Rare items (no Epic or Legendary items exist yet).
- [ ] Trade Bundles (Merchant's Grand Exchange trip) give only Common or Uncommon Adventurer gear.

## Boss and raid triggers (built 2026-11-02)
- [ ] **Boss kills:** about 1 in 20 gives a Boss Bundle. (To check quickly, ask Claude to turn it up to 100% for a test run, then back.)
- [ ] **Raids:** finishing Chambers of Xeric, Theatre of Blood or Tombs of Amascut always gives a Raid Bundle.
- [ ] **The Fractured Archive:** check its completion message. If it isn't "Your completed … count is: N.", tell Claude the exact wording.
- [ ] Barrows chests give nothing (Barrows gets its own bundle later).

## Dorgeshuun at 30 minutes (built 2026-11-03)
- [ ] **Loot the Dorgeshuun Caves** takes 30 minutes (was 2 hours). Rewards are the same: 3,000 XP each, 40–100 coins, a Boss Bundle on success. An injury rests 10 minutes.

## Skiller speed-up (built 2026-11-03)
Each Skiller-tree follower (Skiller, Harvester, Artisan, Explorer) in a party takes 25% off the trip, on every expedition. Old saves: a trip already running when you update takes its full length.
- [ ] **Fortify** with 1 / 2 / 3 Skillers takes about 1h 7m / 45 min / 22 min. The party picker says "Each Skiller in the party makes the trip 25% shorter." and "They'll be back in" shows the shorter time.
- [ ] **A Skiller's card** shows the shorter time on its Send buttons (e.g. Swamp Caves 11 min instead of 15). Other followers' cards show the full time.
- [ ] **Dorgeshuun with a Skiller** in the party takes about 22 min. Without one, 30 min. The Skiller hint doesn't show when no Skiller could go.
- [ ] An injury after a sped-up trip still rests a third of the **full** trip (e.g. Dorgeshuun 10 min, with or without a Skiller).
