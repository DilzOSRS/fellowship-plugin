package com.dilzosrs.fellowship;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * One piece of gear, e.g. a "Rare Bow". Its type decides the slot, stat and class;
 * its rarity decides how much of that stat it gives.
 */
public final class GearItem
{
	private final GearType type;
	private final Rarity rarity;

	public GearItem(GearType type, Rarity rarity)
	{
		this.type = type;
		this.rarity = rarity;
	}

	public GearType getType()
	{
		return type;
	}

	public Rarity getRarity()
	{
		return rarity;
	}

	/** e.g. "Rare Bow". */
	public String getName()
	{
		return rarity.getDisplayName() + " " + type.getDisplayName();
	}

	/** The OSRS item whose icon stands for this item, until there's custom art. */
	public int getIconItemId()
	{
		return type.getIconItemId(rarity);
	}

	public int getPower()
	{
		return rarity.getPower();
	}

	/** e.g. "Wizard/Navigator, ATK 6" or "Generic, MAG 3". */
	public String getDescription()
	{
		List<CharacterClass> classes = type.getClasses();
		String who = classes.isEmpty() ? "Generic"
			: classes.stream().map(CharacterClass::getDisplayName).collect(Collectors.joining("/"));
		return who + ", " + getStatText();
	}

	/** e.g. "ATK 6". Short enough for chat, where the list of classes would be too long. */
	public String getStatText()
	{
		return type.getSlot().getStatName() + " " + getPower();
	}

	@Override
	public boolean equals(Object o)
	{
		if (!(o instanceof GearItem))
		{
			return false;
		}
		GearItem other = (GearItem) o;
		return type == other.type && rarity == other.rarity;
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(type, rarity);
	}
}
