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
	private static final GearItem EPIC_PLATE = new GearItem(GearType.PLATE, Rarity.EPIC);
	private static final GearItem UNCOMMON_TRINKET = new GearItem(GearType.TRINKET, Rarity.UNCOMMON);

	@Test
	public void newCharactersWearNothing()
	{
		FellowshipCharacter knight = new FellowshipCharacter(CharacterClass.KNIGHT, 0);
		assertNull(knight.getEquipped(GearType.Slot.WEAPON));
		assertEquals(0, knight.getAtk());
		assertEquals(0, knight.getDef());
		assertEquals(0, knight.getMag());
	}

	@Test
	public void eachSlotGivesItsStat()
	{
		FellowshipCharacter knight = new FellowshipCharacter(CharacterClass.KNIGHT, 0);
		knight.equip(RARE_SWORD);
		knight.equip(EPIC_PLATE);
		knight.equip(UNCOMMON_TRINKET);
		assertEquals(6, knight.getAtk());
		assertEquals(10, knight.getDef());
		assertEquals(3, knight.getMag());
	}

	@Test
	public void equippingOverAnItemGivesTheOldOneBack()
	{
		FellowshipCharacter knight = new FellowshipCharacter(CharacterClass.KNIGHT, 0);
		GearItem commonSword = new GearItem(GearType.SWORD, Rarity.COMMON);
		assertNull(knight.equip(commonSword));
		assertEquals(commonSword, knight.equip(RARE_SWORD));
		assertEquals(RARE_SWORD, knight.getEquipped(GearType.Slot.WEAPON));
	}

	@Test
	public void unequippingEmptiesTheSlot()
	{
		FellowshipCharacter knight = new FellowshipCharacter(CharacterClass.KNIGHT, 0);
		knight.equip(RARE_SWORD);
		assertEquals(RARE_SWORD, knight.unequip(GearType.Slot.WEAPON));
		assertNull(knight.getEquipped(GearType.Slot.WEAPON));
		assertNull(knight.unequip(GearType.Slot.WEAPON));
	}

	@Test
	public void classGearOnlyFitsItsClass()
	{
		FellowshipCharacter wizard = new FellowshipCharacter(CharacterClass.WIZARD, 0);
		assertFalse(wizard.canEquip(RARE_SWORD));
		assertTrue(wizard.canEquip(new GearItem(GearType.STAFF, Rarity.RARE)));
	}

	@Test(expected = IllegalArgumentException.class)
	public void equippingTheWrongClassIsRefused()
	{
		new FellowshipCharacter(CharacterClass.WIZARD, 0).equip(RARE_SWORD);
	}

	@Test
	public void adventurersCanOnlyWearGenericGear()
	{
		FellowshipCharacter adventurer = new FellowshipCharacter(CharacterClass.ADVENTURER, 0);
		assertFalse(adventurer.canEquip(RARE_SWORD));
		assertTrue(adventurer.canEquip(UNCOMMON_TRINKET));
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
		assertFalse(vault.storeItem(EPIC_PLATE));
		assertEquals(10, vault.getItems().size());
	}

	@Test
	public void equippedGearSurvivesSavingAndLoading()
	{
		Gson gson = new Gson();
		FellowshipCharacter knight = new FellowshipCharacter(CharacterClass.KNIGHT, 500);
		knight.equip(RARE_SWORD);
		knight.equip(UNCOMMON_TRINKET);

		FellowshipCharacter loaded = gson.fromJson(gson.toJson(SavedCharacter.from(knight)), SavedCharacter.class).toCharacter();

		assertEquals(RARE_SWORD, loaded.getEquipped(GearType.Slot.WEAPON));
		assertNull(loaded.getEquipped(GearType.Slot.ARMOUR));
		assertEquals(UNCOMMON_TRINKET, loaded.getEquipped(GearType.Slot.TRINKET));
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
