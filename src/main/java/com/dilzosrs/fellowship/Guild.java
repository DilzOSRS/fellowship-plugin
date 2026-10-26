package com.dilzosrs.fellowship;

/**
 * The account's Fellowship Guild. It earns Guild XP whenever a character levels up and
 * whenever an expedition is collected, so the whole team's progress adds up in one place.
 * Each Guild level above 1 gives one hire. Hiring an Adventurer uses a hire and costs coins.
 */
public class Guild
{
	/** XP needed to go from Guild level 1 to 2. */
	static final int FIRST_LEVEL_COST = 20;
	/** Each Guild level after that costs this much more than the one before (20, 30, 40, ...). */
	static final int COST_INCREASE = 10;
	/** Coins needed for each hire, on top of having a hire available. */
	public static final int HIRE_COST = 100;

	/** Skillers don't need a hire, but there can only be this many, and each costs double the last. */
	public static final int MAX_SKILLERS = 3;
	static final int FIRST_SKILLER_COST = 500;

	private long xp;
	private int hiresUsed;
	private int skillersHired;

	public Guild(long xp, int hiresUsed)
	{
		this(xp, hiresUsed, 0);
	}

	public Guild(long xp, int hiresUsed, int skillersHired)
	{
		this.xp = Math.max(0, xp);
		this.hiresUsed = Math.max(0, hiresUsed);
		this.skillersHired = Math.max(0, skillersHired);
	}

	public int getSkillersHired()
	{
		return skillersHired;
	}

	/** What the next Skiller costs: 500, then 1,000, then 2,000 coins. */
	public int getSkillerCost()
	{
		return FIRST_SKILLER_COST << Math.min(skillersHired, MAX_SKILLERS);
	}

	public boolean canHireSkiller(long coins)
	{
		return skillersHired < MAX_SKILLERS && coins >= getSkillerCost();
	}

	/**
	 * Count a Skiller as hired. The caller takes the coins (check the cost first) and adds the character.
	 *
	 * @return false if the Skiller limit is reached
	 */
	public boolean useSkillerHire()
	{
		if (skillersHired >= MAX_SKILLERS)
		{
			return false;
		}
		skillersHired++;
		return true;
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

	public int getHiresUsed()
	{
		return hiresUsed;
	}

	/** Hires earned from Guild levels and not used yet. Unused hires are kept. */
	public int getHiresAvailable()
	{
		return Math.max(0, getLevel() - 1 - hiresUsed);
	}

	public boolean canHire(long coins)
	{
		return getHiresAvailable() > 0 && coins >= HIRE_COST;
	}

	/**
	 * Use up one hire. The caller takes the coins and adds the character.
	 *
	 * @return false if there's no hire available
	 */
	public boolean useHire()
	{
		if (getHiresAvailable() <= 0)
		{
			return false;
		}
		hiresUsed++;
		return true;
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
