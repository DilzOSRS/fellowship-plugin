package com.dilzosrs.fellowship;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.junit.Test;

public class VoyagePrepTest
{
	private static final Instant START = Instant.parse("2026-11-03T12:00:00Z");
	private static final Expedition PREP = Expedition.PREPPING_FOR_VOYAGE;

	@Test
	public void unlocksWithTheFirstSailorAndIsGoneOnceDone()
	{
		Team team = new Team(List.of(new FellowshipCharacter(CharacterClass.KNIGHT, 0)));
		assertFalse(PREP.isUnlockedFor(team, 1));
		team.add(CharacterClass.SAILOR);
		assertTrue(PREP.isUnlockedFor(team, 1));
		team.setVoyagePrepared(true);
		assertFalse(PREP.isUnlockedFor(team, 1));
	}

	@Test
	public void anAdvancedSailorCountsToo()
	{
		Team team = new Team(List.of(new FellowshipCharacter(CharacterClass.SWASHBUCKLER, 0)));
		assertTrue(PREP.isUnlockedFor(team, 1));
		assertTrue(PREP.canLead(CharacterClass.SWASHBUCKLER));
	}

	@Test
	public void onlyTheSailorTreeLeadsButAnyoneHelps()
	{
		assertTrue(PREP.canLead(CharacterClass.SAILOR));
		assertFalse(PREP.canLead(CharacterClass.KNIGHT));
		assertTrue(PREP.allows(CharacterClass.KNIGHT));

		Team team = new Team(List.of(new FellowshipCharacter(CharacterClass.KNIGHT, 0),
			new FellowshipCharacter(CharacterClass.ARCHER, 0),
			new FellowshipCharacter(CharacterClass.SAILOR, 0)));
		// No Sailor, no trip.
		assertFalse(team.sendParty(List.of(0, 1), PREP, START));
		assertTrue(team.sendParty(List.of(2, 0, 1), PREP, START));
	}

	@Test
	public void eachHelperTakesTenPercentOffUpToFiveInAll()
	{
		assertEquals(Duration.ofMinutes(60), PREP.getDuration(1, 0));
		assertEquals(Duration.ofMinutes(54), PREP.getDuration(2, 0));
		assertEquals(Duration.ofMinutes(36), PREP.getDuration(5, 0));
		assertTrue(PREP.allowsPartySize(5));
		assertFalse(PREP.allowsPartySize(6));
		assertEquals(0, PREP.getThreatLevel());
	}

	@Test
	public void theExpeditionBonusComesFirstThenSkillers()
	{
		// Sailor + 2 Skillers: 20% off the hour (48 min), then 50% off that (24 min).
		assertEquals(Duration.ofMinutes(24), PREP.getDuration(3, 2));
		// Sailor + 4 Skillers: 40% off (36 min), then only 3 Skillers count: 75% off (9 min).
		assertEquals(Duration.ofMinutes(9), PREP.getDuration(5, 4));
	}

	@Test
	public void theXpIsSharedAcrossTheParty()
	{
		assertEquals(10_000, PREP.getXpReward(1));
		assertEquals(5_000, PREP.getXpReward(2));
		assertEquals(2_000, PREP.getXpReward(5));
		// Other expeditions give everyone the full amount.
		assertEquals(3_000, Expedition.DORGESHUUN_CAVES.getXpReward(3));

		Team team = new Team(List.of(new FellowshipCharacter(CharacterClass.SAILOR, 0),
			new FellowshipCharacter(CharacterClass.KNIGHT, 0)));
		assertTrue(team.sendParty(List.of(0, 1), PREP, START));
		Instant back = START.plus(Duration.ofMinutes(54));
		assertEquals(back, team.get(0).getExpeditionEnd());
		team.collectParty(0, back, true);
		assertEquals(5_000, team.get(0).getXp());
		assertEquals(5_000, team.get(1).getXp());
	}

	@Test
	public void theTripLengthSurvivesARestart()
	{
		Team team = new Team(List.of(new FellowshipCharacter(CharacterClass.SAILOR, 0),
			new FellowshipCharacter(CharacterClass.SKILLER, 0)));
		assertTrue(team.sendParty(List.of(0, 1), PREP, START));
		// 10% off for the helper (54 min), then 25% off for the Skiller (40.5 min).
		Instant back = START.plus(Duration.ofMinutes(40).plusSeconds(30));
		assertEquals(back, team.get(0).getExpeditionEnd());
		assertEquals(back, SavedCharacter.from(team.get(0)).toCharacter().getExpeditionEnd());
	}
}
