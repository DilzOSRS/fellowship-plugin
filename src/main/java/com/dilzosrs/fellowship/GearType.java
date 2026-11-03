package com.dilzosrs.fellowship;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import net.runelite.api.gameval.ItemID;

/**
 * The kinds of gear. Each kind goes in one slot and has a named item per tier, with its own stats
 * (docs/design/item-list-v1.md). Its tags decide who can use it (see ClassGear); a kind with no
 * tags is Generic, so anyone can use it, including Adventurers.
 * <p>
 * Not every kind has every tier, and no kind has Epic or Legendary yet.
 * The enum names are saved per account, so never rename them without a migration.
 */
public enum GearType
{
	// The icons are OSRS items drawn by RuneLite, placeholders until there's custom art.
	SWORD("Sword", Slot.WEAPON, Style.MELEE, false, EnumSet.of(Tag.SWORD),
		GearTier.of(Rarity.COMMON, "Bronze sword", ItemID.BRONZE_LONGSWORD, 2, 0, 0),
		GearTier.of(Rarity.UNCOMMON, "Mithril sword", ItemID.MITHRIL_LONGSWORD, 3, 1, 0),
		GearTier.of(Rarity.RARE, "Rune sword", ItemID.RUNE_LONGSWORD, 5, 2, 0)),
	PLATE("Plate", Slot.ARMOUR, null, false, EnumSet.of(Tag.PLATE),
		GearTier.of(Rarity.COMMON, "Bronze platebody", ItemID.BRONZE_PLATEBODY, 0, 2, 0),
		GearTier.of(Rarity.UNCOMMON, "Mithril platebody", ItemID.MITHRIL_PLATEBODY, 0, 4, 0),
		GearTier.of(Rarity.RARE, "Rune platebody", ItemID.RUNE_PLATEBODY, 0, 7, 0)),
	BOW("Bow", Slot.WEAPON, Style.RANGED, false, EnumSet.of(Tag.BOW),
		GearTier.of(Rarity.COMMON, "Shortbow", ItemID.SHORTBOW, 2, 0, 0),
		GearTier.of(Rarity.UNCOMMON, "Maple shortbow", ItemID.MAPLE_SHORTBOW, 4, 0, 0),
		GearTier.of(Rarity.RARE, "Magic shortbow", ItemID.MAGIC_SHORTBOW, 7, 0, 0)),
	// Leather gives ATK too, so it's offence plus defence.
	LEATHER("Leather", Slot.ARMOUR, null, false, EnumSet.of(Tag.LEATHER),
		GearTier.of(Rarity.COMMON, "Leather body", ItemID.LEATHER_ARMOUR, 1, 1, 0),
		GearTier.of(Rarity.UNCOMMON, "Hard leather", ItemID.HARDLEATHER_BODY, 1, 3, 0),
		GearTier.of(Rarity.RARE, "Studded leather", ItemID.STUDDED_BODY, 3, 4, 0)),
	STAFF("Staff", Slot.WEAPON, Style.MAGIC, false, EnumSet.of(Tag.STAFF),
		GearTier.of(Rarity.COMMON, "Staff", ItemID.BATTLESTAFF, 1, 0, 1),
		GearTier.of(Rarity.UNCOMMON, "Battle staff", ItemID.AIR_BATTLESTAFF, 2, 0, 2),
		GearTier.of(Rarity.RARE, "Mystic staff", ItemID.MYSTIC_MIST_BATTLESTAFF, 3, 0, 4)),
	ROBE("Robe", Slot.ARMOUR, null, false, EnumSet.of(Tag.ROBE),
		GearTier.of(Rarity.COMMON, "Apprentice robes", ItemID.WIZARDS_ROBE, 0, 1, 1),
		GearTier.of(Rarity.UNCOMMON, "Magic robes", ItemID.WIZARDS_ROBE_TRIM_GOLD, 0, 1, 3),
		GearTier.of(Rarity.RARE, "Mystic robes", ItemID.MYSTIC_ROBE_TOP, 0, 2, 5)),
	TRINKET("Trinket", Slot.TRINKET, null, false, EnumSet.noneOf(Tag.class),
		GearTier.of(Rarity.COMMON, "Brass necklace", ItemID.BRASS_NECKLACE, 0, 0, 1),
		GearTier.of(Rarity.UNCOMMON, "Silver necklace", ItemID.MURDERNECKLACE, 0, 0, 3),
		GearTier.of(Rarity.RARE, "Gold necklace", ItemID.GOLD_NECKLACE, 0, 0, 6)),
	// Generic, so anyone can use it, but under budget on purpose (Uncommon by 1, Rare by 2).
	DAGGER("Dagger", Slot.WEAPON, Style.MELEE, false, EnumSet.noneOf(Tag.class),
		GearTier.of(Rarity.COMMON, "Bronze dagger", ItemID.BRONZE_DAGGER, 1, 0, 0),
		GearTier.of(Rarity.UNCOMMON, "Mithril dagger", ItemID.MITHRIL_DAGGER, 2, 0, 0),
		GearTier.of(Rarity.RARE, "Rune dagger", ItemID.RUNE_DAGGER, 4, 0, 0)),
	// The whole Sailor tree. Under budget on purpose, like the Dagger.
	ANCHOR("Anchor", Slot.WEAPON, Style.MELEE, false, EnumSet.of(Tag.NAUTICAL),
		GearTier.of(Rarity.UNCOMMON, "Anchor", ItemID.BRAIN_ANCHOR, 3, 0, 0),
		GearTier.of(Rarity.RARE, "Heavy anchor", ItemID.BH_BRAIN_ANCHOR_IMBUE, 5, 0, 0)),
	// Combo items: nautical plus their style, so only the Sailor path with that style can use them.
	CUTLASS("Cutlass", Slot.WEAPON, Style.MELEE, true, EnumSet.of(Tag.NAUTICAL),
		GearTier.of(Rarity.UNCOMMON, "Cutlass", ItemID.OLAF2_BRINE_SABRE, 5, 0, 0),
		GearTier.of(Rarity.RARE, "Ornate cutlass", ItemID.FEVER_CUTLASS_UNLUCKY_JENKINS_LUCKY_CUTLASS, 8, 0, 0)),
	HARPOON("Harpoon", Slot.WEAPON, Style.RANGED, true, EnumSet.of(Tag.NAUTICAL),
		GearTier.of(Rarity.UNCOMMON, "Harpoon", ItemID.HARPOON, 5, 0, 0),
		GearTier.of(Rarity.RARE, "Barbed harpoon", ItemID.HUNTING_BARBED_HARPOON, 8, 0, 0)),
	TRIDENT("Trident", Slot.WEAPON, Style.MAGIC, true, EnumSet.of(Tag.NAUTICAL),
		GearTier.of(Rarity.UNCOMMON, "Trident", ItemID.TOTS_CHARGED, 5, 0, 0),
		GearTier.of(Rarity.RARE, "Magic trident", ItemID.TOTS_I_CHARGED, 8, 0, 0)),
	// Generic armour, Common and Uncommon only.
	CLOTHES("Clothes", Slot.ARMOUR, null, false, EnumSet.noneOf(Tag.class),
		GearTier.of(Rarity.COMMON, "Peasant's clothes", ItemID.MONKROBETOP, 0, 1, 0),
		GearTier.of(Rarity.UNCOMMON, "Traveller's garb", ItemID.PRIEST_GOWN, 0, 3, 0));

