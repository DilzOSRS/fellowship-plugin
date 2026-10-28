package com.dilzosrs.fellowship;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import net.runelite.api.Skill;
import org.junit.Test;

public class VyreTest
{
	private static FellowshipCharacter vyreAtCap()
	{
		return new FellowshipCharacter(CharacterClass.VYRE, LevelCurve.xpForLevel(CharacterClass.VYRE.getLevelCap()));
	}

	@Test
	public void aVyreChoosesOneOfThreePaths()
	{
		for (CharacterClass path : new CharacterClass[]{
			CharacterClass.BLOOD_MAGE, CharacterClass.DEATH_KNIGHT, CharacterClass.SHADOW_LORD})
		{
			FellowshipCharacter vyre = vyreAtCap();
			assertTrue(vyre.evolve(path));
			assertEquals(1, vyre.getLevel());
			assertTrue(path.getEvolutions().isEmpty());
			assertEquals(LevelCurve.MAX_LEVEL, path.getLevelCap());
		}
		assertFalse(vyreAtCap().evolve(CharacterClass.KNIGHT));
	}

	@Test
	public void everyVyreClassGetsOneTimesXpForNow()
	{
		for (CharacterClass vyreClass : new CharacterClass[]{CharacterClass.VYRE,
			CharacterClass.BLOOD_MAGE, CharacterClass.DEATH_KNIGHT, CharacterClass.SHADOW_LORD})
		{
			for (Skill skill : Skill.values())
			{
				assertEquals(1, vyreClass.getXpMultiplier(skill));
			}
		}
	}
}
