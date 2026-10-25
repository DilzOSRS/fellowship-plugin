package com.dilzosrs.fellowship;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import net.runelite.api.Skill;
import org.junit.Test;

public class FellowshipCharacterTest
{
	private static final long CAP_XP = LevelCurve.xpForLevel(CharacterClass.ADVENTURER.getLevelCap());

	@Test
	public void gainsXpBelowTheCap()
	{
		FellowshipCharacter character = new FellowshipCharacter(CharacterClass.ADVENTURER, 0);
		character.addXp(2_000);
		assertEquals(2_000, character.getXp());
		assertEquals(2, character.getLevel());
		assertFalse(character.isAtLevelCap());
	}

	@Test
	public void xpStopsAtTheCap()
	{
		FellowshipCharacter character = new FellowshipCharacter(CharacterClass.ADVENTURER, CAP_XP - 100);
		character.addXp(5_000);
		assertEquals(CAP_XP, character.getXp());
		assertEquals(10, character.getLevel());
		assertTrue(character.isAtLevelCap());
	}

	@Test
	public void xpAtTheCapIsWasted()
	{
		FellowshipCharacter character = new FellowshipCharacter(CharacterClass.ADVENTURER, CAP_XP);
		character.addXp(1_000);
		assertEquals(CAP_XP, character.getXp());
	}

	@Test
	public void savedXpOverTheCapIsTrimmed()
	{
		FellowshipCharacter character = new FellowshipCharacter(CharacterClass.ADVENTURER, 500_000);
		assertEquals(CAP_XP, character.getXp());
	}

	@Test
	public void cantEvolveBelowTheCap()
	{
		FellowshipCharacter character = new FellowshipCharacter(CharacterClass.ADVENTURER, CAP_XP - 1);
		assertFalse(character.canEvolve());
		assertFalse(character.evolve(CharacterClass.KNIGHT));
		assertEquals(CharacterClass.ADVENTURER, character.getCharacterClass());
	}

	@Test
	public void evolvingResetsToLevelOneWithAHigherCap()
	{
		FellowshipCharacter character = new FellowshipCharacter(CharacterClass.ADVENTURER, CAP_XP);
		assertTrue(character.canEvolve());
		assertTrue(character.evolve(CharacterClass.WIZARD));
		assertEquals(CharacterClass.WIZARD, character.getCharacterClass());
		assertEquals(0, character.getXp());
		assertEquals(1, character.getLevel());
		assertEquals(99, character.getLevelCap());
	}

	@Test
	public void evolvedClassesLevelPastTen()
	{
		FellowshipCharacter character = new FellowshipCharacter(CharacterClass.KNIGHT, 500_000);
		assertEquals(500_000, character.getXp());
		assertTrue(character.getLevel() > 10);
	}

	@Test
	public void evolvedClassesCantEvolveAgain()
	{
		FellowshipCharacter character = new FellowshipCharacter(CharacterClass.ARCHER, LevelCurve.xpForLevel(99));
		assertFalse(character.canEvolve());
		assertFalse(character.evolve(CharacterClass.KNIGHT));
	}

	@Test
	public void adventurerHasNoBonuses()
	{
		FellowshipCharacter character = new FellowshipCharacter(CharacterClass.ADVENTURER, 0);
		character.gainXp(Skill.ATTACK, 100);
		character.gainXp(Skill.RANGED, 100);
		character.gainXp(Skill.MAGIC, 100);
		assertEquals(300, character.getXp());
	}

	@Test
	public void knightGetsDoubleAttackAndStrength()
	{
		FellowshipCharacter character = new FellowshipCharacter(CharacterClass.KNIGHT, 0);
		character.gainXp(Skill.ATTACK, 100);
		character.gainXp(Skill.STRENGTH, 100);
		assertEquals(400, character.getXp());
	}

	@Test
	public void archerGetsDoubleRanged()
	{
		FellowshipCharacter character = new FellowshipCharacter(CharacterClass.ARCHER, 0);
		character.gainXp(Skill.RANGED, 100);
		character.gainXp(Skill.MAGIC, 100);
		assertEquals(300, character.getXp());
	}

	@Test
	public void wizardGetsDoubleMagic()
	{
		FellowshipCharacter character = new FellowshipCharacter(CharacterClass.WIZARD, 0);
		character.gainXp(Skill.MAGIC, 100);
		character.gainXp(Skill.RANGED, 100);
		assertEquals(300, character.getXp());
	}

	@Test
	public void defenceAndHitpointsAreNeverDoubled()
	{
		for (CharacterClass characterClass : CharacterClass.values())
		{
			assertEquals(1, characterClass.getXpMultiplier(Skill.DEFENCE));
			assertEquals(1, characterClass.getXpMultiplier(Skill.HITPOINTS));
		}
	}

	@Test
	public void cantEvolveIntoAdventurer()
	{
		FellowshipCharacter character = new FellowshipCharacter(CharacterClass.ADVENTURER, CAP_XP);
		assertFalse(character.evolve(CharacterClass.ADVENTURER));
	}
}
