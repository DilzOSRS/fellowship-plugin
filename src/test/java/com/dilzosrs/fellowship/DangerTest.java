package com.dilzosrs.fellowship;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import com.google.gson.Gson;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Random;
import org.junit.Test;

public class DangerTest
{
	private static final Instant START = Instant.parse("2026-10-26T20:00:00Z");
	private static final Instant BACK = START.plus(Duration.ofHours(2));

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
	public void enoughAtkNeverFails()
	{
		assertEquals(0, Danger.failChance(8, 8), 0.0001);
		assertEquals(0, Danger.failChance(8, 12), 0.0001);
	}

	@Test
	public void failChanceGrowsWithTheGap()
	{
		assertEquals(0.5, Danger.failChance(8, 4), 0.0001);
		assertEquals(0.875, Danger.failChance(8, 1), 0.0001);
		assertEquals(1.0, Danger.failChance(8, 0), 0.0001);
	}

	@Test
	public void safeExpeditionsNeverFail()
	{
		assertEquals(0, Danger.failChance(0, 0), 0.0001);
	}

	@Test
	public void rollingUsesTheFailChance()
	{
		// 50% fail chance: a roll of 0.3 fails, 0.7 succeeds.
		assertFalse(Danger.rollSuccess(8, 4, rolling(0.3)));
		assertTrue(Danger.rollSuccess(8, 4, rolling(0.7)));
		// No chance of failing: even the lowest roll succeeds.
		assertTrue(Danger.rollSuccess(8, 8, rolling(0.0)));
	}

	@Test
	public void magCountsHalfTowardsAtkAndDef()
	{
		FellowshipCharacter wizard = new FellowshipCharacter(CharacterClass.WIZARD, 0);
		wizard.equip(new GearItem(GearType.STAFF, Rarity.RARE));
		wizard.equip(new GearItem(GearType.TRINKET, Rarity.EPIC));
		assertEquals(6 + 5, wizard.getEffectiveAtk(), 0.0001);
		assertEquals(0 + 5, wizard.getEffectiveDef(), 0.0001);
	}

	@Test
	public void partyAtkIsTheAverage()
	{
		Team team = new Team(List.of(
			new FellowshipCharacter(CharacterClass.KNIGHT, 0),
			new FellowshipCharacter(CharacterClass.KNIGHT, 0),
			new FellowshipCharacter(CharacterClass.KNIGHT, 0)));
		team.get(0).equip(new GearItem(GearType.SWORD, Rarity.LEGENDARY));
		team.get(1).equip(new GearItem(GearType.SWORD, Rarity.RARE));
		// 15 + 6 + 0 = 21, averaged over 3 = 7.
		assertEquals(7, team.averageAtk(List.of(0, 1, 2)), 0.0001);
	}

	@Test
	public void failedPartyComesHomeWithNoXp()
	{
		Team team = new Team(List.of(
			new FellowshipCharacter(CharacterClass.ADVENTURER, 0),
			new FellowshipCharacter(CharacterClass.ADVENTURER, 0),
			new FellowshipCharacter(CharacterClass.ADVENTURER, 0)));
		team.sendParty(List.of(0, 1, 2), Expedition.DORGESHUUN_CAVES, START);
		assertEquals(List.of(0, 1, 2), team.collectParty(0, BACK, false));
		for (int i = 0; i < 3; i++)
		{
			assertFalse(team.get(i).isAway());
			assertEquals(0, team.get(i).getXp());
		}
	}

	@Test
	public void gearCantChangeWhileAway()
	{
		FellowshipCharacter knight = new FellowshipCharacter(CharacterClass.KNIGHT, 0);
		knight.equip(new GearItem(GearType.SWORD, Rarity.COMMON));
		knight.startExpedition(Expedition.LUMBRIDGE_SWAMP_CAVES, START);
		assertFalse(knight.canChangeGear());
		assertNull(knight.unequip(GearType.Slot.WEAPON));
		assertEquals(1, knight.getAtk());
	}

	@Test(expected = IllegalStateException.class)
	public void equippingWhileAwayIsRefused()
	{
		FellowshipCharacter knight = new FellowshipCharacter(CharacterClass.KNIGHT, 0);
		knight.startExpedition(Expedition.LUMBRIDGE_SWAMP_CAVES, START);
		knight.equip(new GearItem(GearType.SWORD, Rarity.RARE));
	}

	@Test
	public void awayCharactersKeepTheirGearAfterLoading()
	{
		Gson gson = new Gson();
		FellowshipCharacter knight = new FellowshipCharacter(CharacterClass.KNIGHT, 0);
		knight.equip(new GearItem(GearType.SWORD, Rarity.RARE));
		knight.startExpedition(Expedition.DORGESHUUN_CAVES, START);

		FellowshipCharacter loaded = gson.fromJson(gson.toJson(SavedCharacter.from(knight)), SavedCharacter.class).toCharacter();

		assertTrue(loaded.isAway());
		assertEquals(6, loaded.getAtk());
	}
}
