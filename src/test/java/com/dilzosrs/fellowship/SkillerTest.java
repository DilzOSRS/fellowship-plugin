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
	public void skillersCostDoubleEachTime()
	{
		Guild guild = new Guild(0, 0, 0);
		assertEquals(500, guild.getSkillerCost());
		guild.useSkillerHire();
		assertEquals(1_000, guild.getSkillerCost());
		guild.useSkillerHire();
		assertEquals(2_000, guild.getSkillerCost());
	}

	@Test
	public void skillersDontNeedAGuildHire()
	{
		// Guild level 1: no hires, but a Skiller can still be hired with enough coins.
		Guild guild = new Guild(0, 0, 0);
		assertEquals(0, guild.getHiresAvailable());
		assertTrue(guild.canHireSkiller(500));
		assertFalse(guild.canHireSkiller(499));
	}

	@Test
	public void onlyThreeSkillers()
	{
		Guild guild = new Guild(0, 0, 3);
		assertFalse(guild.canHireSkiller(1_000_000));
		assertFalse(guild.useSkillerHire());
		assertEquals(3, guild.getSkillersHired());
	}

	@Test
	public void skillersStartLikeAdventurers()
	{
		Team team = new Team(Collections.emptyList());
		team.hire(CharacterClass.SKILLER);
		FellowshipCharacter skiller = team.get(1);
		assertEquals(CharacterClass.SKILLER, skiller.getCharacterClass());
		assertEquals(0, skiller.getXp());
		assertEquals(10, skiller.getLevelCap());
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
		assertEquals(99, skiller.getLevelCap());
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
		assertTrue(harvester.canEquip(new GearItem(GearType.TRINKET, Rarity.RARE)));
	}

	@Test
	public void skillersAreNamedByClass()
	{
		Team team = new Team(Collections.emptyList());
		team.hire(CharacterClass.SKILLER);
		team.hire(CharacterClass.SKILLER);
		assertEquals("Adventurer", team.getDisplayName(0));
		assertEquals("Skiller", team.getDisplayName(1));
		assertEquals("Skiller 2", team.getDisplayName(2));
	}
}
