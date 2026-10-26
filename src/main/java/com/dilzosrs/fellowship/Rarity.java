package com.dilzosrs.fellowship;

/**
 * How rare a piece of gear is. Rarer gear has more power and salvages for more coins.
 * <p>
 * The enum names are saved per account, so never rename them without a migration.
 */
public enum Rarity
{
	COMMON("Common", 1, 5),
	UNCOMMON("Uncommon", 3, 15),
	RARE("Rare", 6, 40),
	EPIC("Epic", 10, 100),
	LEGENDARY("Legendary", 15, 200);

	private final String displayName;
	private final int power;
	private final int salvageValue;

	Rarity(String displayName, int power, int salvageValue)
	{
		this.displayName = displayName;
		this.power = power;
		this.salvageValue = salvageValue;
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
