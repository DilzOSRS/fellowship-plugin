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
	public void interactionRollsAreFiveTimesHarderPerOneOwned()
	{
		int firstChance = RareUnlock.chance(RareUnlock.INTERACTION_CHANCE, 0);
		int secondChance = RareUnlock.chance(RareUnlock.INTERACTION_CHANCE, 1);
		assertEquals(firstChance / 5, secondChance);
		assertTrue(RareUnlock.rollInteraction(0, always(firstChance - 1)));
		assertTrue(RareUnlock.rollInteraction(1, always(secondChance - 1)));
		assertFalse(RareUnlock.rollInteraction(1, always(secondChance)));
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
	public void eachOneOwnedMakesTheNextFiveTimesHarder()
	{
		assertEquals(10_000, RareUnlock.chance(RareUnlock.XP_STEP_CHANCE, 0));
		assertEquals(2_000, RareUnlock.chance(RareUnlock.XP_STEP_CHANCE, 1));
		assertEquals(400, RareUnlock.chance(RareUnlock.XP_STEP_CHANCE, 2));
		assertEquals(30_000, RareUnlock.chance(RareUnlock.LEVEL_UP_CHANCE, 0));
		assertEquals(6_000, RareUnlock.chance(RareUnlock.LEVEL_UP_CHANCE, 1));

		// A roll of 5,000 would win the first one (1%) but not the second (0.2%).
		RareUnlock unlock = thief();
		assertTrue(unlock.roll(Skill.THIEVING, 1_000, 0, 0, always(5_000)));
		assertFalse(unlock.roll(Skill.THIEVING, 1_000, 0, 1, always(5_000)));
	}

	private static RareUnlock skiller()
	{
		return RareUnlock.fromSkilling(CharacterClass.SKILLER, 100_000, 10_000, 4_000);
	}

	@Test
	public void skillersOnlyCountSkillingSkills()
	{
		RareUnlock unlock = skiller();
		assertFalse(unlock.roll(Skill.ATTACK, 50_000, 5, 0, always(0)));
		assertFalse(unlock.roll(Skill.PRAYER, 50_000, 5, 0, always(0)));
		assertFalse(unlock.roll(Skill.SLAYER, 50_000, 5, 0, always(0)));
		assertTrue(unlock.roll(Skill.WOODCUTTING, 1_000, 0, 0, always(0)));
		assertTrue(skiller().roll(Skill.COOKING, 0, 1, 0, always(0)));
	}

	@Test
	public void skillersHaveTheirOwnChanceForEachOne()
	{
		// 10% for the 1st, 1% for the 2nd, 0.4% for the 3rd, the same for XP and level-ups.
		assertTrue(skiller().roll(Skill.MINING, 1_000, 0, 0, always(99_999)));
		assertFalse(skiller().roll(Skill.MINING, 1_000, 0, 0, always(100_000)));
		assertTrue(skiller().roll(Skill.MINING, 0, 1, 0, always(99_999)));
		assertTrue(skiller().roll(Skill.MINING, 1_000, 0, 1, always(9_999)));
		assertFalse(skiller().roll(Skill.MINING, 1_000, 0, 1, always(10_000)));
		assertTrue(skiller().roll(Skill.MINING, 1_000, 0, 2, always(3_999)));
		assertFalse(skiller().roll(Skill.MINING, 1_000, 0, 2, always(4_000)));
	}

	@Test
	public void noMoreThanThreeSkillers()
	{
		assertFalse(skiller().roll(Skill.MINING, 50_000, 5, 3, always(0)));
	}

	@Test
	public void advancedCharactersStillCountAsTheirStartingClass()
	{
		assertEquals(CharacterClass.SKILLER, CharacterClass.HARVESTER.getBaseClass());
		assertEquals(CharacterClass.THIEF, CharacterClass.WARLORD.getBaseClass());
		assertEquals(CharacterClass.ADVENTURER, CharacterClass.KNIGHT.getBaseClass());
		assertEquals(CharacterClass.SKILLER, CharacterClass.SKILLER.getBaseClass());

		Team team = new Team(java.util.List.of(
			new FellowshipCharacter(CharacterClass.KNIGHT, 0),
			new FellowshipCharacter(CharacterClass.SKILLER, 0),
			new FellowshipCharacter(CharacterClass.HARVESTER, 0)));
		assertEquals(2, team.countStartedAs(CharacterClass.SKILLER));
		assertEquals(1, team.countStartedAs(CharacterClass.ADVENTURER));
		assertEquals(0, team.countStartedAs(CharacterClass.THIEF));
	}
}
