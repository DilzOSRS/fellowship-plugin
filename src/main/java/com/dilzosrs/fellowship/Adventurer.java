package com.dilzosrs.fellowship;

/**
 * A Fellowship character. Its level comes from its total Fellowship XP.
 */
public class Adventurer
{
	/** An Adventurer can't go past this level until it evolves. XP earned at the cap is wasted. */
	public static final int LEVEL_CAP = 10;

	private final String name;
	private long xp;

	public Adventurer(String name, long xp)
	{
		this.name = name;
		this.xp = Math.min(xp, maxXp());
	}

	public String getName()
	{
		return name;
	}

	public long getXp()
	{
		return xp;
	}

	public int getLevelCap()
	{
		return LEVEL_CAP;
	}

	public void addXp(long amount)
	{
		xp = Math.min(xp + amount, maxXp());
	}

	public int getLevel()
	{
		return LevelCurve.levelForXp(xp);
	}

	public boolean isAtLevelCap()
	{
		return getLevel() >= getLevelCap();
	}

	private long maxXp()
	{
		return LevelCurve.xpForLevel(getLevelCap());
	}
}
