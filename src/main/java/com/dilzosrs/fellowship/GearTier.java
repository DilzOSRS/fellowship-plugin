package com.dilzosrs.fellowship;

/**
 * One tier of a gear type, e.g. the Uncommon sword: its name, icon and stats
 * (docs/design/item-list-v1.md). Not saved: saves store the type and rarity.
 */
public final class GearTier
{
	private final Rarity rarity;
	private final String name;
	private final int iconItemId;
	private final int atk;
	private final int def;
	private final int mag;

	private GearTier(Rarity rarity, String name, int iconItemId, int atk, int def, int mag)
	{
		this.rarity = rarity;
		this.name = name;
		this.iconItemId = iconItemId;
		this.atk = atk;
		this.def = def;
		this.mag = mag;
	}

	static GearTier of(Rarity rarity, String name, int iconItemId, int atk, int def, int mag)
	{
		return new GearTier(rarity, name, iconItemId, atk, def, mag);
	}

	public Rarity getRarity()
	{
		return rarity;
	}

	/** e.g. "Mithril sword". */
	public String getName()
	{
		return name;
	}

	/** The OSRS item whose icon stands for it, until there's custom art. */
	public int getIconItemId()
	{
		return iconItemId;
	}

	public int getAtk()
	{
		return atk;
	}

	public int getDef()
	{
		return def;
	}

	public int getMag()
	{
		return mag;
	}
}
