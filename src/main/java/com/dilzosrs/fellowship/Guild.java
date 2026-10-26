package com.dilzosrs.fellowship;

/**
 * The account's Fellowship Guild. It earns Guild XP whenever a character levels up and
 * whenever an expedition is collected, so the whole team's progress adds up in one place.
 */
public class Guild
{
	/** XP needed to go from Guild level 1 to 2. */
	static final int FIRST_LEVEL_COST = 20;
	/** Each Guild level after that costs this much more than the one before (20, 30, 40, ...). */
	static final int COST_INCREASE = 10;

	private long xp;

	public Guild(long xp)
	{
		this.xp = Math.max(0, xp);
	}

	public long getXp()
	{
		return xp;
	}

	public void addXp(long amount)
	{
		xp += amount;
	}

	public int getLevel()
	{
		return levelForXp(xp);
	}

	/**
	 * @return the total Guild XP needed to reach the given level (level 1 needs 0)
	 */
	public static long xpForLevel(int level)
	{
		long total = 0;
		long cost = FIRST_LEVEL_COST;
		for (int l = 2; l <= level; l++)
		{
			total += cost;
			cost += COST_INCREASE;
		}
		return total;
	}

	/**
	 * @return the Guild level reached with this much Guild XP
	 */
	public static int levelForXp(long xp)
	{
		int level = 1;
		while (xp >= xpForLevel(level + 1))
		{
			level++;
		}
		return level;
	}
}
