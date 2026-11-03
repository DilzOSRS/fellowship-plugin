# Playtest checklist

Things built but not tested in-game yet. The user keeps building, then tests everything in one go. Tick each box when it works; write anything odd in `docs/playtest-notes.md`. When a whole section passes, mark it tested in `CLAUDE.md` (and the design doc's History, if it has one).

## From before 2026-11-02
- [ ] **Fortify with 2 and 3 Skillers.** The trip takes 1 h with 2 Skillers and 30 min with 3 (1 Skiller was 1.5 h, already tested). Advanced Skiller classes count too.
- [ ] **Clue trigger.** Finishing any clue gives a Clue Bundle (now every time, see below).

## Stage C step 4: bundle pools (built 2026-11-02)
- [ ] The Bank says **"Basic Bundle"** instead of "Generic Bundle".
- [ ] **Opening Basic Bundles:** about half give coins only, with "no item this time" in chat, a pop-up and the Bank. The rest give an item.
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
