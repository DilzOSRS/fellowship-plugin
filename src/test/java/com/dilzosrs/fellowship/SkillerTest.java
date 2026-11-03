package com.dilzosrs.fellowship;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.util.Collections;
import java.util.List;
import net.runelite.api.Skill;
import org.junit.Test;

public class SkillerTest
{
	@Test
	public void skillersStartLikeAdventurers()
	{
		Team team = new Team(Collections.emptyList());
		team.add(CharacterClass.SKILLER);
		FellowshipCharacter skiller = team.get(1);
		assertEquals(CharacterClass.SKILLER, skiller.getCharacterClass());
		assertEquals(0, skiller.getXp());
		// No bonus before their class advancement.
		assertEquals(1, CharacterClass.SKILLER.getXpMultiplier(Skill.WOODCUTTING));
	}

	@Test
	public void skillersAdvanceIntoSkillingPaths()
	{
		assertEquals(List.of(CharacterClass.HARVESTER, CharacterClass.ARTISAN, CharacterClass.EXPLORER),
			CharacterClass.SKILLER.getEvolutions());
		FellowshipCharacter skiller = new FellowshipCharacter(CharacterClass.SKILLER, LevelCurve.xpForLevel(10));
		assertFalse(skiller.evolve(CharacterClass.KNIGHT));
		assertTrue(skiller.evolve(CharacterClass.HARVESTER));
	}

	@Test
	public void pathsGetDoubleXpInTheirSkills()
	{
		assertEquals(2, CharacterClass.HARVESTER.getXpMultiplier(Skill.WOODCUTTING));
		assertEquals(2, CharacterClass.ARTISAN.getXpMultiplier(Skill.SMITHING));
		assertEquals(2, CharacterClass.EXPLORER.getXpMultiplier(Skill.AGILITY));
		assertEquals(1, CharacterClass.HARVESTER.getXpMultiplier(Skill.SMITHING));
		assertEquals(1, CharacterClass.EXPLORER.getXpMultiplier(Skill.ATTACK));
	}

	@Test
	public void skillersCanOnlyWearGenericGear()
	{
		FellowshipCharacter harvester = new FellowshipCharacter(CharacterClass.HARVESTER, 0);
		assertFalse(harvester.canEquip(new GearItem(GearType.SWORD, Rarity.RARE)));
		assertTrue(harvester.canEquip(new GearItem(GearType.TRINKET, Rarity.COMMON)));
	}

	@Test
	public void skillersAreNamedByClass()
	{
		Team team = new Team(Collections.emptyList());
		team.add(CharacterClass.SKILLER);
		team.add(CharacterClass.SKILLER);
		assertEquals("Adventurer", team.getDisplayName(0));
		assertEquals("Skiller", team.getDisplayName(1));
		assertEquals("Skiller 2", team.getDisplayName(2));
	}
}
