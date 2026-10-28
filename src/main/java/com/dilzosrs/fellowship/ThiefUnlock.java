package com.dilzosrs.fellowship;

import java.util.Random;
import net.runelite.api.Skill;

/**
 * The rare chance of a Thief joining the Fellowship from Thieving and Agility.
 * <p>
 * There are two rolls: one for every 1,000 XP (fair to fast and slow training methods), and one
 * for every level-up (fair to low-level players, who earn XP slowly but level often).
 * Each Thief you already have makes both chances 10 times smaller.
 */
public class ThiefUnlock
{
	/** OSRS XP in Thieving and Agility (together) per XP roll. */
	static final long XP_STEP = 1_000;
	/** Chances are out of this many, so tiny chances (0.01%) are still whole numbers. */
	static final int ONE_IN = 1_000_000;
	/** 1% per XP step for the first Thief. */
	static final int XP_STEP_CHANCE = 10_000;
	/** 3% per Thieving or Agility level-up for the first Thief. */
	static final int LEVEL_UP_CHANCE = 30_000;
	/** Each Thief already on the team divides the chances by this. */
	static final int HARDER_PER_THIEF = 10;

	// Kept in memory only: a restart loses under 1,000 XP towards the next roll.
	private long xpProgress;

	public static boolean counts(Skill skill)
	{
		return skill == Skill.THIEVING || skill == Skill.AGILITY;
	}

	/**
	 * Roll for OSRS XP gained in a skill, including any level-ups it caused.
	 *
	 * @param levelsGained how many OSRS levels the skill went up with this XP
	 * @param thievesOwned how many Thieves are already on the team
	 * @return true if a Thief joins (at most one per call)
	 */
	public boolean roll(Skill skill, long xpGained, int levelsGained, int thievesOwned, Random random)
	{
		if (!counts(skill))
		{
			return false;
		}
		boolean found = false;
		xpProgress += xpGained;
		while (xpProgress >= XP_STEP)
		{
			xpProgress -= XP_STEP;
			found |= hits(XP_STEP_CHANCE, thievesOwned, random);
		}
		for (int i = 0; i < levelsGained; i++)
		{
			found |= hits(LEVEL_UP_CHANCE, thievesOwned, random);
		}
		return found;
	}

	/** A chance (out of {@link #ONE_IN}), made 10 times smaller for each Thief already owned. */
	static int chance(int baseChance, int thievesOwned)
	{
		int chance = baseChance;
		for (int i = 0; i < thievesOwned; i++)
		{
			chance /= HARDER_PER_THIEF;
		}
		return chance;
	}

	private static boolean hits(int baseChance, int thievesOwned, Random random)
	{
		return random.nextInt(ONE_IN) < chance(baseChance, thievesOwned);
	}

	long getXpProgress()
	{
		return xpProgress;
	}
}
