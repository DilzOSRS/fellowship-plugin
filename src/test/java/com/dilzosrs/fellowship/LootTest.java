package com.dilzosrs.fellowship;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.Collections;
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
		assertEquals("Magic shortbow", bow.getName());
		assertEquals(7, bow.getAtk());
		assertEquals("7 ATK", bow.getStatText());
		assertEquals("bow", GearType.BOW.getTagText());

		GearItem sword = new GearItem(GearType.SWORD, Rarity.UNCOMMON);
		assertEquals("Mithril sword", sword.getName());
		assertEquals("3 ATK, 1 DEF", sword.getStatText());

		GearItem trinket = new GearItem(GearType.TRINKET, Rarity.RARE);
		assertEquals("Gold necklace", trinket.getName());
		assertEquals("6 MAG", trinket.getStatText());
		assertEquals("Generic", GearType.TRINKET.getTagText());
		assertEquals("nautical-melee", GearType.CUTLASS.getTagText());
	}

	@Test
	public void levelToWearByRarity()
	{
		assertEquals(1, Rarity.COMMON.getLevelRequirement());
		assertEquals(10, Rarity.UNCOMMON.getLevelRequirement());
		assertEquals(15, Rarity.RARE.getLevelRequirement());
	}

	@Test
	public void itemsMatchTheirBudgets()
	{
		// Common 2, Uncommon 4, Rare 7 for a single tag; Generic -1, combo +1. The Dagger and Anchor are under on purpose.
		int[] budget = {2, 4, 7};
		for (GearType type : GearType.values())
		{
			int adjust = type.isGeneric() ? -1 : type.isCombo() ? 1 : 0;
			for (Rarity rarity : new Rarity[]{Rarity.COMMON, Rarity.UNCOMMON, Rarity.RARE})
			{
				GearTier tier = type.getTier(rarity);
				if (tier == null || type == GearType.DAGGER || type == GearType.ANCHOR)
				{
					continue;
				}
				assertEquals(type + " " + rarity, budget[rarity.ordinal()] + adjust,
					tier.getAtk() + tier.getDef() + tier.getMag());
			}
		}
	}

	@Test(expected = IllegalArgumentException.class)
	public void noItemAtAMissingTier()
	{
		new GearItem(GearType.ANCHOR, Rarity.COMMON);
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
	public void thereAre34Items()
	{
		// The design's 40, minus the 6 dark weapons that are on hold.
		int items = 0;
		for (GearType type : GearType.values())
		{
			for (Rarity rarity : Rarity.values())
			{
				items += type.hasTier(rarity) ? 1 : 0;
			}
		}
		assertEquals(34, items);
	}

	@Test
	public void rarityOddsAddUpToOneHundred()
	{
		// Rolling every number from 0 to 99 must land on a rarity with that many rolls each.
		int[][] odds = {
			{80, 15, 5, 0, 0}, {50, 30, 15, 4, 1}, {5, 40, 40, 10, 5}, {20, 45, 30, 4, 1}, {30, 45, 20, 4, 1}, {40, 50, 10, 0, 0}};
		BundleType[] bundles = {BundleType.GENERIC, BundleType.SLAYER, BundleType.RAID, BundleType.BOSS, BundleType.CLUE,
			BundleType.TRADE};
		for (int b = 0; b < bundles.length; b++)
		{
			int[] counts = new int[Rarity.values().length];
			for (int roll = 0; roll < 100; roll++)
			{
				counts[bundles[b].rollRarity(always(roll)).ordinal()]++;
			}
			assertEquals(bundles[b].toString(), java.util.Arrays.toString(odds[b]), java.util.Arrays.toString(counts));
		}
	}

	@Test
	public void basicBundleHasNoTrinketsOrNauticalGear()
	{
		for (GearItem item : BundleType.GENERIC.getPoolItems())
		{
			assertFalse(item.getType() == GearType.TRINKET);
			assertFalse(item.getType().getTags().contains(Tag.NAUTICAL));
		}
		assertTrue(BundleType.GENERIC.getPoolItems().contains(new GearItem(GearType.DAGGER, Rarity.COMMON)));
	}

	@Test
	public void bossBundlePool()
	{
		List<GearItem> pool = BundleType.BOSS.getPoolItems();
		// Generic gear from Uncommon up, no Common Dagger.
		assertFalse(pool.contains(new GearItem(GearType.DAGGER, Rarity.COMMON)));
		assertTrue(pool.contains(new GearItem(GearType.DAGGER, Rarity.UNCOMMON)));
		// Trinkets at Common and Rare, not Uncommon.
		assertTrue(pool.contains(new GearItem(GearType.TRINKET, Rarity.COMMON)));
		assertFalse(pool.contains(new GearItem(GearType.TRINKET, Rarity.UNCOMMON)));
		// Nautical weapons at Rare only.
		assertTrue(pool.contains(new GearItem(GearType.CUTLASS, Rarity.RARE)));
		assertFalse(pool.contains(new GearItem(GearType.CUTLASS, Rarity.UNCOMMON)));
	}

	@Test
	public void nauticalWeaponsAreRarerInBossBundles()
	{
		// Rare pool: 6 Adventurer, Rune dagger, Gold necklace at 4 tickets each (32) and 4 nautical at 1 (4).
		Random random = new Random(3);
		int rares = 0;
		int nautical = 0;
		for (int i = 0; i < 50_000; i++)
		{
			GearItem item = BundleType.BOSS.rollItem(random);
			if (item.getRarity() == Rarity.RARE)
			{
				rares++;
				nautical += item.getType().getTags().contains(Tag.NAUTICAL) ? 1 : 0;
			}
		}
		double share = nautical / (double) rares;
		assertTrue("nautical share " + share, share > 0.08 && share < 0.14);
	}

	@Test
	public void tradeBundlesDropToUncommon()
	{
		// Trade's pool stops at Uncommon, so its 10% Rare rolls give an Uncommon item.
		Random random = new Random(5);
		for (int i = 0; i < 5_000; i++)
		{
			GearItem item = BundleType.TRADE.rollItem(random);
			assertTrue(item.getRarity() != Rarity.RARE);
		}
	}

	@Test
	public void basicBundleHoldsAnItemHalfTheTime()
	{
		assertTrue(BundleType.GENERIC.rollHasItem(always(49)));
		assertFalse(BundleType.GENERIC.rollHasItem(always(50)));
		assertTrue(BundleType.BOSS.rollHasItem(always(99)));
	}

	@Test
	public void anEmptyBundleStillGivesCoins()
	{
		Vault vault = Vault.empty();
		vault.addBundle(BundleType.GENERIC);
		Vault.Opened opened = vault.open(BundleType.GENERIC, always(99));
		assertNotNull(opened);
		assertNull(opened.item);
		assertTrue(vault.getItems().isEmpty());
		assertEquals(30, opened.coins);
	}

	@Test
	public void everyItemDropsSomewhere()
	{
		Set<GearItem> dropped = new java.util.HashSet<>();
		for (BundleType bundle : BundleType.values())
		{
			dropped.addAll(bundle.getPoolItems());
		}
		// Uncommon nautical weapons wait for the planned Sailing 1 Bundle.
		for (GearType type : GearType.values())
		{
			for (Rarity rarity : Rarity.values())
			{
				boolean waitsForSailing = type.getTags().contains(Tag.NAUTICAL) && rarity == Rarity.UNCOMMON;
				if (type.hasTier(rarity) && !waitsForSailing)
				{
					assertTrue(type + " " + rarity, dropped.contains(new GearItem(type, rarity)));
				}
			}
		}
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
		Vault vault = new Vault(Collections.emptyList(), List.of(new GearItem(GearType.BOW, Rarity.RARE)), 10, 0);
		assertEquals(40, vault.salvage(0));
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
			List.of(new GearItem(GearType.STAFF, Rarity.RARE)), 15, 42_000);

		Vault loaded = gson.fromJson(gson.toJson(SavedVault.from(original)), SavedVault.class).toVault();

		assertEquals(List.of(BundleType.RAID, BundleType.GENERIC), loaded.getBundles());
		assertEquals(List.of(new GearItem(GearType.STAFF, Rarity.RARE)), loaded.getItems());
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
	public void oldSavedGearMovesToTheNearestTier()
	{
		// Saves from before the item list (2026-11-02) can hold Epic and Legendary gear.
		Gson gson = new Gson();
		Vault loaded = gson.fromJson("{\"items\":[{\"type\":\"SWORD\",\"rarity\":\"EPIC\"},"
			+ "{\"type\":\"TRINKET\",\"rarity\":\"LEGENDARY\"}]}", SavedVault.class).toVault();
		assertEquals(List.of(new GearItem(GearType.SWORD, Rarity.RARE), new GearItem(GearType.TRINKET, Rarity.RARE)),
			loaded.getItems());
	}

	@Test
	public void bigXpDropsRollMoreThanOnce()
	{
		Vault vault = Vault.empty();
		assertEquals(3, vault.addXpForGenericBundles(35_000, always(0)));
		assertEquals(5_000, vault.getGenericXpProgress());
	}
}
