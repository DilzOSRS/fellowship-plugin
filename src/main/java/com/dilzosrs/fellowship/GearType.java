package com.dilzosrs.fellowship;

import java.util.List;
import net.runelite.api.gameval.ItemID;

/**
 * The kinds of gear. Each kind goes in one slot, gives one stat, and is either for certain
 * classes or Generic (anyone can use it, including Adventurers).
 * <p>
 * The enum names are saved per account, so never rename them without a migration.
 */
public enum GearType
{
	// The icons are placeholders until the real item list (OSRS tiers and named uniques) is built:
	// one OSRS item per rarity, Common to Legendary.
	// The Sailor's paths all wear leather, and each shares one weapon with an Adventurer path.
	SWORD("Sword", Slot.WEAPON, List.of(CharacterClass.KNIGHT, CharacterClass.SWASHBUCKLER),
		ItemID.BRONZE_SWORD, ItemID.MITHRIL_SWORD, ItemID.RUNE_SWORD, ItemID.DRAGON_LONGSWORD, ItemID.ABYSSAL_WHIP),
	PLATE("Plate", Slot.ARMOUR, List.of(CharacterClass.KNIGHT), ItemID.BRONZE_PLATEBODY, ItemID.MITHRIL_PLATEBODY,
		ItemID.RUNE_PLATEBODY, ItemID.DRAGON_PLATEBODY, ItemID.TORVA_CHEST),
	BOW("Bow", Slot.WEAPON, List.of(CharacterClass.ARCHER, CharacterClass.HARPOONER),
		ItemID.SHORTBOW, ItemID.MAPLE_SHORTBOW, ItemID.MAGIC_SHORTBOW, ItemID.DARKBOW, ItemID.TWISTED_BOW),
	LEATHER("Leather", Slot.ARMOUR,
		List.of(CharacterClass.ARCHER, CharacterClass.SWASHBUCKLER, CharacterClass.HARPOONER, CharacterClass.NAVIGATOR),
		ItemID.LEATHER_ARMOUR, ItemID.DRAGONHIDE_BODY, ItemID.BLACK_DRAGONHIDE_BODY, ItemID.ARMADYL_CHESTPLATE,
		ItemID.MASORI_BODY),
	STAFF("Staff", Slot.WEAPON, List.of(CharacterClass.WIZARD, CharacterClass.NAVIGATOR),
		ItemID.STAFF_OF_AIR, ItemID.BATTLESTAFF, ItemID.MYSTIC_FIRE_STAFF, ItemID.KODAI_WAND, ItemID.TUMEKENS_SHADOW),
	ROBE("Robe", Slot.ARMOUR, List.of(CharacterClass.WIZARD), ItemID.WIZARDS_ROBE, ItemID.MYSTIC_ROBE_TOP,
		ItemID.MAGICTRAINING_INFINITYTOP, ItemID.VIRTUS_TOP, ItemID.ANCESTRAL_ROBE_TOP),
	TRINKET("Trinket", Slot.TRINKET, List.of(), ItemID.STRUNG_SAPPHIRE_AMULET, ItemID.STRUNG_DIAMOND_AMULET,
		ItemID.STRUNG_DRAGONSTONE_AMULET, ItemID.STRUNG_ONYX_AMULET, ItemID.ZENYTE_AMULET_ENCHANTED);

	/** Where a piece of gear is worn. Weapons give ATK, armour DEF and trinkets MAG. */
	public enum Slot
	{
		WEAPON("Weapon", "ATK"),
		ARMOUR("Armour", "DEF"),
		TRINKET("Trinket", "MAG");

		private final String displayName;
		private final String statName;

		Slot(String displayName, String statName)
		{
			this.displayName = displayName;
			this.statName = statName;
		}

		/** e.g. "Weapon". */
		public String getDisplayName()
		{
			return displayName;
		}

		public String getStatName()
		{
			return statName;
		}
	}

	private final String displayName;
	private final Slot slot;
	/** The classes that can use it, or empty for Generic gear. */
	private final List<CharacterClass> classes;

	/** One OSRS item icon per rarity, in Rarity order. */
	private final int[] iconItemIds;

	GearType(String displayName, Slot slot, List<CharacterClass> classes, int... iconItemIds)
	{
		this.iconItemIds = iconItemIds;
		this.displayName = displayName;
		this.slot = slot;
		this.classes = classes;
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

	/** The classes that can use it, or empty for Generic gear. */
	public List<CharacterClass> getClasses()
	{
		return classes;
	}

	public boolean canBeUsedBy(CharacterClass characterClass)
	{
		return classes.isEmpty() || classes.contains(characterClass);
	}
}
