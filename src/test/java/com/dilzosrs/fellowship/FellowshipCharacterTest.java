package com.dilzosrs.fellowship;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.time.Duration;
import java.time.Instant;
import java.util.Random;
import net.runelite.api.Skill;
import org.junit.Test;

public class FellowshipCharacterTest
{
	private static final Instant START = Instant.parse("2026-10-25T20:00:00Z");

	private static final long ADVANCE_XP = LevelCurve.xpForLevel(CharacterClass.ADVANCEMENT_LEVEL);

	@Test
	public void gainsXp()
	{
		FellowshipCharacter character = new FellowshipCharacter(CharacterClass.ADVENTURER, 0);
		character.addXp(2_000);
		assertEquals(2_000, character.getXp());
		assertEquals(2, character.getLevel());
	}

	@Test
	public void noLevelCapBeforeTheClassAdvancement()
	{
		// Since 2026-11-02 an Adventurer keeps levelling past 10 and chooses when to advance.
		FellowshipCharacter character = new FellowshipCharacter(CharacterClass.ADVENTURER, ADVANCE_XP - 100);
		character.addXp(5_000);
		assertEquals(ADVANCE_XP + 4_900, character.getXp());
		character.addXp(500_000);
		assertTrue(character.getLevel() > 10);
		assertTrue(character.canEvolve());
	}

	@Test
	public void xpStopsAtLevel99()
	{
		long max = LevelCurve.xpForLevel(LevelCurve.MAX_LEVEL);
		FellowshipCharacter character = new FellowshipCharacter(CharacterClass.ADVENTURER, max + 1_000);
		assertEquals(max, character.getXp());
		character.addXp(1_000);
		assertEquals(max, character.getXp());
	}
	@Test
	public void cantEvolveBelowLevelTen()
	{
		FellowshipCharacter character = new FellowshipCharacter(CharacterClass.ADVENTURER, ADVANCE_XP - 1);
		assertFalse(character.canEvolve());
		assertFalse(character.evolve(CharacterClass.KNIGHT));
		assertEquals(CharacterClass.ADVENTURER, character.getCharacterClass());
	}