	/** Where a piece of gear is worn. */
	public enum Slot
	{
		WEAPON("Weapon"),
		ARMOUR("Armour"),
		TRINKET("Trinket");

		private final String displayName;

		Slot(String displayName)
		{
			this.displayName = displayName;
		}

		/** e.g. "Weapon". */
		public String getDisplayName()
		{
			return displayName;
		}
	}

	private final String displayName;
	private final Slot slot;
	/** The hidden style of a weapon, or null for armour and trinkets. */
	private final Style style;
	/** Whether the style counts as a tag too, e.g. the Cutlass is "nautical-melee". */
	private final boolean combo;
	/** The visible tags, or empty for Generic gear. */
	private final Set<Tag> tags;
	private final Map<Rarity, GearTier> tiers = new EnumMap<>(Rarity.class);

	GearType(String displayName, Slot slot, Style style, boolean combo, Set<Tag> tags, GearTier... tiers)
	{
		this.displayName = displayName;
		this.slot = slot;
		this.style = style;
		this.combo = combo;
		this.tags = Collections.unmodifiableSet(tags);
		for (GearTier tier : tiers)
		{
			this.tiers.put(tier.getRarity(), tier);
		}
	}

	public String getDisplayName()
	{
		return displayName;
	}

	/** The item of this type at that rarity, or null if this type has no such tier. */
	public GearTier getTier(Rarity rarity)
	{
		return tiers.get(rarity);
	}

	public boolean hasTier(Rarity rarity)
	{
		return tiers.containsKey(rarity);
	}

	/**
	 * The rarity itself if this type has it, else the nearest lower tier it has, or null if it has
	 * nothing at or below it. Used for saves from before the item list, and the empty-tier rule.
	 */
	public Rarity nearestTier(Rarity rarity)
	{
		for (int i = rarity.ordinal(); i >= 0; i--)
		{
			if (hasTier(Rarity.values()[i]))
			{
				return Rarity.values()[i];
			}
		}
		return null;
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

	/** Whether only followers with this item's style can use it (on top of its tags). */
	public boolean isCombo()
	{
		return combo;
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

	/** e.g. "sword", "nautical-melee", or "Generic" when it has no tags. */
	public String getTagText()
	{
		if (isGeneric())
		{
			return "Generic";
		}
		String text = tags.stream().map(Tag::getDisplayName).collect(Collectors.joining(", "));
		return combo ? text + "-" + style.getDisplayName() : text;
	}

	public boolean canBeUsedBy(CharacterClass characterClass)
	{
		return ClassGear.whyCantUse(characterClass, this) == null;
	}
}
