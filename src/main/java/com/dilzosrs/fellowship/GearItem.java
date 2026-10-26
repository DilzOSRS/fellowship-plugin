package com.dilzosrs.fellowship;

import java.util.Objects;

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

	public int getPower()
	{
		return rarity.getPower();
	}

	/** e.g. "Archer, ATK 6" or "Generic, MAG 3". */
	public String getDescription()
	{
		CharacterClass requiredClass = type.getRequiredClass();
		String who = requiredClass == null ? "Generic" : requiredClass.getDisplayName();
		return who + ", " + type.getSlot().getStatName() + " " + getPower();
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
