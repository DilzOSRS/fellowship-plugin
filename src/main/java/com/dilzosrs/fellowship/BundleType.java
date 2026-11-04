package com.dilzosrs.fellowship;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import net.runelite.api.gameval.ItemID;

/**
 * The kinds of loot bundle (docs/design/bundles-v1.md). Each gives coins and usually one item:
 * roll the tier with the bundle's odds, drop to the nearest lower tier its pool has, then pick
 * one item from that tier by the bundle's weights.
 * <p>
 * The enum names are saved per account, so never rename them without a migration.
 */
public enum BundleType
{
	// Icon (a clue reward casket, fancier for better bundles), drop chance in percent
	// (Basic: per 10,000 OSRS XP; Slayer: see SlayerLoot; the others: per completion or kill), coins,
	// chance in percent it holds an item, then rarity odds in percent: Common, Uncommon, Rare, Epic,
	// Legendary (must add up to 100). Odds and chances decided by the user on 2026-11-02.
	// Shown as "Basic Bundle" since 2026-11-02; the saved name stays GENERIC. Holds an item half the time.
	GENERIC("Basic Bundle", ItemID.TRAIL_REWARD_CASKET_EASY, 25, 10, 30, 50, 80, 15, 5, 0, 0),
	SLAYER("Slayer Bundle", ItemID.TRAIL_REWARD_CASKET_MEDIUM, 0, 10, 30, 100, 50, 30, 15, 4, 1),
	RAID("Raid Bundle", ItemID.TRAIL_REWARD_CASKET_MASTER, 100, 30, 80, 100, 5, 40, 40, 10, 5),
	// The design's odds added up to 101, so Epic went from 5 to 4.
	BOSS("Boss Bundle", ItemID.TRAIL_REWARD_CASKET_ELITE, 5, 10, 40, 100, 20, 45, 30, 4, 1),
	CLUE("Clue Bundle", ItemID.TRAIL_REWARD_CASKET_HARD, 100, 15, 40, 100, 30, 45, 20, 4, 1),
	// Bought on the Merchant's Grand Exchange trip. It never drops, and gives an item but no coins,
	// so the trip can't make more coins than it costs.
	TRADE("Trade Bundle", ItemID.TRAIL_REWARD_CASKET_BEGINNER, 0, 0, 0, 100, 40, 50, 10, 0, 0),
	// "Sailing 1" in the design. Only from the Sailing Voyage (every success). Placeholder icon:
	// a crate of smuggled rum. No Common nautical items exist, so a Common roll gives other gear.
	SAILING("Sailing Bundle", ItemID.SAILING_CHARTING_DRINK_CRATE_SMUGGLED_RUM, 0, 15, 40, 100, 50, 30, 15, 4, 1);

	// Item groups from the design's pools.
	private static final List<GearType> ADVENTURER_GEAR = List.of(GearType.SWORD, GearType.BOW, GearType.STAFF,
		GearType.PLATE, GearType.LEATHER, GearType.ROBE);
	private static final List<GearType> GENERIC_GEAR = List.of(GearType.DAGGER, GearType.CLOTHES);
	private static final List<GearType> ADVENTURER_ARMOUR = List.of(GearType.PLATE, GearType.LEATHER, GearType.ROBE);
	private static final List<GearType> TRINKETS = List.of(GearType.TRINKET);
	private static final List<GearType> NAUTICAL_WEAPONS = List.of(GearType.ANCHOR, GearType.CUTLASS,
		GearType.HARPOON, GearType.TRIDENT);

	/** Draw tickets for a normal item; a special one (e.g. a nautical weapon in a Boss Bundle) gets fewer. */
	private static final int NORMAL = 4;
	private static final int SPECIAL = 1;

