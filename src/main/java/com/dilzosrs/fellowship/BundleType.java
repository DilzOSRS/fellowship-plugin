package com.dilzosrs.fellowship;

import java.util.Random;

/**
 * The kinds of loot bundle. Each gives some coins plus one random item. For now every
 * bundle can roll any item; only the rarity odds and coins differ.
 * <p>
 * The enum names are saved per account, so never rename them without a migration.
 */
public enum BundleType
{
	// Rarity odds in percent: Common, Uncommon, Rare, Epic, Legendary (must add up to 100).
	GENERIC("Generic Bundle", 10, 30, 50, 30, 15, 4, 1),
	SLAYER("Slayer Bundle", 10, 30, 50, 30, 15, 4, 1),
	RAID("Raid Bundle", 30, 80, 0, 30, 40, 22, 8),
	BOSS("Boss Bundle", 10, 40, 30, 35, 25, 8, 2),
	CLUE("Clue Bundle", 15, 40, 40, 35, 20, 4, 1);

	private final String displayName;
	private final int minCoins;
	private final int maxCoins;
	private final int[] rarityOdds;

	BundleType(String displayName, int minCoins, int maxCoins, int... rarityOdds)
	{
		this.displayName = displayName;
		this.minCoins = minCoins;
		this.maxCoins = maxCoins;
		this.rarityOdds = rarityOdds;
	}

	public String getDisplayName()
	{
		return displayName;
	}

	public int rollCoins(Random random)
	{
		return minCoins + random.nextInt(maxCoins - minCoins + 1);
	}

	public Rarity rollRarity(Random random)
	{
		int roll = random.nextInt(100);
		Rarity[] rarities = Rarity.values();
		for (int i = 0; i < rarities.length; i++)
		{
			roll -= rarityOdds[i];
			if (roll < 0)
			{
				return rarities[i];
			}
		}
		return Rarity.COMMON;
	}

	/** A random item: rarity from this bundle's odds, then any type of gear. */
	public GearItem rollItem(Random random)
	{
		Rarity rarity = rollRarity(random);
		GearType[] types = GearType.values();
		return new GearItem(types[random.nextInt(types.length)], rarity);
	}
}
