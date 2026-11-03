package com.dilzosrs.fellowship;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.util.Collections;
import java.util.List;
import net.runelite.api.Skill;
import org.junit.Test;

public class GuildTest
{
	@Test
	public void guildCurveMatchesTheChosenNumbers()
	{
		assertEquals(0, Guild.xpForLevel(1));
		assertEquals(20, Guild.xpForLevel(2));
		assertEquals(50, Guild.xpForLevel(3));
		assertEquals(90, Guild.xpForLevel(4));
		assertEquals(140, Guild.xpForLevel(5));
	}

	@Test
	public void guildLevelAtBoundaries()
	{
		assertEquals(1, Guild.levelForXp(0));
		assertEquals(1, Guild.levelForXp(19));
		assertEquals(2, Guild.levelForXp(20));
		assertEquals(2, Guild.levelForXp(49));
		assertEquals(3, Guild.levelForXp(50));
	}

	@Test
	public void fullXpWaitsForTheLevelUpToBePaidFor()
	{
		Guild guild = new Guild(15, 1);
		assertFalse(guild.isReadyToLevelUp());
		assertFalse(guild.levelUp());
		// 20 XP fills level 2, and the extra 10 is dropped until the level is paid for.
		guild.addXp(15);
		assertEquals(20, guild.getXp());
		assertEquals(1, guild.getLevel());
		assertTrue(guild.isReadyToLevelUp());

		assertTrue(guild.levelUp());
		assertEquals(2, guild.getLevel());
		assertFalse(guild.isReadyToLevelUp());
		guild.addXp(100);
		assertEquals(50, guild.getXp());
	}

	@Test
	public void levelUpsCostOneHundredCoinsDoublingEachLevel()
	{
		assertEquals(100, Guild.levelUpCost(1));
		assertEquals(200, Guild.levelUpCost(2));
		assertEquals(400, Guild.levelUpCost(3));
		assertEquals(800, new Guild(0, 4).getLevelUpCost());
	}

	@Test
	public void oldSavesKeepTheirLevel()
	{
		// Saves from before levels cost coins only stored the XP.
		Guild guild = new Guild(60, Guild.levelForXp(60));
		assertEquals(3, guild.getLevel());
		assertEquals(60, guild.getXp());
	}

	@Test
	public void characterReportsLevelsGained()
	{
		FellowshipCharacter character = new FellowshipCharacter(CharacterClass.KNIGHT, 0);
		assertEquals(0, character.addXp(1_000));
		// 1,500 XP is level 2 and 3,750 is level 3, so this jumps two levels at once.
		assertEquals(2, character.addXp(3_000));
	}

	@Test
	public void noLevelsGainedAtTheCap()
	{
		FellowshipCharacter character = new FellowshipCharacter(CharacterClass.ADVENTURER,
			LevelCurve.xpForLevel(LevelCurve.MAX_LEVEL));
		assertEquals(0, character.addXp(50_000));
	}

	@Test
	public void teamCountsLevelUpsFromEveryCharacter()
	{
		Team team = new Team(List.of(
			new FellowshipCharacter(CharacterClass.ADVENTURER, 1_400),
			new FellowshipCharacter(CharacterClass.ADVENTURER, 1_490)));
		// Active gets 200 (1,600: level 2), inactive gets 20 (1,510: level 2).
		assertEquals(2, team.gainXp(Skill.WOODCUTTING, 200));
	}

	@Test
	public void noLevelUpsMeansNoGuildXp()
	{
		Team team = new Team(Collections.emptyList());
		assertEquals(0, team.gainXp(Skill.WOODCUTTING, 10));
	}
}