	@Test
	public void evolvingResetsToLevelOne()
	{
		FellowshipCharacter character = new FellowshipCharacter(CharacterClass.ADVENTURER, ADVANCE_XP);
		assertTrue(character.canEvolve());
		assertTrue(character.evolve(CharacterClass.WIZARD));
		assertEquals(CharacterClass.WIZARD, character.getCharacterClass());
		assertEquals(0, character.getXp());
		assertEquals(1, character.getLevel());
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
	public void startsAtHome()
	{
		FellowshipCharacter character = new FellowshipCharacter(CharacterClass.ADVENTURER, 0);
		assertFalse(character.isAway());
		assertNull(character.getExpeditionEnd());
		assertNull(character.getTimeLeft(START));
	}

	@Test
	public void swampCavesLastFifteenMinutes()
	{
		FellowshipCharacter character = new FellowshipCharacter(CharacterClass.ADVENTURER, 0);
		assertTrue(character.startExpedition(Expedition.LUMBRIDGE_SWAMP_CAVES, START));
		assertTrue(character.isAway());
		assertEquals(START.plus(Duration.ofMinutes(15)), character.getExpeditionEnd());
		assertEquals(Duration.ofMinutes(15), character.getTimeLeft(START));
		assertEquals(Duration.ofMinutes(5), character.getTimeLeft(START.plus(Duration.ofMinutes(10))));
	}

	@Test
	public void expeditionIsDoneWhenTimeIsUp()
	{
		FellowshipCharacter character = new FellowshipCharacter(CharacterClass.ADVENTURER, 0);
		character.startExpedition(Expedition.LUMBRIDGE_SWAMP_CAVES, START);
		assertFalse(character.isExpeditionDone(START.plus(Duration.ofMinutes(14))));
		assertTrue(character.isExpeditionDone(START.plus(Duration.ofMinutes(15))));
		// Hours later (e.g. after being logged out), it's still done and never "negative".
		assertTrue(character.isExpeditionDone(START.plus(Duration.ofHours(5))));
		assertEquals(Duration.ZERO, character.getTimeLeft(START.plus(Duration.ofHours(5))));
		// It stays away until collected.
		assertTrue(character.isAway());
	}

	@Test
	public void noOsrsXpWhileAway()
	{
		FellowshipCharacter character = new FellowshipCharacter(CharacterClass.WIZARD, 0);
		character.startExpedition(Expedition.LUMBRIDGE_SWAMP_CAVES, START);
		character.gainXp(Skill.MAGIC, 500);
		character.gainXp(Skill.WOODCUTTING, 500);
		assertEquals(0, character.getXp());
	}

	@Test
	public void cantStartTwoExpeditions()
	{
		FellowshipCharacter character = new FellowshipCharacter(CharacterClass.ADVENTURER, 0);
		character.startExpedition(Expedition.LUMBRIDGE_SWAMP_CAVES, START);
		assertFalse(character.startExpedition(Expedition.LUMBRIDGE_SWAMP_CAVES, START.plus(Duration.ofMinutes(5))));
		assertEquals(START, character.getExpeditionStart());
	}

	@Test
	public void cantEvolveWhileAway()
	{
		FellowshipCharacter character = new FellowshipCharacter(CharacterClass.ADVENTURER, ADVANCE_XP);
		character.startExpedition(Expedition.LUMBRIDGE_SWAMP_CAVES, START);
		assertFalse(character.canEvolve());
		assertFalse(character.evolve(CharacterClass.KNIGHT));
	}

	@Test
	public void cantCollectBeforeTheTimeIsUp()
	{
		FellowshipCharacter character = new FellowshipCharacter(CharacterClass.ADVENTURER, 0);
		assertNull(character.collectExpedition(START));
		character.startExpedition(Expedition.LUMBRIDGE_SWAMP_CAVES, START);
		assertNull(character.collectExpedition(START.plus(Duration.ofMinutes(14))));
		assertTrue(character.isAway());
		assertEquals(0, character.getXp());
	}

	@Test
	public void collectingGivesXpAndBringsTheCharacterHome()
	{
		FellowshipCharacter character = new FellowshipCharacter(CharacterClass.ADVENTURER, 0);
		character.startExpedition(Expedition.LUMBRIDGE_SWAMP_CAVES, START);
		assertEquals(Expedition.LUMBRIDGE_SWAMP_CAVES, character.collectExpedition(START.plus(Duration.ofMinutes(15))));
		assertEquals(1_000, character.getXp());
		assertFalse(character.isAway());
		// Home again, so OSRS XP counts again.
		character.gainXp(Skill.WOODCUTTING, 50);
		assertEquals(1_050, character.getXp());
		// Can't collect the same expedition twice.
		assertNull(character.collectExpedition(START.plus(Duration.ofMinutes(31))));
	}

	@Test
	public void expeditionXpGetsNoClassBonus()
	{
		FellowshipCharacter character = new FellowshipCharacter(CharacterClass.WIZARD, 0);
		character.startExpedition(Expedition.LUMBRIDGE_SWAMP_CAVES, START);
		character.collectExpedition(START.plus(Duration.ofMinutes(15)));
		assertEquals(1_000, character.getXp());
	}


	@Test
	public void coinRewardIsBetweenTenAndFifty()
	{
		Random random = new Random(42);
		boolean sawTen = false;
		boolean sawFifty = false;
		for (int i = 0; i < 5_000; i++)
		{
			int coins = Expedition.LUMBRIDGE_SWAMP_CAVES.rollCoins(random);
			assertTrue(coins >= 10 && coins <= 50);
			sawTen |= coins == 10;
			sawFifty |= coins == 50;
		}
		// Both ends of the range can actually happen.
		assertTrue(sawTen);
		assertTrue(sawFifty);
	}

	@Test
	public void cantEvolveIntoAdventurer()
	{
		FellowshipCharacter character = new FellowshipCharacter(CharacterClass.ADVENTURER, ADVANCE_XP);
		assertFalse(character.evolve(CharacterClass.ADVENTURER));
	}
}
