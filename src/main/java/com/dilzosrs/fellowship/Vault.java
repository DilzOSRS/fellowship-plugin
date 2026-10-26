package com.dilzosrs.fellowship;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * The account's unopened bundles and gear storage (shown to the player as "Storage").
 * Bundles wait here until opened, and opening one needs a free storage slot, so loot is never lost.
 */
public class Vault
{
	public static final int STARTING_CAPACITY = 10;

	/** OSRS XP needed for each Generic Bundle roll. The chance is in {@link BundleType#GENERIC}. */
	static final long GENERIC_BUNDLE_XP = 10_000;

	private final List<BundleType> bundles;
	private final List<GearItem> items;
	private final int capacity;
	/** OSRS XP gained towards the next Generic Bundle roll. */
	private long genericXpProgress;

	public Vault(List<BundleType> bundles, List<GearItem> items, int capacity, long genericXpProgress)
	{
		this.bundles = new ArrayList<>(bundles);
		this.items = new ArrayList<>(items);
		this.capacity = Math.max(STARTING_CAPACITY, capacity);
		this.genericXpProgress = Math.max(0, genericXpProgress);
	}

	public static Vault empty()
	{
		return new Vault(Collections.emptyList(), Collections.emptyList(), STARTING_CAPACITY, 0);
	}

	public List<BundleType> getBundles()
	{
		return Collections.unmodifiableList(bundles);
	}

	public List<GearItem> getItems()
	{
		return Collections.unmodifiableList(items);
	}

	public int getCapacity()
	{
		return capacity;
	}

	public long getGenericXpProgress()
	{
		return genericXpProgress;
	}

	public boolean isFull()
	{
		return items.size() >= capacity;
	}

	public void addBundle(BundleType bundle)
	{
		bundles.add(bundle);
	}

	public int countBundles(BundleType bundle)
	{
		return Collections.frequency(bundles, bundle);
	}

	/**
	 * Count OSRS XP towards Generic Bundles: every 100,000 XP gives a 10% chance of one.
	 *
	 * @return how many Generic Bundles were found (they're added to the vault)
	 */
	public int addXpForGenericBundles(long osrsXp, Random random)
	{
		genericXpProgress += osrsXp;
		int found = 0;
		while (genericXpProgress >= GENERIC_BUNDLE_XP)
		{
			genericXpProgress -= GENERIC_BUNDLE_XP;
			if (BundleType.GENERIC.rollDrop(random))
			{
				addBundle(BundleType.GENERIC);
				found++;
			}
		}
		return found;
	}

	/**
	 * Open one bundle of this type: its item goes into storage. The caller adds the coins.
	 *
	 * @return what was inside, or null if there's no such bundle or storage is full
	 */
	public Opened open(BundleType bundle, Random random)
	{
		if (isFull() || !bundles.remove(bundle))
		{
			return null;
		}
		GearItem item = bundle.rollItem(random);
		items.add(item);
		return new Opened(item, bundle.rollCoins(random));
	}

	/**
	 * Take an item out of storage (to equip it).
	 *
	 * @return the item, or null if there's no item there
	 */
	public GearItem takeItem(int index)
	{
		if (index < 0 || index >= items.size())
		{
			return null;
		}
		return items.remove(index);
	}

	/**
	 * Put an item into storage (e.g. after taking it off a character).
	 *
	 * @return false if storage is full
	 */
	public boolean storeItem(GearItem item)
	{
		if (isFull())
		{
			return false;
		}
		items.add(item);
		return true;
	}

	/**
	 * Remove an item from storage for coins.
	 *
	 * @return the coins it was worth, or 0 if there's no item there
	 */
	public int salvage(int index)
	{
		if (index < 0 || index >= items.size())
		{
			return 0;
		}
		return items.remove(index).getRarity().getSalvageValue();
	}

	/** What came out of a bundle. */
	public static final class Opened
	{
		public final GearItem item;
		public final int coins;

		Opened(GearItem item, int coins)
		{
			this.item = item;
			this.coins = coins;
		}
	}
}
