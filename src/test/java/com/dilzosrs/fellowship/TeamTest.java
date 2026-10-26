package com.dilzosrs.fellowship;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import net.runelite.api.Skill;
import org.junit.Test;

public class TeamTest
{
	@Test
	public void emptySaveStartsWithOneAdventurer()
	{
		Team team = new Team(Collections.emptyList());
		assertEquals(1, team.size());
		assertEquals(CharacterClass.ADVENTURER, team.get(0).getCharacterClass());
	}

	@Test
	public void hiresAreFreshAdventurers()
	{
		Team team = new Team(List.of(new FellowshipCharacter(CharacterClass.KNIGHT, 5_000)));
		team.hire();
		assertEquals(2, team.size());
		assertEquals(CharacterClass.ADVENTURER, team.get(1).getCharacterClass());
		assertEquals(0, team.get(1).getXp());
	}

	@Test
	public void teamHasNoSizeLimit()
	{
		Team team = new Team(Collections.emptyList());
		for (int i = 0; i < 9; i++)
		{
			team.hire();
		}
		assertEquals(10, team.size());
		assertEquals("Adventurer 10", team.getDisplayName(9));
	}

	@Test
	public void hiresAreNumbered()
	{
		Team team = new Team(List.of(new FellowshipCharacter(CharacterClass.KNIGHT, 0)));
		team.hire();
		team.hire();
		assertEquals("Knight", team.getDisplayName(0));
		assertEquals("Adventurer 2", team.getDisplayName(1));
		assertEquals("Adventurer 3", team.getDisplayName(2));
	}

	@Test
	public void activeGetsFullXpAndTheRestGetTenPercent()
	{
		Team team = new Team(Collections.emptyList());
		team.hire();
		team.gainXp(Skill.WOODCUTTING, 500);
		assertEquals(500, team.get(0).getXp());
		assertEquals(50, team.get(1).getXp());
	}

	@Test
	public void classBonusStacksOnTheTenPercent()
	{
		Team team = new Team(List.of(
			new FellowshipCharacter(CharacterClass.ADVENTURER, 0),
			new FellowshipCharacter(CharacterClass.WIZARD, 0)));
		team.gainXp(Skill.MAGIC, 500);
		// Inactive Wizard: 500 x 2 (Magic bonus) x 10% = 100.
		assertEquals(100, team.get(1).getXp());
	}

	@Test
	public void smallDropsStillAddUpForInactiveCharacters()
	{
		Team team = new Team(Collections.emptyList());
		team.hire();
		// 10% of 7 XP is 0.7, so nothing yet...
		team.gainXp(Skill.MINING, 7);
		assertEquals(0, team.get(1).getXp());
		// ...but the fractions carry over: 0.7 x 10 = 7.
		for (int i = 0; i < 9; i++)
		{
			team.gainXp(Skill.MINING, 7);
		}
		assertEquals(7, team.get(1).getXp());
	}

	@Test
	public void switchingActiveMovesTheFullShare()
	{
		Team team = new Team(Collections.emptyList());
		team.hire();
		assertTrue(team.setActive(1));
		assertEquals(1, team.getActiveIndex());
		team.gainXp(Skill.WOODCUTTING, 500);
		assertEquals(50, team.get(0).getXp());
		assertEquals(500, team.get(1).getXp());
	}

	@Test
	public void cantMakeAMissingOrAlreadyActiveCharacterActive()
	{
		Team team = new Team(Collections.emptyList());
		assertFalse(team.setActive(0));
		assertFalse(team.setActive(1));
		assertFalse(team.setActive(-1));
		assertEquals(0, team.getActiveIndex());
	}

	@Test
	public void savedActiveIndexIsRestored()
	{
		Team team = new Team(List.of(
			new FellowshipCharacter(CharacterClass.ADVENTURER, 0),
			new FellowshipCharacter(CharacterClass.ADVENTURER, 0)), 1);
		assertEquals(1, team.getActiveIndex());
	}

	@Test
	public void badSavedActiveIndexFallsBackToTheFirst()
	{
		Team team = new Team(List.of(new FellowshipCharacter(CharacterClass.ADVENTURER, 0)), 5);
		assertEquals(0, team.getActiveIndex());
	}

	@Test
	public void activeAwayMeansNobodyGetsFullXp()
	{
		Team team = new Team(Collections.emptyList());
		team.hire();
		team.get(0).startExpedition(Expedition.LUMBRIDGE_SWAMP_CAVES, Instant.now());
		team.gainXp(Skill.WOODCUTTING, 500);
		assertEquals(0, team.get(0).getXp());
		assertEquals(50, team.get(1).getXp());
	}

	@Test
	public void awayCharactersGetNothing()
	{
		Team team = new Team(Collections.emptyList());
		team.hire();
		team.get(1).startExpedition(Expedition.LUMBRIDGE_SWAMP_CAVES, Instant.now());
		team.gainXp(Skill.WOODCUTTING, 500);
		assertEquals(500, team.get(0).getXp());
		assertEquals(0, team.get(1).getXp());
	}
}
