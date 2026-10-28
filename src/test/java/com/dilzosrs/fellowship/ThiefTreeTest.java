package com.dilzosrs.fellowship;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import net.runelite.api.Skill;
import org.junit.Test;

public class ThiefTreeTest
{
	/** A character of a class, at its level cap. */
	private static FellowshipCharacter atCap(CharacterClass characterClass)
	{
		return new FellowshipCharacter(characterClass, LevelCurve.xpForLevel(characterClass.getLevelCap()));
	}

	@Test
	public void thiefBecomesBanditThenWarlord()
	{
		FellowshipCharacter thief = atCap(CharacterClass.THIEF);
		assertFalse(thief.evolve(CharacterClass.WARLORD));
		assertTrue(thief.evolve(CharacterClass.BANDIT));
		assertEquals(1, thief.getLevel());
		// Not ready again until the Bandit reaches level 10 too.
		assertFalse(thief.canEvolve());

		FellowshipCharacter bandit = atCap(CharacterClass.BANDIT);
		assertFalse(bandit.evolve(CharacterClass.NIGHT_BLADE));
		assertTrue(bandit.evolve(CharacterClass.WARLORD));
	}

	@Test
	public void thiefBecomesAssassinThenNightBlade()
	{
		assertTrue(atCap(CharacterClass.THIEF).evolve(CharacterClass.ASSASSIN));
		FellowshipCharacter assassin = atCap(CharacterClass.ASSASSIN);
		assertFalse(assassin.evolve(CharacterClass.WARLORD));
		assertTrue(assassin.evolve(CharacterClass.NIGHT_BLADE));
	}

	@Test
	public void finalClassesHaveNoMoreAdvancements()
	{
		assertTrue(CharacterClass.WARLORD.getEvolutions().isEmpty());
		assertTrue(CharacterClass.NIGHT_BLADE.getEvolutions().isEmpty());
		assertEquals(LevelCurve.MAX_LEVEL, CharacterClass.WARLORD.getLevelCap());
		assertEquals(LevelCurve.MAX_LEVEL, CharacterClass.NIGHT_BLADE.getLevelCap());
	}

	@Test
	public void bonusSkillsGrowWithTheTree()
	{
		assertEquals(2, CharacterClass.BANDIT.getXpMultiplier(Skill.STRENGTH));
		assertEquals(1, CharacterClass.BANDIT.getXpMultiplier(Skill.RANGED));
		assertEquals(2, CharacterClass.ASSASSIN.getXpMultiplier(Skill.RANGED));
		assertEquals(2, CharacterClass.NIGHT_BLADE.getXpMultiplier(Skill.THIEVING));
	}
}
