package com.dilzosrs.fellowship;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.util.EnumSet;
import java.util.List;
import org.junit.Test;

public class ExpeditionUnlockTest
{
	@Test
	public void expeditionsUnlockAsTheTeamGrows()
	{
		// A new team has no expeditions until someone takes a class advancement.
		Team team = new Team(List.of(new FellowshipCharacter(CharacterClass.ADVENTURER, LevelCurve.xpForLevel(10))));
		assertEquals(EnumSet.noneOf(Expedition.class), team.unlockedExpeditions(1));

		// Three characters make a party for the Dorgeshuun Caves.
		team.add(CharacterClass.SKILLER);
		assertEquals(EnumSet.noneOf(Expedition.class), team.unlockedExpeditions(1));
		team.add(CharacterClass.THIEF);
		assertEquals(EnumSet.of(Expedition.DORGESHUUN_CAVES), team.unlockedExpeditions(1));

		// The first Merchant unlocks the trade run.
		team.add(CharacterClass.MERCHANT);
		assertEquals(EnumSet.of(Expedition.DORGESHUUN_CAVES, Expedition.GRAND_EXCHANGE_TRADE),
			team.unlockedExpeditions(1));

		// The first Alchemist unlocks Transmuting metals.
		team.add(CharacterClass.ALCHEMIST);
		assertEquals(EnumSet.of(Expedition.DORGESHUUN_CAVES, Expedition.GRAND_EXCHANGE_TRADE,
			Expedition.TRANSMUTING_METALS), team.unlockedExpeditions(1));

		// The first class advancement unlocks the Swamp Caves.
		assertTrue(team.get(0).evolve(CharacterClass.KNIGHT));
		assertEquals(EnumSet.complementOf(EnumSet.of(Expedition.FORTIFYING_ENCAMPMENT,
			Expedition.PREPPING_FOR_VOYAGE)), team.unlockedExpeditions(1));

		// Guild level 2 with a Skiller on the team unlocks Fortifying the Encampment.
		assertEquals(EnumSet.complementOf(EnumSet.of(Expedition.PREPPING_FOR_VOYAGE)), team.unlockedExpeditions(2));

		// The first Sailor unlocks Prep for voyage.
		team.add(CharacterClass.SAILOR);
		assertEquals(EnumSet.allOf(Expedition.class), team.unlockedExpeditions(2));
	}

	@Test
	public void onlyAdvancementsCountAsAdvanced()
	{
		assertFalse(CharacterClass.ADVENTURER.isAdvanced());
		assertFalse(CharacterClass.THIEF.isAdvanced());
		assertFalse(CharacterClass.MERCHANT.isAdvanced());
		assertTrue(CharacterClass.KNIGHT.isAdvanced());
		assertTrue(CharacterClass.BANDIT.isAdvanced());
		assertTrue(CharacterClass.ZAROSIAN.isAdvanced());
	}

	@Test
	public void cardsOnlyOfferWhatTheirClassCanDo()
	{
		Team team = new Team(List.of(new FellowshipCharacter(CharacterClass.KNIGHT, 0),
			new FellowshipCharacter(CharacterClass.MERCHANT, 0)));
		assertEquals(List.of(Expedition.LUMBRIDGE_SWAMP_CAVES), new CharacterView(team, 0, 1).expeditions);
		assertEquals(List.of(Expedition.LUMBRIDGE_SWAMP_CAVES, Expedition.GRAND_EXCHANGE_TRADE),
			new CharacterView(team, 1, 1).expeditions);
	}
}
