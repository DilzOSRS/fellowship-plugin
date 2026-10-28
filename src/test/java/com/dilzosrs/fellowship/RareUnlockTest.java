package com.dilzosrs.fellowship;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.util.Random;
import net.runelite.api.Skill;
import org.junit.Test;

public class RareUnlockTest
{
	/** A Random that always returns the same number, to make rolls predictable in tests. */
	private static Random always(int value)
	{
		return new Random()
		{
			@Override
			public int nextInt(int bound)
			{
				return Math.min(value, bound - 1);
			}
		};
	}

	private static RareUnlock thief()
	{
		return RareUnlock.fromSkills(CharacterClass.THIEF, Skill.THIEVING, Skill.AGILITY);
	}

	@Test
	public void onlyItsOwnSkillsCount()
	{
		RareUnlock unlock = thief();
		assertFalse(unlock.roll(Skill.WOODCUTTING, 50_000, 5, 0, always(0)));
		assertEquals(0, unlock.getXpProgress());
		assertTrue(unlock.roll(Skill.AGILITY, 1_000, 0, 0, always(0)));

		RareUnlock acolyte = RareUnlock.fromSkills(CharacterClass.ACOLYTE, Skill.PRAYER);
		assertFalse(acolyte.roll(Skill.THIEVING, 1_000, 0, 0, always(0)));
		assertTrue(acolyte.roll(Skill.PRAYER, 1_000, 0, 0, always(0)));
	}

	@Test
	public void vyreRollsOnAnyXpButNotOnLevelUps()
	{
		RareUnlock vyre = RareUnlock.fromMorytania(CharacterClass.VYRE);
		assertTrue(vyre.isMorytaniaOnly());
		assertFalse(thief().isMorytaniaOnly());
		assertTrue(vyre.roll(Skill.WOODCUTTING, 1_000, 0, 0, always(0)));
		// A level-up with under 1,000 XP gives no roll, even one that would always hit.
		assertFalse(vyre.roll(Skill.ATTACK, 100, 5, 0, always(0)));
		// It still adds to the XP total: 100 more makes 1,000.
		assertEquals(100, vyre.getXpProgress());
	}

	@Test
	public void xpRollsEveryThousandXp()
	{
		RareUnlock unlock = thief();
		// Always rolls 0, which is inside any chance.
		assertFalse(unlock.roll(Skill.THIEVING, 999, 0, 0, always(0)));
		assertTrue(unlock.roll(Skill.THIEVING, 1, 0, 0, always(0)));
		assertEquals(0, unlock.getXpProgress());
	}

	@Test
	public void levelUpsRollToo()
	{
		RareUnlock unlock = thief();
		// Under 1,000 XP, so only the level-up rolls. 20,000 is outside 1% but inside 3%.
		assertTrue(unlock.roll(Skill.THIEVING, 100, 1, 0, always(20_000)));
		assertFalse(unlock.roll(Skill.THIEVING, 100, 0, 0, always(0)));
	}

	@Test
	public void rollsCanMiss()
	{
		RareUnlock unlock = thief();
		assertFalse(unlock.roll(Skill.THIEVING, 5_000, 3, 0, always(500_000)));
	}

	@Test
	public void eachOneOwnedMakesTheNextTenTimesHarder()
	{
		assertEquals(10_000, RareUnlock.chance(RareUnlock.XP_STEP_CHANCE, 0));
		assertEquals(1_000, RareUnlock.chance(RareUnlock.XP_STEP_CHANCE, 1));
		assertEquals(100, RareUnlock.chance(RareUnlock.XP_STEP_CHANCE, 2));
		assertEquals(30_000, RareUnlock.chance(RareUnlock.LEVEL_UP_CHANCE, 0));
		assertEquals(3_000, RareUnlock.chance(RareUnlock.LEVEL_UP_CHANCE, 1));

		// A roll of 5,000 would win the first one (1%) but not the second (0.1%).
		RareUnlock unlock = thief();
		assertTrue(unlock.roll(Skill.THIEVING, 1_000, 0, 0, always(5_000)));
		assertFalse(unlock.roll(Skill.THIEVING, 1_000, 0, 1, always(5_000)));
	}
}
