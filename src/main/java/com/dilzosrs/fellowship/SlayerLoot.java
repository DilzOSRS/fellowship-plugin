package com.dilzosrs.fellowship;

import java.util.Random;

/**
 * Slayer Bundles from Slayer task kills, worked out from the game's count of kills left on the task
 * (it goes down by one per kill). Each kill has a small chance, and the final kill always gives one.
 * Checking a Slayer helmet or gem doesn't change the count, so it can't drop anything.
 */
public final class SlayerLoot
{
	/** 1 in this many per task kill (1%). */
	static final int KILL_CHANCE_ONE_IN = 100;
	/**
	 * The most the count can drop in one change and still be kills. Bigger drops are a task that was
	 * cancelled or skipped at a Slayer master, which give nothing.
	 */
	static final int MAX_KILLS_PER_CHANGE = 3;

	private SlayerLoot()
	{
	}

	/** How many task kills a change in the kills-left count means, or 0 if it wasn't kills. */
	static int killsBetween(int before, int after)
	{
		int kills = before - after;
		return kills >= 1 && kills <= MAX_KILLS_PER_CHANGE ? kills : 0;
	}

	/**
	 * How many Slayer Bundles a change in the kills-left count drops: one for finishing the task,
	 * plus a roll for each kill.
	 */
	public static int bundlesFor(int before, int after, Random random)
	{
		int kills = killsBetween(before, after);
		int bundles = kills > 0 && after == 0 ? 1 : 0;
		for (int i = 0; i < kills; i++)
		{
			if (random.nextInt(KILL_CHANCE_ONE_IN) == 0)
			{
				bundles++;
			}
		}
		return bundles;
	}
}
