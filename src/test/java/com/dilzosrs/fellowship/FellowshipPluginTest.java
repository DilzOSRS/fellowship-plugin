package com.dilzosrs.fellowship;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class FellowshipPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(FellowshipPlugin.class);
		RuneLite.main(args);
	}
}
