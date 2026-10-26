package com.dilzosrs.fellowship;

import java.util.Random;
import net.runelite.api.gameval.ItemID;

/**
 * The kinds of loot bundle. Each gives some coins plus one random item. For now every
 * bundle can roll any item; only the rarity odds and coins differ.
 * <p>
 * The enum names are saved per account, so never rename them without a migration.
 */
public enum BundleType
{
	// Icon (a clue reward casket, fancier for better bundles), drop chance in percent
	// (Generic: per 100,000 OSRS XP; the others: per completion or kill), coins, then rarity odds
	// in percent: Common, Uncommon, Rare, Epic, Legendary (must add up to 100).
	GENERIC("Generic Bundle", ItemID.TRAIL_REWARD_CASKET_EASY, 10, 10, 30, 50, 30, 15, 4, 1),
	SLAYER("Slayer Bundle", ItemID.TRAIL_REWARD_CASKET_MEDIUM, 10, 10, 30, 50, 30, 15, 4, 1),
	RAID("Raid Bundle", ItemID.TRAIL_REWARD_CASKET_MASTER, 10, 30, 80, 0, 30, 40, 22, 8),
	BOSS("Boss Bundle", ItemID.TRAIL_REWARD_CASKET_ELITE, 1, 10, 40, 30, 35, 25, 8, 2),
	CLUE("Clue Bundle", ItemID.TRAIL_REWARD_CASKET_HARD, 25, 15, 40, 40, 35, 20, 4, 1);

	private final String displayName;
	private final int iconItemId;
	private final int dropChance;
	private final int minCoins;
	private final int maxCoins;
	private final int[] rarityOdds;

	BundleType(String displayName, int iconItemId, int dropChance, int minCoins, int maxCoins, int... rarityOdds)
	{
		this.displayName = displayName;
		this.iconItemId = iconItemId;
		this.dropChance = dropChance;
		this.minCoins = minCoins;
		this.maxCoins = maxCoins;
		this.rarityOdds = rarityOdds;
	}

	public String getDisplayName()
	{
		return displayName;
	}

	/** The OSRS item whose icon stands for this bundle in pop-ups, until there's custom art. */
	public int getIconItemId()
	{
		return iconItemId;
	}

	/** Chance in percent of getting this bundle each time its trigger happens. */
	public int getDropChance()
	{
		return dropChance;
	}

	/** Roll the drop chance once. */
	public boolean rollDrop(Random random)
	{
		return random.nextInt(100) < dropChance;
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
