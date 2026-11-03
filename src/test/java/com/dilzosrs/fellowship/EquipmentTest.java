package com.dilzosrs.fellowship;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.Test;

public class EquipmentTest
{
	private static final GearItem RARE_SWORD = new GearItem(GearType.SWORD, Rarity.RARE);
	private static final GearItem RARE_PLATE = new GearItem(GearType.PLATE, Rarity.RARE);
	private static final GearItem UNCOMMON_TRINKET = new GearItem(GearType.TRINKET, Rarity.UNCOMMON);
	/** Level 15: high enough for any item that exists (Rare). */
	private static final long LEVEL_15 = LevelCurve.xpForLevel(15);

	@Test
	public void newCharactersWearNothing()
	{
		FellowshipCharacter knight = new FellowshipCharacter(CharacterClass.KNIGHT, 0);
		assertNull(knight.getEquipped(GearType.Slot.WEAPON));
		// Only the Knight's class stats.
		assertEquals(1, knight.getAtk());
		assertEquals(3, knight.getDef());
		assertEquals(0, knight.getMag());
	}

	@Test
	public void gearStatsAddUp()
	{
		FellowshipCharacter knight = new FellowshipCharacter(CharacterClass.KNIGHT, LEVEL_15);
		knight.equip(RARE_SWORD);
		knight.equip(RARE_PLATE);
		knight.equip(UNCOMMON_TRINKET);
		// Rune sword 5 ATK 2 DEF, Rune platebody 7 DEF, Silver necklace 3 MAG, plus the Knight's 1 ATK and 3 DEF.
		assertEquals(6, knight.getAtk());
		assertEquals(12, knight.getDef());
		assertEquals(3, knight.getMag());
	}

	@Test
	public void equippingOverAnItemGivesTheOldOneBack()
	{
		FellowshipCharacter knight = new FellowshipCharacter(CharacterClass.KNIGHT, LEVEL_15);
		GearItem commonSword = new GearItem(GearType.SWORD, Rarity.COMMON);
		assertNull(knight.equip(commonSword));
		assertEquals(commonSword, knight.equip(RARE_SWORD));
		assertEquals(RARE_SWORD, knight.getEquipped(GearType.Slot.WEAPON));
	}

	@Test
	public void unequippingEmptiesTheSlot()
	{
		FellowshipCharacter knight = new FellowshipCharacter(CharacterClass.KNIGHT, LEVEL_15);
		knight.equip(RARE_SWORD);
		assertEquals(RARE_SWORD, knight.unequip(GearType.Slot.WEAPON));
		assertNull(knight.getEquipped(GearType.Slot.WEAPON));
		assertNull(knight.unequip(GearType.Slot.WEAPON));
	}

	@Test
	public void classGearOnlyFitsItsClass()
	{
		FellowshipCharacter wizard = new FellowshipCharacter(CharacterClass.WIZARD, LEVEL_15);
		assertFalse(wizard.canEquip(RARE_SWORD));
		assertTrue(wizard.canEquip(new GearItem(GearType.STAFF, Rarity.RARE)));
	}

	@Test(expected = IllegalArgumentException.class)
	public void equippingTheWrongClassIsRefused()
	{
		new FellowshipCharacter(CharacterClass.WIZARD, LEVEL_15).equip(RARE_SWORD);
	}

	@Test
	public void adventurersCanOnlyWearGenericGear()
	{
		FellowshipCharacter adventurer = new FellowshipCharacter(CharacterClass.ADVENTURER, LEVEL_15);
		assertFalse(adventurer.canEquip(RARE_SWORD));
		assertTrue(adventurer.canEquip(UNCOMMON_TRINKET));
		assertTrue(adventurer.canEquip(new GearItem(GearType.DAGGER, Rarity.RARE)));
		assertTrue(adventurer.canEquip(new GearItem(GearType.CLOTHES, Rarity.UNCOMMON)));
	}

	@Test
	public void levelRequirements()
	{
		FellowshipCharacter knight = new FellowshipCharacter(CharacterClass.KNIGHT, LevelCurve.xpForLevel(10));
		assertTrue(knight.canEquip(new GearItem(GearType.SWORD, Rarity.COMMON)));
		assertTrue(knight.canEquip(new GearItem(GearType.SWORD, Rarity.UNCOMMON)));
		assertFalse(knight.canEquip(RARE_SWORD));
		assertTrue(knight.canUse(RARE_SWORD));
	}

