package com.dilzosrs.fellowship;

import net.runelite.api.gameval.ItemID;

/**
 * The kinds of gear. Each kind goes in one slot, gives one stat, and is either for one
 * class or Generic (anyone can use it, including Adventurers).
 * <p>
 * The enum names are saved per account, so never rename them without a migration.
 */
public enum GearType
{
	// The icons are placeholders until the real item list (OSRS tiers and named uniques) is built:
	// one OSRS item per rarity, Common to Legendary.
	SWORD("Sword", Slot.WEAPON, CharacterClass.KNIGHT, ItemID.BRONZE_SWORD, ItemID.MITHRIL_SWORD,
		ItemID.RUNE_SWORD, ItemID.DRAGON_LONGSWORD, ItemID.ABYSSAL_WHIP),
	PLATE("Plate", Slot.ARMOUR, CharacterClass.KNIGHT, ItemID.BRONZE_PLATEBODY, ItemID.MITHRIL_PLATEBODY,
		ItemID.RUNE_PLATEBODY, ItemID.DRAGON_PLATEBODY, ItemID.TORVA_CHEST),
	BOW("Bow", Slot.WEAPON, CharacterClass.ARCHER, ItemID.SHORTBOW, ItemID.MAPLE_SHORTBOW,
		ItemID.MAGIC_SHORTBOW, ItemID.DARKBOW, ItemID.TWISTED_BOW),
	LEATHER("Leather", Slot.ARMOUR, CharacterClass.ARCHER, ItemID.LEATHER_ARMOUR, ItemID.DRAGONHIDE_BODY,
		ItemID.BLACK_DRAGONHIDE_BODY, ItemID.ARMADYL_CHESTPLATE, ItemID.MASORI_BODY),
	STAFF("Staff", Slot.WEAPON, CharacterClass.WIZARD, ItemID.STAFF_OF_AIR, ItemID.BATTLESTAFF,
		ItemID.MYSTIC_FIRE_STAFF, ItemID.KODAI_WAND, ItemID.TUMEKENS_SHADOW),
	ROBE("Robe", Slot.ARMOUR, CharacterClass.WIZARD, ItemID.WIZARDS_ROBE, ItemID.MYSTIC_ROBE_TOP,
		ItemID.MAGICTRAINING_INFINITYTOP, ItemID.VIRTUS_TOP, ItemID.ANCESTRAL_ROBE_TOP),
	TRINKET("Trinket", Slot.TRINKET, null, ItemID.STRUNG_SAPPHIRE_AMULET, ItemID.STRUNG_DIAMOND_AMULET,
		ItemID.STRUNG_DRAGONSTONE_AMULET, ItemID.STRUNG_ONYX_AMULET, ItemID.ZENYTE_AMULET_ENCHANTED);

	/** Where a piece of gear is worn. Weapons give ATK, armour DEF and trinkets MAG. */
	public enum Slot
	{
		WEAPON("ATK"),
		ARMOUR("DEF"),
		TRINKET("MAG");

		private final String statName;

		Slot(String statName)
		{
			this.statName = statName;
		}

		public String getStatName()
		{
			return statName;
		}
	}

	private final String displayName;
	private final Slot slot;
	/** The only class that can use it, or null for Generic gear. */
	private final CharacterClass requiredClass;

	/** One OSRS item icon per rarity, in Rarity order. */
	private final int[] iconItemIds;

	GearType(String displayName, Slot slot, CharacterClass requiredClass, int... iconItemIds)
	{
		this.iconItemIds = iconItemIds;
		this.displayName = displayName;
		this.slot = slot;
		this.requiredClass = requiredClass;
	}

	public String getDisplayName()
	{
		return displayName;
	}

	/** The OSRS item whose icon stands for this type at this rarity, until there's custom art. */
	public int getIconItemId(Rarity rarity)
	{
		return iconItemIds[rarity.ordinal()];
	}

	public Slot getSlot()
	{
		return slot;
	}

	/** The only class that can use it, or null for Generic gear. */
	public CharacterClass getRequiredClass()
	{
		return requiredClass;
	}

	public boolean canBeUsedBy(CharacterClass characterClass)
	{
		return requiredClass == null || requiredClass == characterClass;
	}
}
