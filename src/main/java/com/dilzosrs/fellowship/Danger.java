package com.dilzosrs.fellowship;

import java.util.Random;

/**
 * The rules for dangerous expeditions: how likely a party is to fail, based on its power
 * compared with the expedition's threat level. (Injuries come in step 6.3.)
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
}
