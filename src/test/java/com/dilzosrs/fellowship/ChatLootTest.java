package com.dilzosrs.fellowship;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.junit.Test;

public class ChatLootTest
{
	@Test
	public void slayerTaskCompletions()
	{
		assertEquals(BundleType.SLAYER, ChatLoot.bundleFor(
			"You've completed 45 tasks and received 15 points, giving you a total of 300; return to a Slayer master."));
		assertEquals(BundleType.SLAYER, ChatLoot.bundleFor(
			"You've completed 1,234 tasks and received 25 points, giving you a total of 2,050; return to a Slayer master."));
		assertEquals(BundleType.SLAYER, ChatLoot.bundleFor("You've completed 1 task; return to a Slayer master."));
		assertEquals(BundleType.SLAYER, ChatLoot.bundleFor(
			"You've completed 12 Wilderness tasks and received 25 points, giving you a total of 400; return to a Slayer master."));
		assertEquals(BundleType.SLAYER, ChatLoot.bundleFor(
			"You've completed at least 5 tasks; return to a Slayer master."));
	}

	@Test
	public void clueCompletions()
	{
		assertEquals(BundleType.CLUE, ChatLoot.bundleFor("You have completed 12 medium Treasure Trails."));
		assertEquals(BundleType.CLUE, ChatLoot.bundleFor("You have completed 1 beginner Treasure Trail."));
		assertEquals(BundleType.CLUE, ChatLoot.bundleFor("You have completed 1,050 elite Treasure Trails."));
	}

	@Test
	public void otherMessagesDropNothing()
	{
		assertNull(ChatLoot.bundleFor("Your Zulrah kill count is: 5."));
		assertNull(ChatLoot.bundleFor("Congratulations, you've just advanced your Slayer level. You are now level 80."));
		assertNull(ChatLoot.bundleFor("You're assigned to kill abyssal demons; only 120 more to go."));
		assertNull(ChatLoot.bundleFor("You have completed the Cook's Assistant quest!"));
		// The plugin's own messages must never trigger loot.
		assertNull(ChatLoot.bundleFor("[Fellowship] Knight reached level 5!"));
		assertNull(ChatLoot.bundleFor("Player123: You've completed 45 tasks lol"));
	}
}
