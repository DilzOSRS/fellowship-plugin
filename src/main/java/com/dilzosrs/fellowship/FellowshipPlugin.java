package com.dilzosrs.fellowship;

import lombok.extern.slf4j.Slf4j;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

@Slf4j
@PluginDescriptor(
	name = "Fellowship",
	description = "A side game where your OSRS XP levels up a team of adventurers",
	tags = {"minigame", "game", "xp", "adventure"}
)
public class FellowshipPlugin extends Plugin
{
	@Override
	protected void startUp() throws Exception
	{
		log.debug("Fellowship started!");
	}

	@Override
	protected void shutDown() throws Exception
	{
		log.debug("Fellowship stopped!");
	}
}
