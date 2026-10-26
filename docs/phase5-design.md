# Phase 5–6 design: gear, loot and dangerous expeditions

**Status (2026-10-25):** the structure is decided by the user. Numbers marked *draft* are starting points for playtesting. Open questions are at the bottom.

**Split:** Phase 5 builds gear, bundles, storage and equipping (stats are shown but don't matter yet). Phase 6 makes them matter: threat levels, parties, success or failure, and injuries.

## 1. Stats

- **ATK Power** – mostly from weapons. Covers all offence, including magic weapons.
- **DEF Power** – mostly from armour.
- **MAG Power** – from mage gear and some trinkets. For now it counts **half towards ATK and half towards DEF**, so mages get some protection without much DEF.
- **Power comes from gear only** (character level doesn't count).
- A character's **effective ATK** = ATK + half its MAG. **Effective DEF** = DEF + half its MAG.
- A party's power is the **average** of its members' effective ATK and DEF (not the total), so one weak member pulls the party down.

## 2. Rarity, power and salvage

Kept super simple for the first playtest (decided 2026-10-25): every item has **one stat**, set by its slot, and the amount is set by its rarity.

| Rarity | Power | Salvage value (*draft*) |
|---|---|---|
| Common | 1 | 5 coins |
| Uncommon | 3 | 15 coins |
| Rare | 6 | 40 coins |
| Epic | 10 | 100 coins |
| Legendary | 15 | 200 coins |

- **Weapons** give ATK Power only.
- **Armour** gives DEF Power only.
- **Trinkets** give MAG Power only.

## 3. Items: 7 categories, one per rarity = 35 items

Item names are the rarity plus the type, e.g. "Rare Bow" or "Legendary Trinket" (decided 2026-10-25; real names can come later). **"Generic"** gear has no class requirement, so anyone can use it, including Adventurers. For now only trinkets are Generic; there will be a mix of class and Generic gear later.

| Type | Slot | Class | Stat | Common / Uncommon / Rare / Epic / Legendary |
|---|---|---|---|---|
| Sword | Weapon | Knight | ATK | 1 / 3 / 6 / 10 / 15 |
| Plate | Armour | Knight | DEF | 1 / 3 / 6 / 10 / 15 |
| Bow | Weapon | Archer | ATK | 1 / 3 / 6 / 10 / 15 |
| Leather | Armour | Archer | DEF | 1 / 3 / 6 / 10 / 15 |
| Staff | Weapon | Wizard | ATK | 1 / 3 / 6 / 10 / 15 |
| Robe | Armour | Wizard | DEF | 1 / 3 / 6 / 10 / 15 |
| Trinket | Trinket | Generic | MAG | 1 / 3 / 6 / 10 / 15 |

For now the classes' gear is identical apart from who can wear it. Making classes feel different (e.g. Wizards trading DEF for MAG) can come after playtesting.

## 4. Bundles

Every bundle gives some coins plus one item. Rarity is rolled first, then one item of that rarity. For now **every bundle can roll any item** (decided 2026-10-25); item pools per bundle come later.

| Bundle | How you get it | Chance | Coins inside (*draft*) | Common / Uncommon / Rare / Epic / Legendary (*draft*) |
|---|---|---|---|---|
| Generic Bundle | Every 10,000 OSRS XP gained (all skills added together; was 100,000 at 10% until 2026-10-26, which felt too rare) | 25% | 10–30 | 50 / 30 / 15 / 4 / 1 % |
| Slayer Bundle | Finish a Slayer task | 10% | 10–30 | 50 / 30 / 15 / 4 / 1 % |
| Raid Bundle | Finish any raid: Chambers of Xeric, Theatre of Blood, Tombs of Amascut, The Fractured Archive | 10% | 30–80 | 0 / 30 / 40 / 22 / 8 % |
| Boss Bundle | Kill a boss | 1% | 10–40 | 30 / 35 / 25 / 8 / 2 % |
| Clue Bundle | Finish any clue scroll | 25% (flat, any tier) | 15–40 | 40 / 35 / 20 / 4 / 1 % |

(The Fractured Archive is new to Claude's knowledge: check its real completion message when building the raid trigger.)

## 5. Storage and equipping

- Storage starts at **10 items**, shared by the account. Equipped items don't use storage.
- From **Guild level 4**, more storage can be bought: **+5 slots** per purchase, starting at **400 coins** and **doubling** each time (400, 800, 1,600, ...).
- Each character has a Weapon, Armour and Trinket slot. Class gear can only be equipped by that class; Generic gear by anyone.
- Any stored item can be salvaged for coins.
- When storage is full, new bundles wait unopened until there's room. Nothing is lost.

## 6. Phase 6: dangerous expeditions (*draft*)

- Every expedition has a **Threat Level** (how dangerous it is). This is separate from requirements: if you meet the requirements, you can send anyone, whatever the threat.
- Each expedition sets its **party size** (some need several characters; Looting the Dorgeshuun Caves needs exactly 3). Party power is the **average** of the members' effective ATK and DEF.
- **Success:** if the party's ATK ≥ threat, it always succeeds. Below that, the chance of failing grows with the gap (*draft*: fail chance = (threat − ATK) ÷ threat). Failing means no loot.
- **Injuries after a success:** if effective DEF ≥ threat, nobody is hurt. Below that, there's a chance someone is injured (*draft*: (threat − DEF) ÷ threat), and lower-DEF members are more likely to be the one.
- **Injuries after a failure:** DEF ≥ threat means injuries are rare. DEF below threat means a high chance that several members are injured.
- **Injured characters** rest for **5× the expedition's length** (Looting the Dorgeshuun Caves: 2 hours, so 10 hours of rest). They earn **no XP** while resting and can't go on expeditions.
- **First dangerous expedition: Looting the Dorgeshuun Caves** (renamed from "Mory Raid" on 2026-10-26). **Threat level 8**, needs **exactly 3 characters**, 2 hours. Rewards 3,000 XP, 40–100 coins and a 20% chance of a Boss Bundle.

For scale: a character with a Rare weapon (ATK 6) and a Rare trinket (MAG 6, so +3 ATK) has effective ATK 9. So a party of three around that level clears its threat 8 reliably, while a party in Common gear (about ATK 1.5 each) almost always fails.

**Decided 2026-10-26:**
- **Rewards on success:** each member gets the XP (3,000 each). The coins and the Boss Bundle chance are rolled once for the account.
- **Failure:** no XP and no loot, plus the injury risk.
- **Injury rolls (*draft*):** with p = (threat − party DEF) ÷ threat:
  - Success, DEF ≥ threat: nobody is hurt.
  - Success, DEF < threat: one roll at chance p; if it hits, one member is hurt, picked with lower-DEF members more likely.
  - Failure, DEF ≥ threat: 10% chance one member is hurt.
  - Failure, DEF < threat: each member is hurt with chance 50% + p ÷ 2 (so at least 50%).

## 7. Open questions

None blocking Phase 5. Still to decide later: real item names, and whether rewards (XP, coins) are split between party members or given to each.
