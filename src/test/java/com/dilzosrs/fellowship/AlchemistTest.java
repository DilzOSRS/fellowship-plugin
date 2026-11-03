package com.dilzosrs.fellowship;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Random;
import net.runelite.api.Skill;
import org.junit.Test;

public class AlchemistTest
{
	private static final Instant START = Instant.parse("2026-10-29T12:00:00Z");

	@Test
	public void alchemistsGetDoubleHerbloreAndMagic()
	{
		assertEquals(2, CharacterClass.ALCHEMIST.getXpMultiplier(Skill.HERBLORE));
		assertEquals(2, CharacterClass.ALCHEMIST.getXpMultiplier(Skill.MAGIC));
		assertEquals(1, CharacterClass.ALCHEMIST.getXpMultiplier(Skill.WOODCUTTING));
	}

	@Test
	public void onlyAnAlchemistCanTransmuteAlone()
	{
		Expedition transmute = Expedition.TRANSMUTING_METALS;
		assertTrue(transmute.needsItem());
		assertFalse(Expedition.LUMBRIDGE_SWAMP_CAVES.needsItem());
		assertTrue(transmute.allows(CharacterClass.ALCHEMIST));
		assertFalse(transmute.allows(CharacterClass.KNIGHT));
		assertEquals(1, transmute.getMinPartySize());
		assertEquals(0, transmute.getThreatLevel());
		assertEquals(Duration.ofMinutes(10), transmute.getDuration());
		// It brings back no coins of its own: only the transmuted item's.
		assertEquals(0, transmute.rollCoins(new Random()));
		assertNull(transmute.rollBonusBundle(new Random()));
	}

	@Test
	public void theCarriedItemIsSaved()
	{
		Team team = new Team(List.of(new FellowshipCharacter(CharacterClass.ALCHEMIST, 0)));
		assertTrue(team.sendParty(List.of(0), Expedition.TRANSMUTING_METALS, START));
		GearItem item = new GearItem(GearType.SWORD, Rarity.RARE);
		team.get(0).setCarrying(item);

		FellowshipCharacter loaded = SavedCharacter.from(team.get(0)).toCharacter();
		assertEquals(Expedition.TRANSMUTING_METALS, loaded.getExpedition());
		assertEquals(item.getName(), loaded.getCarrying().getName());
		assertEquals(Rarity.RARE, loaded.getCarrying().getRarity());
	}

	@Test
	public void alchCastsRollAtTheirOwnChance()
	{
		int first = RareUnlock.ALCH_CHANCE;
		assertTrue(RareUnlock.rollAlch(0, always(first - 1)));
		assertFalse(RareUnlock.rollAlch(0, always(first)));
		// Five times harder per Alchemist already on the team.
		assertTrue(RareUnlock.rollAlch(1, always(first / 5 - 1)));
		assertFalse(RareUnlock.rollAlch(1, always(first / 5)));
	}

	private static Random always(int value)
	{
		return new Random()
		{
			@Override
			public int nextInt(int bound)
			{
				return Math.min(value, bound - 1);
			}
		};
	}
}
