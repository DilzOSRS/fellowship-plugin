package com.dilzosrs.fellowship;

/**
 * A Fellowship character. Its level comes from its total Fellowship XP.
 */
public class Adventurer
{
	private final String name;
	private long xp;

	public Adventurer(String name, long xp)
	{
		this.name = name;
		this.xp = xp;
	}

	public String getName()
	{
		return name;
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
		return LevelCurve.levelForXp(xp);
	}
}
