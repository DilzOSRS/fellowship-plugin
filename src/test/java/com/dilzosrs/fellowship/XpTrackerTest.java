package com.dilzosrs.fellowship;

import static org.junit.Assert.assertEquals;
import net.runelite.api.Skill;
import org.junit.Test;

public class XpTrackerTest
{
	@Test
	public void firstValueIsNotCounted()
	{
		XpTracker tracker = new XpTracker();
		assertEquals(0, tracker.update(Skill.WOODCUTTING, 500_000));
	}

	@Test
	public void gainsAfterFirstValueAreCounted()
	{
		XpTracker tracker = new XpTracker();
		tracker.update(Skill.WOODCUTTING, 500_000);
		assertEquals(25, tracker.update(Skill.WOODCUTTING, 500_025));
		assertEquals(100, tracker.update(Skill.WOODCUTTING, 500_125));
	}

	@Test
	public void skillsAreTrackedSeparately()
	{
		XpTracker tracker = new XpTracker();
		tracker.update(Skill.WOODCUTTING, 500_000);
		assertEquals(0, tracker.update(Skill.MINING, 1_000));
		assertEquals(40, tracker.update(Skill.MINING, 1_040));
	}

	@Test
	public void sameValueAgainGivesNothing()
	{
		XpTracker tracker = new XpTracker();
		tracker.update(Skill.ATTACK, 2_000);
		assertEquals(0, tracker.update(Skill.ATTACK, 2_000));
	}

	@Test
	public void resetTreatsNextValueAsSnapshot()
	{
		XpTracker tracker = new XpTracker();
		tracker.update(Skill.ATTACK, 2_000);
		tracker.reset();
		// e.g. logging into a different account with more XP
		assertEquals(0, tracker.update(Skill.ATTACK, 90_000));
	}
}
