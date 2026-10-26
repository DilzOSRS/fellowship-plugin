package com.dilzosrs.fellowship;

import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 * How the account's bundles and storage are stored in its save, as JSON.
 * <p>
 * The field names end up in players' saves, so never rename them without a migration.
 * Bundles, gear types and rarities are stored by their enum names.
 */
@Slf4j
public class SavedVault
{
	List<String> bundles = new ArrayList<>();
	List<SavedGear> items = new ArrayList<>();
	int capacity = Vault.STARTING_CAPACITY;
	long genericXpProgress;

	/** One piece of gear in a save, used for storage and for what characters wear. */
	static class SavedGear
	{
		String type;
		String rarity;

		static SavedGear from(GearItem item)
		{
			if (item == null)
			{
				return null;
			}
			SavedGear gear = new SavedGear();
			gear.type = item.getType().name();
			gear.rarity = item.getRarity().name();
			return gear;
		}

		/** The item, or null if the save names gear that doesn't exist (any more). */
		GearItem toItem()
		{
			try
			{
				return new GearItem(GearType.valueOf(type), Rarity.valueOf(rarity));
			}
			catch (IllegalArgumentException | NullPointerException e)
			{
				log.warn("Unknown saved gear {} {}, skipping it", rarity, type);
				return null;
			}
		}
	}

	public static SavedVault from(Vault vault)
	{
		SavedVault saved = new SavedVault();
		for (BundleType bundle : vault.getBundles())
		{
			saved.bundles.add(bundle.name());
		}
		for (GearItem item : vault.getItems())
		{
			saved.items.add(SavedGear.from(item));
		}
		saved.capacity = vault.getCapacity();
		saved.genericXpProgress = vault.getGenericXpProgress();
		return saved;
	}

	public Vault toVault()
	{
		List<BundleType> loadedBundles = new ArrayList<>();
		if (bundles != null)
		{
			for (String bundle : bundles)
			{
				try
				{
					loadedBundles.add(BundleType.valueOf(bundle));
				}
				catch (IllegalArgumentException | NullPointerException e)
				{
					log.warn("Unknown saved bundle {}, skipping it", bundle);
				}
			}
		}
		List<GearItem> loadedItems = new ArrayList<>();
		if (items != null)
		{
			for (SavedGear gear : items)
			{
				GearItem item = gear == null ? null : gear.toItem();
				if (item != null)
				{
					loadedItems.add(item);
				}
			}
		}
		return new Vault(loadedBundles, loadedItems, capacity, genericXpProgress);
	}
}
