package com.dilzosrs.fellowship;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class LevelCurveTest
{
	@Test
	public void earlyLevelsMatchTheChosenCurve()
	{
		assertEquals(0, LevelCurve.xpForLevel(1));
		assertEquals(1_500, LevelCurve.xpForLevel(2));
		assertEquals(3_750, LevelCurve.xpForLevel(3));
		assertEquals(112_331, LevelCurve.xpForLevel(10));
	}

	@Test
	public void levelsAfterTenGrowGently()
	{
		// Level 10 cost 38,443 XP; level 11 costs 5% more (40,366).
		assertEquals(152_697, LevelCurve.xpForLevel(11));
		assertEquals(620_045, LevelCurve.xpForLevel(20));
		assertEquals(61_375_954, LevelCurve.xpForLevel(99));
	}

	@Test
	public void levelForXpAtBoundaries()
	{
		assertEquals(1, LevelCurve.levelForXp(0));
		assertEquals(1, LevelCurve.levelForXp(1_499));
		assertEquals(2, LevelCurve.levelForXp(1_500));
		assertEquals(9, LevelCurve.levelForXp(112_330));
		assertEquals(10, LevelCurve.levelForXp(112_331));
		assertEquals(10, LevelCurve.levelForXp(152_696));
		assertEquals(11, LevelCurve.levelForXp(152_697));
	}

	@Test
	public void levelStopsAtMax()
	{
		assertEquals(99, LevelCurve.levelForXp(61_375_954));
		assertEquals(LevelCurve.MAX_LEVEL, LevelCurve.levelForXp(Long.MAX_VALUE));
	}
}
