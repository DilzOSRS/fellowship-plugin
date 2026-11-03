package com.dilzosrs.fellowship;

import java.util.regex.Pattern;

/**
 * Works out which bundle (if any) a game chat message can drop. The plugin only reads these
 * messages; it never sends anything to the game.
 */
public final class ChatLoot
{
	/** Sent when a clue scroll is finished, e.g. "You have completed 12 medium Treasure Trails." */
	private static final Pattern CLUE_DONE =
		Pattern.compile("^You have completed [\\d,]+ [a-z]+ Treasure Trails?\\.");

	/** Sent when a raid is finished, e.g. "Your completed Chambers of Xeric count is: 12." */
	private static final Pattern RAID_DONE = Pattern.compile("^Your completed .+ count is: [\\d,]+\\.");

	/** Sent when a boss dies, e.g. "Your Zulrah kill count is: 5." */
	private static final Pattern BOSS_KILL = Pattern.compile("^Your .+ kill count is: [\\d,]+\\.");

	private ChatLoot()
	{
	}

	/**
	 * @param message the chat message with colour tags already removed
	 * @return the bundle this message can drop, or null if it's not a loot trigger
	 */
	public static BundleType bundleFor(String message)
	{
		if (CLUE_DONE.matcher(message).find())
		{
			return BundleType.CLUE;
		}
		if (RAID_DONE.matcher(message).find())
		{
			return BundleType.RAID;
		}
		if (BOSS_KILL.matcher(message).find())
		{
			return BundleType.BOSS;
		}
		return null;
	}
}
