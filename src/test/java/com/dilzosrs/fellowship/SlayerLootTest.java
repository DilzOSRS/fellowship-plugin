package com.dilzosrs.fellowship;

import static org.junit.Assert.assertEquals;
import java.util.Random;
import org.junit.Test;

public class SlayerLootTest
{
	/** A Random that always returns the same number, to make rolls predictable in tests. */
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

	@Test
	public void theFinalKillAlwaysGivesOne()
	{
		assertEquals(1, SlayerLoot.bundlesFor(1, 0, always(1)));
	}

	@Test
	public void eachKillHasASmallChance()
	{
		assertEquals(0, SlayerLoot.bundlesFor(50, 49, always(1)));
		assertEquals(1, SlayerLoot.bundlesFor(50, 49, always(0)));
		// Two kills at once roll twice, and the final kill can add a roll on top.
		assertEquals(2, SlayerLoot.bundlesFor(50, 48, always(0)));
		assertEquals(2, SlayerLoot.bundlesFor(1, 0, always(0)));
	}

	@Test
	public void newCancelledOrUnchangedTasksGiveNothing()
	{
		// A new task makes the count go up.
		assertEquals(0, SlayerLoot.bundlesFor(0, 120, always(0)));
		// Cancelling or skipping at a Slayer master drops it straight to 0.
		assertEquals(0, SlayerLoot.bundlesFor(80, 0, always(0)));
		// Checking the task changes nothing.
		assertEquals(0, SlayerLoot.bundlesFor(0, 0, always(0)));
	}
}
