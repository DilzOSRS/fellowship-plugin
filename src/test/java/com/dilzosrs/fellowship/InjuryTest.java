package com.dilzosrs.fellowship;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import com.google.gson.Gson;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import net.runelite.api.Skill;
import org.junit.Test;

public class InjuryTest
{
	private static final Instant NOW = Instant.parse("2026-10-26T20:00:00Z");

	/** A Random whose nextDouble always returns the same value. */
	private static Random rolling(double value)
	{
		return new Random()
		{
			@Override
			public double nextDouble()
			{
				return value;
			}
		};
	}

	@Test
	public void safeExpeditionsNeverInjure()
	{
		assertTrue(Danger.rollInjuries(0, false, new double[]{0, 0, 0}, rolling(0)).isEmpty());
	}

	@Test
	public void successWithEnoughDefIsSafe()
	{
		assertTrue(Danger.rollInjuries(8, true, new double[]{8, 9, 10}, rolling(0)).isEmpty());
	}

	@Test
	public void successWithLowDefCanHurtOne()
	{
		// DEF 4 vs threat 8: a 50% chance of one injury.
		assertEquals(1, Danger.rollInjuries(8, true, new double[]{4, 4, 4}, rolling(0.3)).size());
		assertTrue(Danger.rollInjuries(8, true, new double[]{4, 4, 4}, rolling(0.7)).isEmpty());
	}

	@Test
	public void failureWithEnoughDefRarelyHurts()
	{
		// 10% chance of one injury.
		assertEquals(1, Danger.rollInjuries(8, false, new double[]{8, 8, 8}, rolling(0.05)).size());
		assertTrue(Danger.rollInjuries(8, false, new double[]{8, 8, 8}, rolling(0.2)).isEmpty());
	}

	@Test
	public void failureWithLowDefCanHurtEveryone()
	{
		// DEF 0 vs threat 8: each member has a 100% chance (50% + 100% / 2).
		assertEquals(List.of(0, 1, 2), Danger.rollInjuries(8, false, new double[]{0, 0, 0}, rolling(0.99)));
		// DEF 4: each member 75%.
		assertEquals(List.of(0, 1, 2), Danger.rollInjuries(8, false, new double[]{4, 4, 4}, rolling(0.7)));
		assertTrue(Danger.rollInjuries(8, false, new double[]{4, 4, 4}, rolling(0.8)).isEmpty());
	}

	@Test
	public void lowerDefIsMoreLikelyToBePicked()
	{
		// Weights: DEF 0 -> 9, DEF 8 -> 1. Out of 10, a roll below 0.9 picks the first member.
		double[] defs = {0, 8};
		assertEquals(0, Danger.pickWeakest(8, defs, rolling(0.5)));
		assertEquals(1, Danger.pickWeakest(8, defs, rolling(0.95)));
	}

	@Test
	public void restingCharactersEarnNoXp()
	{
		FellowshipCharacter knight = new FellowshipCharacter(CharacterClass.KNIGHT, 0);
		knight.injure(Instant.now().plus(Duration.ofHours(10)));
		knight.gainXp(Skill.ATTACK, 500);
		assertEquals(0, knight.getXp());
	}

	@Test
	public void restingEndsWhenHealed()
	{
		FellowshipCharacter knight = new FellowshipCharacter(CharacterClass.KNIGHT, 0);
		knight.injure(NOW.plus(Duration.ofHours(10)));
		assertTrue(knight.isResting(NOW));
		assertFalse(knight.isAvailable(NOW));
		assertFalse(knight.isResting(NOW.plus(Duration.ofHours(10))));
		assertTrue(knight.isAvailable(NOW.plus(Duration.ofHours(10))));
	}

	@Test
	public void restingCharactersCantBeSent()
	{
		Team team = new Team(Collections.emptyList());
		team.hire();
		team.hire();
		team.get(2).injure(NOW.plus(Duration.ofHours(1)));
		assertFalse(team.sendParty(List.of(0, 1, 2), Expedition.DORGESHUUN_CAVES, NOW));
		assertFalse(team.sendParty(List.of(2), Expedition.LUMBRIDGE_SWAMP_CAVES, NOW));
		assertTrue(team.sendParty(List.of(0), Expedition.LUMBRIDGE_SWAMP_CAVES, NOW));
	}

	@Test
	public void restingCharactersCanStillChangeGear()
	{
		FellowshipCharacter knight = new FellowshipCharacter(CharacterClass.KNIGHT, 0);
		knight.injure(NOW.plus(Duration.ofHours(1)));
		assertTrue(knight.canChangeGear());
	}

	@Test
	public void injurySurvivesSavingAndLoading()
	{
		Gson gson = new Gson();
		FellowshipCharacter knight = new FellowshipCharacter(CharacterClass.KNIGHT, 0);
		Instant healed = NOW.plus(Duration.ofHours(10));
		knight.injure(healed);
		FellowshipCharacter loaded = gson.fromJson(gson.toJson(SavedCharacter.from(knight)), SavedCharacter.class).toCharacter();
		assertEquals(healed, loaded.getRestingUntil());
	}
}
