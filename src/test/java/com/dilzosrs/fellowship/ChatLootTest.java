package com.dilzosrs.fellowship;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.junit.Test;

public class ChatLootTest
{
	@Test
	public void clueCompletions()
	{
		assertEquals(BundleType.CLUE, ChatLoot.bundleFor("You have completed 12 medium Treasure Trails."));
		assertEquals(BundleType.CLUE, ChatLoot.bundleFor("You have completed 1 beginner Treasure Trail."));
		assertEquals(BundleType.CLUE, ChatLoot.bundleFor("You have completed 1,050 elite Treasure Trails."));
	}

	@Test
	public void raidCompletions()
	{
		assertEquals(BundleType.RAID, ChatLoot.bundleFor("Your completed Chambers of Xeric count is: 12."));
		assertEquals(BundleType.RAID, ChatLoot.bundleFor("Your completed Theatre of Blood count is: 1,204."));
		assertEquals(BundleType.RAID, ChatLoot.bundleFor("Your completed Tombs of Amascut: Expert Mode count is: 3."));
	}

	@Test
	public void bossKills()
	{
		assertEquals(BundleType.BOSS, ChatLoot.bundleFor("Your Zulrah kill count is: 5."));
		assertEquals(BundleType.BOSS, ChatLoot.bundleFor("Your Grotesque Guardians kill count is: 1,050."));
	}

	@Test
	public void otherMessagesDropNothing()
	{
		// Barrows gets its own bundle later.
		assertNull(ChatLoot.bundleFor("Your Barrows chest count is: 40."));
		assertNull(ChatLoot.bundleFor("Player123: Your Zulrah kill count is: 5."));
		assertNull(ChatLoot.bundleFor("Congratulations, you've just advanced your Slayer level. You are now level 80."));
		assertNull(ChatLoot.bundleFor("You're assigned to kill abyssal demons; only 120 more to go."));
		// Slayer Bundles come from task kills now (SlayerLoot), not chat.
		assertNull(ChatLoot.bundleFor(
			"You've completed 45 tasks and received 15 points, giving you a total of 300; return to a Slayer master."));
		assertNull(ChatLoot.bundleFor("You have completed the Cook's Assistant quest!"));
		// The plugin's own messages must never trigger loot.
		assertNull(ChatLoot.bundleFor("[Fellowship] Knight reached level 5!"));
		assertNull(ChatLoot.bundleFor("Player123: You've completed 45 tasks lol"));
	}
}
