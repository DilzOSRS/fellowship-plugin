package com.dilzosrs.fellowship;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class LevelCurveTest
{
	@Test
	public void xpForLevelMatchesTheChosenCurve()
	{
		assertEquals(0, LevelCurve.xpForLevel(1));
		assertEquals(6_000, LevelCurve.xpForLevel(2));
		assertEquals(13_500, LevelCurve.xpForLevel(3));
		assertEquals(154_814, LevelCurve.xpForLevel(10));
	}

	@Test
	public void levelForXpAtBoundaries()
	{
		assertEquals(1, LevelCurve.levelForXp(0));
		assertEquals(1, LevelCurve.levelForXp(5_999));
		assertEquals(2, LevelCurve.levelForXp(6_000));
		assertEquals(9, LevelCurve.levelForXp(154_813));
		assertEquals(10, LevelCurve.levelForXp(154_814));
	}

	@Test
	public void levelStopsAtMax()
	{
		assertEquals(LevelCurve.MAX_LEVEL, LevelCurve.levelForXp(1_000_000_000L));
	}

	@Test
	public void progressThroughALevel()
	{
		assertEquals(0.0, LevelCurve.progressToNextLevel(0), 0.0001);
		assertEquals(0.5, LevelCurve.progressToNextLevel(3_000), 0.0001);
		assertEquals(0.0, LevelCurve.progressToNextLevel(6_000), 0.0001);
		assertEquals(0.5, LevelCurve.progressToNextLevel(6_000 + 3_750), 0.0001);
	}

	@Test
	public void progressIsFullAtMaxLevel()
	{
		assertEquals(1.0, LevelCurve.progressToNextLevel(154_814), 0.0001);
		assertEquals(1.0, LevelCurve.progressToNextLevel(9_999_999), 0.0001);
	}
}
