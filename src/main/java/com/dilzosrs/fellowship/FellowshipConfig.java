package com.dilzosrs.fellowship;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

/**
 * The plugin's settings, under the gear icon in RuneLite's plugin list.
 */
@ConfigGroup(FellowshipPlugin.CONFIG_GROUP)
public interface FellowshipConfig extends Config
{
	String RESET_KEY = "resetProgress";

	/**
	 * Ticking this wipes the logged-in account's Fellowship and starts it again. The plugin unticks
	 * it straight away, so it works like a button.
	 */
	@ConfigItem(
		keyName = RESET_KEY,
		name = "Start a new game",
		description = "Wipes the Fellowship of the account you're logged into (followers, coins, Guild, bundles"
			+ " and gear) and starts again with one Adventurer. Your real OSRS account isn't touched.",
		warning = "This wipes all Fellowship progress on the account you're logged into and can't be undone."
			+ " Start a new game?"
	)
	default boolean resetProgress()
	{
		return false;
	}
}
