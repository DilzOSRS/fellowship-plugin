package com.dilzosrs.fellowship;

import static org.junit.Assert.assertEquals;
import java.util.EnumSet;
import java.util.List;
import org.junit.Test;

public class ExpeditionUnlockTest
{
	@Test
	public void expeditionsUnlockAsTheTeamGrows()
	{
		Team team = new Team(List.of(new FellowshipCharacter(CharacterClass.ADVENTURER, 0)));
		assertEquals(EnumSet.of(Expedition.LUMBRIDGE_SWAMP_CAVES), team.unlockedExpeditions());

		// Three characters make a party for the Dorgeshuun Caves.
		team.hire(CharacterClass.SKILLER);
		assertEquals(EnumSet.of(Expedition.LUMBRIDGE_SWAMP_CAVES), team.unlockedExpeditions());
		team.hire(CharacterClass.THIEF);
		assertEquals(EnumSet.of(Expedition.LUMBRIDGE_SWAMP_CAVES, Expedition.DORGESHUUN_CAVES),
			team.unlockedExpeditions());

		// The first Merchant unlocks the trade run.
		team.hire(CharacterClass.MERCHANT);
		assertEquals(EnumSet.allOf(Expedition.class), team.unlockedExpeditions());
	}

	@Test
	public void cardsOnlyOfferWhatTheirClassCanDo()
	{
		Team team = new Team(List.of(new FellowshipCharacter(CharacterClass.KNIGHT, 0),
			new FellowshipCharacter(CharacterClass.MERCHANT, 0)));
		assertEquals(List.of(Expedition.LUMBRIDGE_SWAMP_CAVES), new CharacterView(team, 0).expeditions);
		assertEquals(List.of(Expedition.LUMBRIDGE_SWAMP_CAVES, Expedition.GRAND_EXCHANGE_TRADE),
			new CharacterView(team, 1).expeditions);
	}
}
