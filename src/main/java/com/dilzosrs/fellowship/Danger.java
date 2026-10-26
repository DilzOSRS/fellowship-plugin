package com.dilzosrs.fellowship;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * The rules for dangerous expeditions: how likely a party is to fail (ATK against the threat
 * level) and who gets injured (DEF against the threat level).
 */
public final class Danger
{
	private Danger()
	{
	}

	/**
	 * Chance of failing, from 0.0 to 1.0. A party with ATK at or above the threat never fails;
	 * below it, the chance grows with the gap: (threat - ATK) / threat.
	 */
	public static double failChance(int threat, double partyAtk)
	{
		if (threat <= 0 || partyAtk >= threat)
		{
			return 0;
		}
		return (threat - Math.max(0, partyAtk)) / threat;
	}

	/** Roll whether the party clears the expedition. */
	public static boolean rollSuccess(int threat, double partyAtk, Random random)
	{
		return random.nextDouble() >= failChance(threat, partyAtk);
	}

	/** Chance of a single injury after a failure when the party's DEF is high enough. */
	static final double FAIL_INJURY_CHANCE_WITH_DEF = 0.1;

	/** How long an injured character rests, compared with the expedition's length. */
	static final int REST_MULTIPLIER = 5;

	/**
	 * Roll who gets injured on an expedition. With p = (threat - party DEF) / threat:
	 * <ul>
	 * <li>Success, DEF at or above the threat: nobody.</li>
	 * <li>Success, DEF below: one roll at chance p for one injury.</li>
	 * <li>Failure, DEF at or above: a 10% chance of one injury.</li>
	 * <li>Failure, DEF below: each member is injured with chance 50% + p / 2.</li>
	 * </ul>
	 * When only one member is hurt, members with lower DEF are more likely to be picked.
	 *
	 * @param memberDefs each party member's effective DEF; the party DEF is their average
	 * @return positions in memberDefs of the injured members
	 */
	public static List<Integer> rollInjuries(int threat, boolean success, double[] memberDefs, Random random)
	{
		List<Integer> injured = new ArrayList<>();
		if (threat <= 0 || memberDefs.length == 0)
		{
			return injured;
		}
		double partyDef = Arrays.stream(memberDefs).average().orElse(0);
		double p = partyDef >= threat ? 0 : (threat - Math.max(0, partyDef)) / threat;

		if (success)
		{
			if (p > 0 && random.nextDouble() < p)
			{
				injured.add(pickWeakest(threat, memberDefs, random));
			}
		}
		else if (p == 0)
		{
			if (random.nextDouble() < FAIL_INJURY_CHANCE_WITH_DEF)
			{
				injured.add(pickWeakest(threat, memberDefs, random));
			}
		}
		else
		{
			double eachChance = 0.5 + p / 2;
			for (int i = 0; i < memberDefs.length; i++)
			{
				if (random.nextDouble() < eachChance)
				{
					injured.add(i);
				}
			}
		}
		return injured;
	}

	/** Pick one member, with lower-DEF members more likely: weight = (threat - DEF) + 1, at least 1. */
	static int pickWeakest(int threat, double[] memberDefs, Random random)
	{
		double[] weights = new double[memberDefs.length];
		double total = 0;
		for (int i = 0; i < memberDefs.length; i++)
		{
			weights[i] = Math.max(0, threat - memberDefs[i]) + 1;
			total += weights[i];
		}
		double roll = random.nextDouble() * total;
		for (int i = 0; i < weights.length; i++)
		{
			roll -= weights[i];
			if (roll < 0)
			{
				return i;
			}
		}
		return weights.length - 1;
	}
}
