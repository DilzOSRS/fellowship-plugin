package com.dilzosrs.fellowship;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.time.Instant;
import java.util.List;
import java.util.Random;
import org.junit.Test;

public class MerchantTest
{
	private static Team knightAndMerchant()
	{
		return new Team(List.of(new FellowshipCharacter(CharacterClass.KNIGHT, 0),
			new FellowshipCharacter(CharacterClass.MERCHANT, 0)));
	}

	@Test
	public void onlyAMerchantCanGoTrading()
	{
		Expedition trade = Expedition.GRAND_EXCHANGE_TRADE;
		assertFalse(knightAndMerchant().sendParty(List.of(0), trade, Instant.now()));
		assertTrue(knightAndMerchant().sendParty(List.of(1), trade, Instant.now()));
		// Other expeditions are open to the Merchant too.
		assertTrue(knightAndMerchant().sendParty(List.of(1), Expedition.LUMBRIDGE_SWAMP_CAVES, Instant.now()));
	}

	@Test
	public void theTradeRunCostsCoinsAndBringsBackFiveBundlesWithNoCoins()
	{
		Expedition trade = Expedition.GRAND_EXCHANGE_TRADE;
		Random random = new Random(1);
		assertEquals(100, trade.getCost());
		assertEquals(0, trade.rollCoins(random));
		assertEquals(BundleType.TRADE, trade.rollBonusBundle(random));
		assertEquals(5, trade.getBonusBundleCount());
		assertEquals(0, BundleType.TRADE.rollCoins(random));
		assertEquals(0, BundleType.TRADE.getDropChance());
	}

	@Test
	public void otherExpeditionsAreFreeAndBringOneBundle()
	{
		assertEquals(0, Expedition.LUMBRIDGE_SWAMP_CAVES.getCost());
		assertEquals(1, Expedition.DORGESHUUN_CAVES.getBonusBundleCount());
	}
}
