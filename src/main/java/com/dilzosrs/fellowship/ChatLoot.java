package com.dilzosrs.fellowship;

import java.util.regex.Pattern;

/**
 * Works out which bundle (if any) a game chat message can drop. The plugin only reads these
 * messages; it never sends anything to the game.
 */
public final class ChatLoot
{
	/**
	 * Sent when a Slayer task is finished, e.g. "You've completed 45 tasks and received 15 points,
	 * giving you a total of 300; return to a Slayer master." Wilderness tasks too.
	 */
	private static final Pattern SLAYER_TASK_DONE =
		Pattern.compile("^You've completed (?:at least )?[\\d,]+ (?:Wilderness )?tasks?\\b");

	/** Sent when a clue scroll is finished, e.g. "You have completed 12 medium Treasure Trails." */
	private static final Pattern CLUE_DONE =
		Pattern.compile("^You have completed [\\d,]+ [a-z]+ Treasure Trails?\\.");

	private ChatLoot()
	{
	}

	/**
	 * @param message the chat message with colour tags already removed
	 * @return the bundle this message can drop, or null if it's not a loot trigger
	 */
	public static BundleType bundleFor(String message)
	{
		if (SLAYER_TASK_DONE.matcher(message).find())
		{
			return BundleType.SLAYER;
		}
		if (CLUE_DONE.matcher(message).find())
		{
			return BundleType.CLUE;
		}
		return null;
	}
}