	// Each bundle's pool. Epic tiers are listed as designed, but no Epic items exist yet.
	static
	{
		GENERIC.add(GENERIC_GEAR, NORMAL, Rarity.COMMON, Rarity.UNCOMMON, Rarity.RARE);
		GENERIC.add(ADVENTURER_GEAR, NORMAL, Rarity.COMMON, Rarity.UNCOMMON, Rarity.RARE);

		SLAYER.add(GENERIC_GEAR, NORMAL, Rarity.COMMON, Rarity.UNCOMMON, Rarity.RARE, Rarity.EPIC);
		SLAYER.add(ADVENTURER_GEAR, NORMAL, Rarity.COMMON, Rarity.UNCOMMON, Rarity.RARE, Rarity.EPIC);
		SLAYER.add(TRINKETS, NORMAL, Rarity.COMMON, Rarity.UNCOMMON);

		CLUE.add(GENERIC_GEAR, NORMAL, Rarity.COMMON, Rarity.UNCOMMON, Rarity.RARE, Rarity.EPIC);
		CLUE.add(ADVENTURER_GEAR, NORMAL, Rarity.COMMON, Rarity.UNCOMMON, Rarity.RARE, Rarity.EPIC);
		CLUE.add(TRINKETS, NORMAL, Rarity.COMMON, Rarity.UNCOMMON, Rarity.RARE);

		// Raid is the same as Boss for now, except its nautical weapons come at Epic too.
		for (BundleType bundle : new BundleType[]{BOSS, RAID})
		{
			bundle.add(GENERIC_GEAR, NORMAL, Rarity.UNCOMMON, Rarity.RARE);
			bundle.add(ADVENTURER_GEAR, NORMAL, Rarity.COMMON, Rarity.UNCOMMON, Rarity.RARE, Rarity.EPIC);
			bundle.add(TRINKETS, NORMAL, Rarity.COMMON, Rarity.RARE, Rarity.EPIC);
		}
		BOSS.add(NAUTICAL_WEAPONS, SPECIAL, Rarity.RARE);
		RAID.add(NAUTICAL_WEAPONS, SPECIAL, Rarity.RARE, Rarity.EPIC);

		TRADE.add(ADVENTURER_GEAR, NORMAL, Rarity.COMMON, Rarity.UNCOMMON);

		// Nautical weapons are the main event here, so they get normal weight.
		SAILING.add(GENERIC_GEAR, NORMAL, Rarity.COMMON, Rarity.UNCOMMON);
		SAILING.add(NAUTICAL_WEAPONS, NORMAL, Rarity.UNCOMMON, Rarity.RARE);
		SAILING.add(ADVENTURER_ARMOUR, NORMAL, Rarity.COMMON, Rarity.UNCOMMON, Rarity.RARE);
	}

	private final String displayName;
	private final int iconItemId;
	private final int dropChance;
	private final int minCoins;
	private final int maxCoins;
	private final int itemChance;
	private final int[] rarityOdds;
	/** Every item this bundle can give, with its draw tickets. */
	private final List<PoolEntry> pool = new ArrayList<>();

	BundleType(String displayName, int iconItemId, int dropChance, int minCoins, int maxCoins, int itemChance,
		int... rarityOdds)
	{
		this.displayName = displayName;
		this.iconItemId = iconItemId;
		this.dropChance = dropChance;
		this.minCoins = minCoins;
		this.maxCoins = maxCoins;
		this.itemChance = itemChance;
		this.rarityOdds = rarityOdds;
	}

	/** Add these types at these tiers to the pool, skipping tiers a type doesn't have. */
	private void add(List<GearType> types, int weight, Rarity... rarities)
	{
		for (GearType type : types)
		{
			for (Rarity rarity : rarities)
			{
				if (type.hasTier(rarity))
				{
					pool.add(new PoolEntry(new GearItem(type, rarity), weight));
				}
			}
		}
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

	/** Whether this bundle holds an item this time (the Basic Bundle only does half the time). */
	public boolean rollHasItem(Random random)
	{
		return random.nextInt(100) < itemChance;
	}

	/** Every item this bundle can give. */
	public List<GearItem> getPoolItems()
	{
		return pool.stream().map(entry -> entry.item).collect(Collectors.toList());
	}

	/**
	 * A random item from this bundle's pool: the tier from its odds, dropping to the nearest lower
	 * tier the pool has (the empty-tier rule), then one item of that tier by weight.
	 */
	public GearItem rollItem(Random random)
	{
		for (int i = rollRarity(random).ordinal(); i >= 0; i--)
		{
			Rarity rarity = Rarity.values()[i];
			List<PoolEntry> tier = pool.stream().filter(entry -> entry.item.getRarity() == rarity)
				.collect(Collectors.toList());
			if (tier.isEmpty())
			{
				continue;
			}
			int ticket = random.nextInt(tier.stream().mapToInt(entry -> entry.weight).sum());
			for (PoolEntry entry : tier)
			{
				ticket -= entry.weight;
				if (ticket < 0)
				{
					return entry.item;
				}
			}
		}
		throw new IllegalStateException(this + " has no Common items");
	}

	/** One item in a pool and its draw tickets. */
	private static final class PoolEntry
	{
		final GearItem item;
		final int weight;

		PoolEntry(GearItem item, int weight)
		{
			this.item = item;
			this.weight = weight;
		}
	}
}