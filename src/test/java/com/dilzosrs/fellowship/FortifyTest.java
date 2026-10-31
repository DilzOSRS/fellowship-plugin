package com.dilzosrs.fellowship;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.junit.Test;

public class FortifyTest
{
	private static final Instant START = Instant.parse("2026-10-30T12:00:00Z");
	private static final Expedition FORTIFY = Expedition.FORTIFYING_ENCAMPMENT;

	@Test
	public void eachExtraSkillerMakesItHalfAnHourShorter()
	{
		assertEquals(Duration.ofMinutes(90), FORTIFY.getDuration(1));
		assertEquals(Duration.ofMinutes(60), FORTIFY.getDuration(2));
		assertEquals(Duration.ofMinutes(30), FORTIFY.getDuration(3));
		assertTrue(FORTIFY.allowsPartySize(1));
		assertTrue(FORTIFY.allowsPartySize(3));
		assertFalse(FORTIFY.allowsPartySize(4));
		assertEquals(0, FORTIFY.getThreatLevel());
		assertEquals(5_000, FORTIFY.getXpReward());
	}

	@Test
	public void needsGuildLevelTwoAndASkiller()
	{
		Team team = new Team(List.of(new FellowshipCharacter(CharacterClass.KNIGHT, 0)));
		assertFalse(FORTIFY.isUnlockedFor(team, 5));
		team.add(CharacterClass.SKILLER);
		assertFalse(FORTIFY.isUnlockedFor(team, 1));
		assertTrue(FORTIFY.isUnlockedFor(team, 2));
	}

	@Test
	public void theWholeSkillerTreeCanGoButNobodyElse()
	{
		assertTrue(FORTIFY.allows(CharacterClass.SKILLER));
		assertTrue(FORTIFY.allows(CharacterClass.HARVESTER));
		assertFalse(FORTIFY.allows(CharacterClass.KNIGHT));
		assertFalse(FORTIFY.allows(CharacterClass.ADVENTURER));
	}

	@Test
	public void aBiggerPartyComesBackSoonerEvenAfterARestart()
	{
		Team team = new Team(List.of(new FellowshipCharacter(CharacterClass.SKILLER, 0),
			new FellowshipCharacter(CharacterClass.HARVESTER, 0),
			new FellowshipCharacter(CharacterClass.KNIGHT, 0)));
		// A Knight can't come along.
		assertFalse(team.sendParty(List.of(0, 2), FORTIFY, START));
		assertTrue(team.sendParty(List.of(0, 1), FORTIFY, START));
		assertEquals(START.plus(Duration.ofMinutes(60)), team.get(0).getExpeditionEnd());

		FellowshipCharacter loaded = SavedCharacter.from(team.get(1)).toCharacter();
		assertEquals(START.plus(Duration.ofMinutes(60)), loaded.getExpeditionEnd());
	}

	@Test
	public void fixedPartiesStillNeedTheirExactSize()
	{
		Team team = new Team(List.of(new FellowshipCharacter(CharacterClass.KNIGHT, 0),
			new FellowshipCharacter(CharacterClass.ARCHER, 0)));
		assertFalse(team.sendParty(List.of(0, 1), Expedition.DORGESHUUN_CAVES, START));
		assertFalse(team.sendParty(List.of(0, 1), Expedition.LUMBRIDGE_SWAMP_CAVES, START));
	}
}
