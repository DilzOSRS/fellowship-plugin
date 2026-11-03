package com.dilzosrs.fellowship;

import java.awt.Color;

/**
 * How rare a piece of gear is (t1 Common to t5 Legendary). Rarer gear needs a higher level to wear
 * and salvages for more coins; its stats come from the item itself (GearTier).
 * <p>
 * The enum names are saved per account, so never rename them without a migration.
 */
public enum Rarity
{
	// Level to equip (docs/design/roadmap-items-bundles-stats.md): Common 1, Uncommon 10, Rare 15.
	// Epic (20–25) and Legendary (30+) items don't exist yet; each will sit somewhere in its range.
	COMMON("Common", 1, 5, new Color(200, 200, 200)),
	UNCOMMON("Uncommon", 10, 15, new Color(30, 255, 0)),
	RARE("Rare", 15, 40, new Color(40, 130, 255)),
	EPIC("Epic", 20, 100, new Color(180, 80, 255)),
	LEGENDARY("Legendary", 30, 200, new Color(255, 128, 0));

	private final String displayName;
	private final int levelRequirement;
	private final int salvageValue;
	private final Color color;

	Rarity(String displayName, int levelRequirement, int salvageValue, Color color)
	{
		this.displayName = displayName;
		this.levelRequirement = levelRequirement;
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

	/** The level a follower needs to wear an item of this rarity. */
	public int getLevelRequirement()
	{
		return levelRequirement;
	}

	/** Coins for salvaging an item of this rarity. */
	public int getSalvageValue()
	{
		return salvageValue;
	}
}
