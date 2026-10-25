package com.dilzosrs.fellowship;

import java.awt.image.BufferedImage;
import javax.inject.Inject;
import javax.swing.SwingUtilities;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Skill;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.StatChanged;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;

@Slf4j
@PluginDescriptor(
	name = "Fellowship",
	description = "A side game where your OSRS XP levels up a team of adventurers",
	tags = {"minigame", "game", "xp", "adventure"}
)
public class FellowshipPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private ClientToolbar clientToolbar;

	private final XpTracker xpTracker = new XpTracker();
	private FellowshipPanel panel;
	private NavigationButton navButton;
	private Adventurer adventurer;

	@Override
	protected void startUp() throws Exception
	{
		panel = new FellowshipPanel();
		BufferedImage icon = ImageUtil.loadImageResource(getClass(), "icon.png");

		navButton = NavigationButton.builder()
			.tooltip("Fellowship")
			.icon(icon)
			.priority(10)
			.panel(panel)
			.build();

		clientToolbar.addNavigation(navButton);

		// Not saved yet, so progress resets on restart. Saving comes in step 1.5.
		adventurer = new Adventurer("Adventurer");
		updatePanel();

		xpTracker.reset();
		// If the plugin is turned on while already logged in, the game won't resend
		// the XP totals, so take the snapshot ourselves.
		clientThread.invokeLater(this::snapshotXpIfLoggedIn);

		log.debug("Fellowship started!");
	}

	@Override
	protected void shutDown() throws Exception
	{
		clientToolbar.removeNavigation(navButton);
		log.debug("Fellowship stopped!");
	}

	@Subscribe
	public void onStatChanged(StatChanged event)
	{
		int gained = xpTracker.update(event.getSkill(), event.getXp());
		if (gained > 0)
		{
			adventurer.addXp(gained);
			updatePanel();
		}
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		// Logged out: the next login (maybe on another account) starts a fresh snapshot.
		if (event.getGameState() == GameState.LOGIN_SCREEN)
		{
			xpTracker.reset();
		}
	}

	private void updatePanel()
	{
		// Copy the values now, because the panel is updated later on the Swing thread.
		String name = adventurer.getName();
		long xp = adventurer.getXp();
		SwingUtilities.invokeLater(() -> panel.showAdventurer(name, xp));
	}

	private void snapshotXpIfLoggedIn()
	{
		if (client.getGameState() != GameState.LOGGED_IN)
		{
			return;
		}
		for (Skill skill : Skill.values())
		{
			xpTracker.update(skill, client.getSkillExperience(skill));
		}
	}
}
