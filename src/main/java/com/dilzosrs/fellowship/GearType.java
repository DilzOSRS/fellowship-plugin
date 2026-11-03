package com.dilzosrs.fellowship;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;
import net.runelite.api.gameval.ItemID;

/**
 * The kinds of gear. Each kind goes in one slot and gives one stat. Its tags decide who can use it
 * (see ClassGear); a kind with no tags is Generic, so anyone can use it, including Adventurers.
 * <p>
 * The enum names are saved per account, so never rename them without a migration.
 */
public enum GearType
{
	// The icons are placeholders until the real item list (OSRS tiers and named uniques) is built:
	// one OSRS item per rarity, Common to Legendary.
	SWORD("Sword", Slot.WEAPON, Style.MELEE, EnumSet.of(Tag.SWORD),
		ItemID.BRONZE_SWORD, ItemID.MITHRIL_SWORD, ItemID.RUNE_SWORD, ItemID.DRAGON_LONGSWORD, ItemID.ABYSSAL_WHIP),
	PLATE("Plate", Slot.ARMOUR, null, EnumSet.of(Tag.PLATE), ItemID.BRONZE_PLATEBODY, ItemID.MITHRIL_PLATEBODY,
		ItemID.RUNE_PLATEBODY, ItemID.DRAGON_PLATEBODY, ItemID.TORVA_CHEST),
	BOW("Bow", Slot.WEAPON, Style.RANGED, EnumSet.of(Tag.BOW),
		ItemID.SHORTBOW, ItemID.MAPLE_SHORTBOW, ItemID.MAGIC_SHORTBOW, ItemID.DARKBOW, ItemID.TWISTED_BOW),
	LEATHER("Leather", Slot.ARMOUR, null, EnumSet.of(Tag.LEATHER),
		ItemID.LEATHER_ARMOUR, ItemID.DRAGONHIDE_BODY, ItemID.BLACK_DRAGONHIDE_BODY, ItemID.ARMADYL_CHESTPLATE,
		ItemID.MASORI_BODY),
	STAFF("Staff", Slot.WEAPON, Style.MAGIC, EnumSet.of(Tag.STAFF),
		ItemID.STAFF_OF_AIR, ItemID.BATTLESTAFF, ItemID.MYSTIC_FIRE_STAFF, ItemID.KODAI_WAND, ItemID.TUMEKENS_SHADOW),
	ROBE("Robe", Slot.ARMOUR, null, EnumSet.of(Tag.ROBE), ItemID.WIZARDS_ROBE, ItemID.MYSTIC_ROBE_TOP,
		ItemID.MAGICTRAINING_INFINITYTOP, ItemID.VIRTUS_TOP, ItemID.ANCESTRAL_ROBE_TOP),
	TRINKET("Trinket", Slot.TRINKET, null, EnumSet.noneOf(Tag.class), ItemID.STRUNG_SAPPHIRE_AMULET,
		ItemID.STRUNG_DIAMOND_AMULET, ItemID.STRUNG_DRAGONSTONE_AMULET, ItemID.STRUNG_ONYX_AMULET,
		ItemID.ZENYTE_AMULET_ENCHANTED);

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
	/** The hidden style of a weapon, or null for armour and trinkets. */
	private final Style style;
	/** The visible tags, or empty for Generic gear. */
	private final Set<Tag> tags;

	/** One OSRS item icon per rarity, in Rarity order. */
	private final int[] iconItemIds;

	GearType(String displayName, Slot slot, Style style, Set<Tag> tags, int... iconItemIds)
	{
		this.iconItemIds = iconItemIds;
		this.displayName = displayName;
		this.slot = slot;
		this.style = style;
		this.tags = Collections.unmodifiableSet(tags);
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

	/** The hidden style of a weapon, or null for armour and trinkets. */
	public Style getStyle()
	{
		return style;
	}

	/** The visible tags, or empty for Generic gear. */
	public Set<Tag> getTags()
	{
		return tags;
	}

	public boolean isGeneric()
	{
		return tags.isEmpty();
	}

	/** e.g. "sword", or "Generic" when it has no tags. */
	public String getTagText()
	{
		return isGeneric() ? "Generic" : tags.stream().map(Tag::getDisplayName).collect(Collectors.joining(", "));
	}

	public boolean canBeUsedBy(CharacterClass characterClass)
	{
		return ClassGear.whyCantUse(characterClass, this) == null;
	}
}