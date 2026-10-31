package com.dilzosrs.fellowship;

/**
 * The account's Fellowship Guild. It earns Guild XP whenever a character levels up or takes a
 * class advancement, and whenever an expedition is collected, so the whole team's progress adds
 * up in one place. Once the XP for the next level is full it stops filling, and the player pays
 * coins to level up (100 for level 2, doubling each level).
 */
public class Guild
{
	/** XP needed to go from Guild level 1 to 2. */
	static final int FIRST_LEVEL_COST = 20;
	/** Each Guild level after that costs this much more than the one before (20, 30, 40, ...). */
	static final int COST_INCREASE = 10;
	/** Coins to reach Guild level 2. Each level after costs twice as much as the one before. */
	static final long FIRST_LEVEL_COINS = 100;

	private long xp;
	private int level;

	/** A Guild with this much total XP and this level. XP past the next level is dropped. */
	public Guild(long xp, int level)
	{
		this.level = Math.max(1, level);
		this.xp = Math.min(Math.max(0, xp), xpForLevel(this.level + 1));
	}

	public long getXp()
	{
		return xp;
	}

	/** Adds XP, but never past what the next level needs: the rest waits until it's paid for. */
	public void addXp(long amount)
	{
		xp = Math.min(xp + amount, xpForLevel(level + 1));
	}

	public int getLevel()
	{
		return level;
	}

	/** Whether the XP for the next level is full, so it only needs paying for. */
	public boolean isReadyToLevelUp()
	{
		return xp >= xpForLevel(level + 1);
	}

	/** Coins to go up from the current level: 100, 200, 400, ... */
	public long getLevelUpCost()
	{
		return levelUpCost(level);
	}

	/** Goes up a level if the XP is full. The caller takes the coins. */
	public boolean levelUp()
	{
		if (!isReadyToLevelUp())
		{
			return false;
		}
		level++;
		return true;
	}

	/**
	 * @return the coins to go from this level to the next (level 1 to 2 costs 100)
	 */
	public static long levelUpCost(int level)
	{
		return FIRST_LEVEL_COINS << (Math.max(1, level) - 1);
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
	 * @return the Guild level reached with this much Guild XP, for saves from before levels
	 * cost coins
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
