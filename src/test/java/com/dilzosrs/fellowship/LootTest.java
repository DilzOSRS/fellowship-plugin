package com.dilzosrs.fellowship;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import org.junit.Test;

public class LootTest
{
	/** A Random that always returns the same number, to make rolls predictable in tests. */
	private static Random always(int value)
	{
		return new Random()
		{
			@Override
			public int nextInt(int bound)
			{
				return Math.min(value, bound - 1);
			}
		};
	}

	@Test
	public void itemNamesAndStats()
	{
		GearItem bow = new GearItem(GearType.BOW, Rarity.RARE);
		assertEquals("Rare Bow", bow.getName());
		assertEquals(6, bow.getPower());
		assertEquals("Archer, ATK 6", bow.getDescription());

		GearItem trinket = new GearItem(GearType.TRINKET, Rarity.LEGENDARY);
		assertEquals("Legendary Trinket", trinket.getName());
		assertEquals("Generic, MAG 15", trinket.getDescription());

		assertEquals("Knight, DEF 1", new GearItem(GearType.PLATE, Rarity.COMMON).getDescription());
	}

	@Test
	public void powerByRarity()
	{
		assertEquals(1, Rarity.COMMON.getPower());
		assertEquals(3, Rarity.UNCOMMON.getPower());
		assertEquals(6, Rarity.RARE.getPower());
		assertEquals(10, Rarity.EPIC.getPower());
		assertEquals(15, Rarity.LEGENDARY.getPower());
	}

	@Test
	public void classRulesForGear()
	{
		assertTrue(GearType.SWORD.canBeUsedBy(CharacterClass.KNIGHT));
		assertFalse(GearType.SWORD.canBeUsedBy(CharacterClass.WIZARD));
		assertFalse(GearType.ROBE.canBeUsedBy(CharacterClass.ADVENTURER));
		// Generic gear: anyone, including Adventurers.
		assertTrue(GearType.TRINKET.canBeUsedBy(CharacterClass.ADVENTURER));
		assertTrue(GearType.TRINKET.canBeUsedBy(CharacterClass.ARCHER));
	}

	@Test
	public void thereAre35Items()
	{
		assertEquals(35, GearType.values().length * Rarity.values().length);
	}

	@Test
	public void rarityOddsAddUpToOneHundred()
	{
		// Rolling every number from 0 to 99 must give a rarity each time, matching the odds.
		for (BundleType bundle : BundleType.values())
		{
			int legendaries = 0;
			for (int roll = 0; roll < 100; roll++)
			{
				if (bundle.rollRarity(always(roll)) == Rarity.LEGENDARY)
				{
					legendaries++;
				}
			}
			assertTrue(bundle + " has no legendary chance", legendaries > 0);
		}
	}

	@Test
	public void raidBundlesNeverGiveCommons()
	{
		for (int roll = 0; roll < 100; roll++)
		{
			assertFalse(BundleType.RAID.rollRarity(always(roll)) == Rarity.COMMON);
		}
	}

	@Test
	public void anyBundleCanRollAnyTypeAndRarity()
	{
		Random random = new Random(7);
		Set<GearType> types = EnumSet.noneOf(GearType.class);
		Set<Rarity> rarities = EnumSet.noneOf(Rarity.class);
		for (int i = 0; i < 20_000; i++)
		{
			GearItem item = BundleType.GENERIC.rollItem(random);
			types.add(item.getType());
			rarities.add(item.getRarity());
		}
		assertEquals(EnumSet.allOf(GearType.class), types);
		assertEquals(EnumSet.allOf(Rarity.class), rarities);
	}

	@Test
	public void openingPutsTheItemInStorage()
	{
		Vault vault = Vault.empty();
		vault.addBundle(BundleType.SLAYER);
		Vault.Opened opened = vault.open(BundleType.SLAYER, new Random(1));
		assertNotNull(opened);
		assertEquals(1, vault.getItems().size());
		assertEquals(opened.item, vault.getItems().get(0));
		assertTrue(opened.coins >= 10 && opened.coins <= 30);
		assertEquals(0, vault.countBundles(BundleType.SLAYER));
	}

	@Test
	public void cantOpenABundleYouDontHave()
	{
		Vault vault = Vault.empty();
		assertNull(vault.open(BundleType.RAID, new Random(1)));
	}

	@Test
	public void bundlesWaitWhenStorageIsFull()
	{
		List<GearItem> full = new ArrayList<>(Collections.nCopies(10, new GearItem(GearType.SWORD, Rarity.COMMON)));
		Vault vault = new Vault(List.of(BundleType.BOSS), full, 10, 0);
		assertTrue(vault.isFull());
		assertNull(vault.open(BundleType.BOSS, new Random(1)));
		// The bundle is kept, not lost.
		assertEquals(1, vault.countBundles(BundleType.BOSS));
	}

	@Test
	public void salvagingGivesCoinsAndFreesASlot()
	{
		Vault vault = new Vault(Collections.emptyList(), List.of(new GearItem(GearType.BOW, Rarity.EPIC)), 10, 0);
		assertEquals(100, vault.salvage(0));
		assertTrue(vault.getItems().isEmpty());
		assertEquals(0, vault.salvage(0));
	}

	@Test
	public void genericBundleRollsEveryTenThousandXp()
	{
		Vault vault = Vault.empty();
		// Always rolls 0, which is inside the 25% chance.
		assertEquals(0, vault.addXpForGenericBundles(9_999, always(0)));
		assertEquals(1, vault.addXpForGenericBundles(1, always(0)));
		assertEquals(0, vault.getGenericXpProgress());
		assertEquals(1, vault.countBundles(BundleType.GENERIC));
	}

	@Test
	public void genericBundleCanMiss()
	{
		Vault vault = Vault.empty();
		// Rolls 50, outside the 25% chance: the 10k XP is used up but no bundle is found.
		assertEquals(0, vault.addXpForGenericBundles(10_000, always(50)));
		assertEquals(0, vault.countBundles(BundleType.GENERIC));
	}

	@Test
	public void vaultSurvivesSavingAndLoading()
	{
		Gson gson = new Gson();
		Vault original = new Vault(List.of(BundleType.RAID, BundleType.GENERIC),
			List.of(new GearItem(GearType.STAFF, Rarity.EPIC)), 15, 42_000);

		Vault loaded = gson.fromJson(gson.toJson(SavedVault.from(original)), SavedVault.class).toVault();

		assertEquals(List.of(BundleType.RAID, BundleType.GENERIC), loaded.getBundles());
		assertEquals(List.of(new GearItem(GearType.STAFF, Rarity.EPIC)), loaded.getItems());
		assertEquals(15, loaded.getCapacity());
		assertEquals(42_000, loaded.getGenericXpProgress());
	}

	@Test
	public void unknownSavedLootIsSkipped()
	{
		Gson gson = new Gson();
		Vault loaded = gson.fromJson("{\"bundles\":[\"DRAGON\",\"BOSS\"],"
			+ "\"items\":[{\"type\":\"AXE\",\"rarity\":\"RARE\"},{\"type\":\"BOW\",\"rarity\":\"RARE\"}]}", SavedVault.class).toVault();
		assertEquals(List.of(BundleType.BOSS), loaded.getBundles());
		assertEquals(List.of(new GearItem(GearType.BOW, Rarity.RARE)), loaded.getItems());
	}

	@Test
	public void bigXpDropsRollMoreThanOnce()
	{
		Vault vault = Vault.empty();
		assertEquals(3, vault.addXpForGenericBundles(35_000, always(0)));
		assertEquals(5_000, vault.getGenericXpProgress());
	}
}
