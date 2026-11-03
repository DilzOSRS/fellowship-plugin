package com.dilzosrs.fellowship;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * What gear a class can use: its visible tags, its hidden styles, and limits like "dark melee
 * weapons only". The table is in docs/design/roadmap-items-bundles-stats.md (B1 part 1).
 * <p>
 * A class can use an item if it has every tag on the item, and the item's style passes any limit
 * on those tags. Generic gear (no tags) fits everyone.
 */
public final class ClassGear
{
	private static final ClassGear NONE = new ClassGear(EnumSet.noneOf(Tag.class), EnumSet.noneOf(Style.class));
	private static final Map<CharacterClass, ClassGear> BY_CLASS = new EnumMap<>(CharacterClass.class);

	static
	{
		BY_CLASS.put(CharacterClass.KNIGHT, new ClassGear(EnumSet.of(Tag.SWORD, Tag.PLATE), EnumSet.of(Style.MELEE)));
		BY_CLASS.put(CharacterClass.ARCHER, new ClassGear(EnumSet.of(Tag.BOW, Tag.LEATHER), EnumSet.of(Style.RANGED)));
		BY_CLASS.put(CharacterClass.WIZARD, new ClassGear(EnumSet.of(Tag.STAFF, Tag.ROBE), EnumSet.of(Style.MAGIC)));

		BY_CLASS.put(CharacterClass.THIEF,
			new ClassGear(EnumSet.of(Tag.SWORD, Tag.BOW, Tag.LEATHER), EnumSet.of(Style.MELEE, Style.RANGED)));
		ClassGear bandit = new ClassGear(EnumSet.of(Tag.SWORD, Tag.DARK, Tag.LEATHER), EnumSet.of(Style.MELEE))
			.limit(Tag.DARK, Style.MELEE);
		ClassGear assassin = new ClassGear(EnumSet.of(Tag.BOW, Tag.DARK, Tag.LEATHER), EnumSet.of(Style.RANGED))
			.limit(Tag.DARK, Style.RANGED);
		BY_CLASS.put(CharacterClass.BANDIT, bandit);
		BY_CLASS.put(CharacterClass.ASSASSIN, assassin);
		// The t3s aren't designed yet, so they keep the gear of the class before them.
		BY_CLASS.put(CharacterClass.WARLORD, bandit);
		BY_CLASS.put(CharacterClass.NIGHT_BLADE, assassin);

		BY_CLASS.put(CharacterClass.SAILOR, new ClassGear(EnumSet.of(Tag.NAUTICAL, Tag.LEATHER), EnumSet.noneOf(Style.class)));
		BY_CLASS.put(CharacterClass.SWASHBUCKLER,
			new ClassGear(EnumSet.of(Tag.SWORD, Tag.NAUTICAL, Tag.LEATHER), EnumSet.of(Style.MELEE)));
		BY_CLASS.put(CharacterClass.HARPOONER,
			new ClassGear(EnumSet.of(Tag.BOW, Tag.NAUTICAL, Tag.LEATHER), EnumSet.of(Style.RANGED)));
		BY_CLASS.put(CharacterClass.NAVIGATOR,
			new ClassGear(EnumSet.of(Tag.STAFF, Tag.NAUTICAL, Tag.LEATHER), EnumSet.of(Style.MAGIC)));

		BY_CLASS.put(CharacterClass.VYRE, new ClassGear(
			EnumSet.of(Tag.SWORD, Tag.STAFF, Tag.DARK, Tag.LEATHER, Tag.ROBE), EnumSet.of(Style.MELEE, Style.MAGIC)));
		BY_CLASS.put(CharacterClass.DEATH_KNIGHT,
			new ClassGear(EnumSet.of(Tag.SWORD, Tag.DARK, Tag.LEATHER, Tag.ROBE), EnumSet.of(Style.MELEE)));
		BY_CLASS.put(CharacterClass.SHADOW_LORD,
			new ClassGear(EnumSet.of(Tag.BOW, Tag.DARK, Tag.LEATHER, Tag.ROBE), EnumSet.of(Style.RANGED)));
		BY_CLASS.put(CharacterClass.BLOOD_MAGE,
			new ClassGear(EnumSet.of(Tag.STAFF, Tag.DARK, Tag.LEATHER, Tag.ROBE), EnumSet.of(Style.MAGIC)));
		// Everyone else (Adventurer, the Skiller tree, Acolyte tree, Merchant, Alchemist) wears Generic gear only.
	}

	private final Set<Tag> tags;
	/** Hidden for now; combo items (e.g. a nautical-melee Cutlass) will check it. */
	private final Set<Style> styles;
	/** Tags this class can only use with certain styles, e.g. the Bandit's dark melee weapons. */
	private final Map<Tag, Style> limits = new EnumMap<>(Tag.class);

	private ClassGear(Set<Tag> tags, Set<Style> styles)
	{
		this.tags = Collections.unmodifiableSet(tags);
		this.styles = Collections.unmodifiableSet(styles);
	}

	private ClassGear limit(Tag tag, Style style)
	{
		limits.put(tag, style);
		return this;
	}

	public static ClassGear of(CharacterClass characterClass)
	{
		return BY_CLASS.getOrDefault(characterClass, NONE);
	}

	public Set<Tag> getTags()
	{
		return tags;
	}

	public Set<Style> getStyles()
	{
		return styles;
	}

	/**
	 * Why a class can't use this kind of gear, e.g. "Bandits only use dark melee weapons", or null
	 * if it can.
	 */
	public static String whyCantUse(CharacterClass characterClass, GearType type)
	{
		return of(characterClass).whyCantUse(type, plural(characterClass));
	}

	private String whyCantUse(GearType type, String who)
	{
		if (type.isGeneric())
		{
			return null;
		}
		if (tags.isEmpty())
		{
			return who + " can only use Generic gear";
		}
		for (Tag tag : type.getTags())
		{
			if (!tags.contains(tag))
			{
				return who + " can't use " + tag.getDisplayName() + " gear";
			}
			Style only = limits.get(tag);
			if (only != null && type.getStyle() != only)
			{
				return who + " only use " + tag.getDisplayName() + " " + only.getDisplayName() + " weapons";
			}
		}
		return null;
	}

	/** The names of every class that can use this kind of gear, or "Anyone" for Generic gear. */
	public static String whoCanUse(GearType type)
	{
		if (type.isGeneric())
		{
			return "Anyone";
		}
		List<String> names = new ArrayList<>();
		for (CharacterClass characterClass : CharacterClass.values())
		{
			if (type.canBeUsedBy(characterClass))
			{
				names.add(characterClass.getDisplayName());
			}
		}
		return names.isEmpty() ? "Nobody yet" : String.join(", ", names);
	}

	/** e.g. "Knights", "Thieves". */
	static String plural(CharacterClass characterClass)
	{
		String name = characterClass.getDisplayName();
		return name.endsWith("f") ? name.substring(0, name.length() - 1) + "ves" : name + "s";
	}
}
