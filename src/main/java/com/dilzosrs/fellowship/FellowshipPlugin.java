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
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.RuneScapeProfileChanged;
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
	// Never rename these without a migration, or players lose their saved progress.
	private static final String CONFIG_GROUP = "fellowship";
	private static final String ADVENTURER_XP_KEY = "adventurerXp";

	@Inject
	private Client client;

	@Inject
	private ConfigManager configManager;

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

		xpTracker.reset();
		// If the plugin is turned on while already logged in, the account is already known
		// and the game won't resend the XP totals, so load and snapshot ourselves.
		clientThread.invokeLater(() ->
		{
			loadAdventurer();
			snapshotXpIfLoggedIn();
		});

		log.debug("Fellowship started!");
	}

	@Override
	protected void shutDown() throws Exception
	{
		clientToolbar.removeNavigation(navButton);
		adventurer = null;
		log.debug("Fellowship stopped!");
	}

	@Subscribe
	public void onRuneScapeProfileChanged(RuneScapeProfileChanged event)
	{
		// Logged into an account (or a different one): load its saved Adventurer.
		loadAdventurer();
	}

	@Subscribe
	public void onStatChanged(StatChanged event)
	{
		int gained = xpTracker.update(event.getSkill(), event.getXp());
		if (gained > 0 && adventurer != null)
		{
			adventurer.addXp(gained);
			// Save on every gain, so progress is never lost. This only updates RuneLite's
			// settings in memory; RuneLite writes them to disk itself.
			configManager.setRSProfileConfiguration(CONFIG_GROUP, ADVENTURER_XP_KEY, adventurer.getXp());
			updatePanel();
		}
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		// Logged out: the next login (maybe on another account) starts a fresh snapshot,
		// and no XP is counted until that account's Adventurer is loaded.
		if (event.getGameState() == GameState.LOGIN_SCREEN)
		{
			xpTracker.reset();
			adventurer = null;
		}
	}

	private void loadAdventurer()
	{
		if (configManager.getRSProfileKey() == null)
		{
			// Not logged in yet, so we don't know which account's progress to load.
			adventurer = null;
			return;
		}

		Long savedXp = configManager.getRSProfileConfiguration(CONFIG_GROUP, ADVENTURER_XP_KEY, Long.class);
		adventurer = new Adventurer("Adventurer", savedXp == null ? 0 : savedXp);
		updatePanel();
	}

	private void updatePanel()
	{
		// Copy the values now, because the panel is updated later on the Swing thread.
		String name = adventurer.getName();
		long xp = adventurer.getXp();
		int levelCap = adventurer.getLevelCap();
		SwingUtilities.invokeLater(() -> panel.showAdventurer(name, xp, levelCap));
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
