package com.dilzosrs.fellowship;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class AdventurerTest
{
	private static final long CAP_XP = LevelCurve.xpForLevel(Adventurer.LEVEL_CAP);

	@Test
	public void gainsXpBelowTheCap()
	{
		Adventurer adventurer = new Adventurer("Adventurer", 0);
		adventurer.addXp(2_000);
		assertEquals(2_000, adventurer.getXp());
		assertEquals(2, adventurer.getLevel());
		assertFalse(adventurer.isAtLevelCap());
	}

	@Test
	public void xpStopsAtTheCap()
	{
		Adventurer adventurer = new Adventurer("Adventurer", CAP_XP - 100);
		adventurer.addXp(5_000);
		assertEquals(CAP_XP, adventurer.getXp());
		assertEquals(Adventurer.LEVEL_CAP, adventurer.getLevel());
		assertTrue(adventurer.isAtLevelCap());
	}

	@Test
	public void xpAtTheCapIsWasted()
	{
		Adventurer adventurer = new Adventurer("Adventurer", CAP_XP);
		adventurer.addXp(1_000);
		assertEquals(CAP_XP, adventurer.getXp());
	}

	@Test
	public void savedXpOverTheCapIsTrimmed()
	{
		Adventurer adventurer = new Adventurer("Adventurer", 500_000);
		assertEquals(CAP_XP, adventurer.getXp());
		assertEquals(Adventurer.LEVEL_CAP, adventurer.getLevel());
	}
}
