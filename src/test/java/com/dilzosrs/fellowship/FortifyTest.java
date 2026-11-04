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
	public void eachSkillerTakesAQuarterOff()
	{
		assertEquals(Duration.ofMinutes(90), FORTIFY.getDuration());
		assertEquals(Duration.ofMinutes(67).plusSeconds(30), FORTIFY.getDuration(1, 1));
		assertEquals(Duration.ofMinutes(45), FORTIFY.getDuration(2, 2));
		assertEquals(Duration.ofMinutes(22).plusSeconds(30), FORTIFY.getDuration(3, 3));
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
	public void aPartyComesBackTogetherEvenAfterARestart()
	{
		Team team = new Team(List.of(new FellowshipCharacter(CharacterClass.SKILLER, 0),
			new FellowshipCharacter(CharacterClass.HARVESTER, 0),
			new FellowshipCharacter(CharacterClass.KNIGHT, 0)));
		// A Knight can't come along.
		assertFalse(team.sendParty(List.of(0, 2), FORTIFY, START));
		// A Skiller and a Harvester: two from the Skiller tree, so half off.
		assertTrue(team.sendParty(List.of(0, 1), FORTIFY, START));
		assertEquals(START.plus(Duration.ofMinutes(45)), team.get(0).getExpeditionEnd());

		FellowshipCharacter loaded = SavedCharacter.from(team.get(1)).toCharacter();
		assertEquals(START.plus(Duration.ofMinutes(45)), loaded.getExpeditionEnd());
	}

	@Test
	public void skillersSpeedUpEveryExpeditionButOnlyThreeCount()
	{
		// A Skiller in a Dorgeshuun party: 30 minutes becomes 22.5.
		Team team = new Team(List.of(new FellowshipCharacter(CharacterClass.KNIGHT, 0),
			new FellowshipCharacter(CharacterClass.ARCHER, 0),
			new FellowshipCharacter(CharacterClass.SKILLER, 0)));
		assertTrue(team.sendParty(List.of(0, 1, 2), Expedition.DORGESHUUN_CAVES, START));
		assertEquals(START.plus(Duration.ofMinutes(22).plusSeconds(30)), team.get(0).getExpeditionEnd());

		// Nobody from the Skiller tree: full length. A Skiller alone: a quarter off.
		assertEquals(Duration.ofMinutes(15), Expedition.LUMBRIDGE_SWAMP_CAVES.getDuration(1, 0));
		FellowshipCharacter skiller = new FellowshipCharacter(CharacterClass.ARTISAN, 0);
		skiller.startExpedition(Expedition.LUMBRIDGE_SWAMP_CAVES, START);
		assertEquals(START.plus(Duration.ofMinutes(11).plusSeconds(15)), skiller.getExpeditionEnd());

		// A fourth Skiller would add nothing, so a trip never drops below a quarter of its length.
		assertEquals(FORTIFY.getDuration(3, 3), FORTIFY.getDuration(4, 4));
	}

	@Test
	public void olderSavesWithoutASkillerCountTakeTheFullLength()
	{
		FellowshipCharacter skiller = new FellowshipCharacter(CharacterClass.SKILLER, 0);
		skiller.startExpedition(FORTIFY, START);
		SavedCharacter saved = SavedCharacter.from(skiller);
		saved.skillers = null;
		assertEquals(START.plus(Duration.ofMinutes(90)), saved.toCharacter().getExpeditionEnd());
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
