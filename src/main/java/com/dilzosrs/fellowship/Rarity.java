package com.dilzosrs.fellowship;

import java.awt.Color;

/**
 * How rare a piece of gear is. Rarer gear has more power and salvages for more coins.
 * <p>
 * The enum names are saved per account, so never rename them without a migration.
 */
public enum Rarity
{
	COMMON("Common", 1, 5, new Color(200, 200, 200)),
	UNCOMMON("Uncommon", 3, 15, new Color(30, 255, 0)),
	RARE("Rare", 6, 40, new Color(40, 130, 255)),
	EPIC("Epic", 10, 100, new Color(180, 80, 255)),
	LEGENDARY("Legendary", 15, 200, new Color(255, 128, 0));

	private final String displayName;
	private final int power;
	private final int salvageValue;
	private final Color color;

	Rarity(String displayName, int power, int salvageValue, Color color)
	{
		this.displayName = displayName;
		this.power = power;
		this.salvageValue = salvageValue;
		this.color = color;
	}

	/** The colour an item of this rarity is shown in: grey, green, blue, purple, orange. */
	public Color getColor()
	{
		return color;
	}

	public String getDisplayName()
	{
		return displayName;
	}

	/** How much of its stat an item of this rarity gives. */
	public int getPower()
	{
		return power;
	}

	/** Coins for salvaging an item of this rarity. */
	public int getSalvageValue()
	{
		return salvageValue;
	}
}
