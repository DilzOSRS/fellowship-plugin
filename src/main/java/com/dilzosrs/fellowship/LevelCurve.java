package com.dilzosrs.fellowship;

/**
 * Fellowship's own level curve. Each level costs more XP than the last: steeply up to
 * {@link #EARLY_LEVELS_END}, then gently after that.
 * <p>
 * Tune the game by changing the constants below, then playtest.
 */
public final class LevelCurve
{
	/** XP needed to go from level 1 to level 2. */
	static final int FIRST_LEVEL_COST = 1_500;

	/** Up to this level, each level costs {@link #EARLY_GROWTH} times the last. */
	static final int EARLY_LEVELS_END = 10;

	/** How much more each early level costs than the one before (1.5 = 50% more). */
	static final double EARLY_GROWTH = 1.5;

	/** How much more each level after {@link #EARLY_LEVELS_END} costs (1.05 = 5% more). */
	static final double LATE_GROWTH = 1.05;

	/** The highest level any character can reach. */
	public static final int MAX_LEVEL = 99;

	/** TOTAL_XP[level] is the total XP needed to reach that level. Index 0 is unused. */
	private static final long[] TOTAL_XP = new long[MAX_LEVEL + 1];

	static
	{
		double cost = FIRST_LEVEL_COST;
		for (int level = 2; level <= MAX_LEVEL; level++)
		{
			TOTAL_XP[level] = TOTAL_XP[level - 1] + Math.round(cost);
			cost *= level < EARLY_LEVELS_END ? EARLY_GROWTH : LATE_GROWTH;
		}
	}

	private LevelCurve()
	{
	}

	/**
	 * @return the total XP needed to reach the given level (level 1 needs 0)
	 */
	public static long xpForLevel(int level)
	{
		return TOTAL_XP[Math.max(1, Math.min(level, MAX_LEVEL))];
	}

	/**
	 * @return the level reached with this much total XP
	 */
	public static int levelForXp(long xp)
	{
		int level = 1;
		while (level < MAX_LEVEL && xp >= TOTAL_XP[level + 1])
		{
			level++;
		}
		return level;
	}
}