	@Test(expected = IllegalArgumentException.class)
	public void equippingBelowTheLevelIsRefused()
	{
		new FellowshipCharacter(CharacterClass.KNIGHT, 0).equip(RARE_SWORD);
	}

	@Test
	public void gearAboveTheLevelStaysOnButGivesNothing()
	{
		// A level 12 Adventurer in a Mithril dagger (Uncommon, level 10) takes its class advancement and drops to level 1.
		FellowshipCharacter adventurer = new FellowshipCharacter(CharacterClass.ADVENTURER, LevelCurve.xpForLevel(12));
		GearItem mithrilDagger = new GearItem(GearType.DAGGER, Rarity.UNCOMMON);
		adventurer.equip(mithrilDagger);
		assertEquals(2, adventurer.getAtk());
		assertTrue(adventurer.evolve(CharacterClass.ARCHER));
		assertEquals(mithrilDagger, adventurer.getEquipped(GearType.Slot.WEAPON));
		assertFalse(adventurer.meetsLevel(mithrilDagger));
		// Only the Archer's own 3 ATK until level 10.
		assertEquals(3, adventurer.getAtk());
		adventurer.addXp(LevelCurve.xpForLevel(10));
		assertEquals(5, adventurer.getAtk());
	}

	@Test
	public void storageHandsItemsOutAndTakesThemBack()
	{
		Vault vault = new Vault(Collections.emptyList(), List.of(RARE_SWORD), 10, 0);
		assertEquals(RARE_SWORD, vault.takeItem(0));
		assertTrue(vault.getItems().isEmpty());
		assertNull(vault.takeItem(0));
		assertTrue(vault.storeItem(RARE_SWORD));
		assertEquals(1, vault.getItems().size());
	}

	@Test
	public void fullStorageRefusesItems()
	{
		List<GearItem> full = new ArrayList<>(Collections.nCopies(10, RARE_SWORD));
		Vault vault = new Vault(Collections.emptyList(), full, 10, 0);
		assertFalse(vault.storeItem(RARE_PLATE));
		assertEquals(10, vault.getItems().size());
	}

	@Test
	public void equippedGearSurvivesSavingAndLoading()
	{
		Gson gson = new Gson();
		FellowshipCharacter knight = new FellowshipCharacter(CharacterClass.KNIGHT, LEVEL_15);
		knight.equip(RARE_SWORD);
		knight.equip(UNCOMMON_TRINKET);

		FellowshipCharacter loaded = gson.fromJson(gson.toJson(SavedCharacter.from(knight)), SavedCharacter.class).toCharacter();

		assertEquals(RARE_SWORD, loaded.getEquipped(GearType.Slot.WEAPON));
		assertNull(loaded.getEquipped(GearType.Slot.ARMOUR));
		assertEquals(UNCOMMON_TRINKET, loaded.getEquipped(GearType.Slot.TRINKET));
	}

	@Test
	public void savedGearAboveTheLevelStaysOn()
	{
		Gson gson = new Gson();
		FellowshipCharacter loaded = gson.fromJson("{\"characterClass\":\"KNIGHT\",\"xp\":10,"
			+ "\"weapon\":{\"type\":\"SWORD\",\"rarity\":\"RARE\"}}", SavedCharacter.class).toCharacter();
		assertEquals(RARE_SWORD, loaded.getEquipped(GearType.Slot.WEAPON));
		assertEquals(1, loaded.getAtk());
	}

	@Test
	public void oldSavesWithoutGearStillLoad()
	{
		Gson gson = new Gson();
		FellowshipCharacter loaded = gson.fromJson("{\"characterClass\":\"KNIGHT\",\"xp\":10}", SavedCharacter.class).toCharacter();
		assertNull(loaded.getEquipped(GearType.Slot.WEAPON));
		assertEquals(10, loaded.getXp());
	}

	@Test
	public void savedGearTheClassCantUseIsLeftOff()
	{
		Gson gson = new Gson();
		FellowshipCharacter loaded = gson.fromJson("{\"characterClass\":\"WIZARD\",\"xp\":10,"
			+ "\"weapon\":{\"type\":\"SWORD\",\"rarity\":\"RARE\"}}", SavedCharacter.class).toCharacter();
		assertNull(loaded.getEquipped(GearType.Slot.WEAPON));
	}
}
