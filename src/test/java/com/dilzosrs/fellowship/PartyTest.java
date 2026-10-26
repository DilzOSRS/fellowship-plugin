package com.dilzosrs.fellowship;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import org.junit.Test;

public class PartyTest
{
	private static final Instant START = Instant.parse("2026-10-26T20:00:00Z");
	private static final Instant BACK = START.plus(Duration.ofHours(2));

	private static Team teamOf(int size)
	{
		Team team = new Team(Collections.emptyList());
		for (int i = 1; i < size; i++)
		{
			team.hire();
		}
		return team;
	}

	@Test
	public void dorgeshuunCavesNeedsThreeAndIsDangerous()
	{
		assertEquals(3, Expedition.DORGESHUUN_CAVES.getPartySize());
		assertEquals(8, Expedition.DORGESHUUN_CAVES.getThreatLevel());
		assertEquals(Duration.ofHours(2), Expedition.DORGESHUUN_CAVES.getDuration());
		assertEquals(1, Expedition.LUMBRIDGE_SWAMP_CAVES.getPartySize());
		assertEquals(0, Expedition.LUMBRIDGE_SWAMP_CAVES.getThreatLevel());
	}

	@Test
	public void sendingAPartySendsEveryone()
	{
		Team team = teamOf(4);
		assertTrue(team.sendParty(List.of(0, 2, 3), Expedition.DORGESHUUN_CAVES, START));
		assertTrue(team.get(0).isAway());
		assertFalse(team.get(1).isAway());
		assertTrue(team.get(2).isAway());
		assertTrue(team.get(3).isAway());
		assertEquals(List.of(0, 2, 3), team.partyOf(2));
	}

	@Test
	public void partyMustBeTheRightSize()
	{
		Team team = teamOf(4);
		assertFalse(team.sendParty(List.of(0, 1), Expedition.DORGESHUUN_CAVES, START));
		assertFalse(team.sendParty(List.of(0, 1, 2, 3), Expedition.DORGESHUUN_CAVES, START));
		assertFalse(team.get(0).isAway());
	}

	@Test
	public void partyCantHaveRepeatsOrAwayMembers()
	{
		Team team = teamOf(4);
		assertFalse(team.sendParty(List.of(0, 0, 1), Expedition.DORGESHUUN_CAVES, START));
		team.get(3).startExpedition(Expedition.LUMBRIDGE_SWAMP_CAVES, START);
		assertFalse(team.sendParty(List.of(0, 1, 3), Expedition.DORGESHUUN_CAVES, START));
		// Nobody was sent by the failed attempt.
		assertFalse(team.get(0).isAway());
		assertFalse(team.get(1).isAway());
	}

	@Test
	public void separateTripsAreSeparateParties()
	{
		Team team = teamOf(4);
		team.get(0).startExpedition(Expedition.LUMBRIDGE_SWAMP_CAVES, START);
		team.get(1).startExpedition(Expedition.LUMBRIDGE_SWAMP_CAVES, START.plusSeconds(60));
		assertEquals(List.of(0), team.partyOf(0));
		assertEquals(List.of(1), team.partyOf(1));
		assertTrue(team.partyOf(2).isEmpty());
	}

	@Test
	public void collectingBringsTheWholePartyHomeWithXpEach()
	{
		Team team = teamOf(3);
		team.sendParty(List.of(0, 1, 2), Expedition.DORGESHUUN_CAVES, START);
		assertTrue(team.collectParty(1, BACK.minusSeconds(1)).isEmpty());
		assertEquals(List.of(0, 1, 2), team.collectParty(1, BACK));
		for (int i = 0; i < 3; i++)
		{
			assertFalse(team.get(i).isAway());
			assertEquals(3_000, team.get(i).getXp());
		}
	}

	@Test
	public void partySurvivesSavingAndLoading()
	{
		Team team = teamOf(3);
		team.sendParty(List.of(0, 1, 2), Expedition.DORGESHUUN_CAVES, START);
		com.google.gson.Gson gson = new com.google.gson.Gson();
		List<FellowshipCharacter> loaded = new java.util.ArrayList<>();
		for (FellowshipCharacter character : team.getCharacters())
		{
			loaded.add(gson.fromJson(gson.toJson(SavedCharacter.from(character)), SavedCharacter.class).toCharacter());
		}
		assertEquals(List.of(0, 1, 2), new Team(loaded).partyOf(0));
	}

	@Test
	public void bonusBundleIsRolledFromTheExpedition()
	{
		Random always0 = new Random()
		{
			@Override
			public int nextInt(int bound)
			{
				return 0;
			}
		};
		assertEquals(BundleType.BOSS, Expedition.DORGESHUUN_CAVES.rollBonusBundle(always0));
		assertNull(Expedition.LUMBRIDGE_SWAMP_CAVES.rollBonusBundle(always0));
	}
}
