package com.dilzosrs.fellowship;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class LevelCurveTest
{
	@Test
	public void xpForLevelMatchesTheChosenCurve()
	{
		assertEquals(0, LevelCurve.xpForLevel(1));
		assertEquals(1_500, LevelCurve.xpForLevel(2));
		assertEquals(3_750, LevelCurve.xpForLevel(3));
		assertEquals(112_331, LevelCurve.xpForLevel(10));
	}

	@Test
	public void levelForXpAtBoundaries()
	{
		assertEquals(1, LevelCurve.levelForXp(0));
		assertEquals(1, LevelCurve.levelForXp(1_499));
		assertEquals(2, LevelCurve.levelForXp(1_500));
		assertEquals(9, LevelCurve.levelForXp(112_330));
		assertEquals(10, LevelCurve.levelForXp(112_331));
	}

	@Test
	public void levelStopsAtMax()
	{
		assertEquals(LevelCurve.MAX_LEVEL, LevelCurve.levelForXp(1_000_000_000L));
	}
}
