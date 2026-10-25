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
	// Never change these strings without a migration, or players lose their saved progress.
	private static final String CONFIG_GROUP = "fellowship";
	private static final String CHARACTER_XP_KEY = "adventurerXp";
	private static final String CHARACTER_CLASS_KEY = "characterClass";
	private static final String COINS_KEY = "coins";

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
	private FellowshipCharacter character;
	/** Coins belong to the account, not to one character. */
	private long coins;

	@Override
	protected void startUp() throws Exception
	{
		panel = new FellowshipPanel(this::evolve);
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
			loadCharacter();
			snapshotXpIfLoggedIn();
		});

		log.debug("Fellowship started!");
	}

	@Override
	protected void shutDown() throws Exception
	{
		clientToolbar.removeNavigation(navButton);
		character = null;
		log.debug("Fellowship stopped!");
	}

	@Subscribe
	public void onRuneScapeProfileChanged(RuneScapeProfileChanged event)
	{
		// Logged into an account (or a different one): load its saved character.
		loadCharacter();
	}

	@Subscribe
	public void onStatChanged(StatChanged event)
	{
		int gained = xpTracker.update(event.getSkill(), event.getXp());
		if (gained > 0 && character != null)
		{
			character.gainXp(event.getSkill(), gained);
			// Save on every gain, so progress is never lost. This only updates RuneLite's
			// settings in memory; RuneLite writes them to disk itself.
			configManager.setRSProfileConfiguration(CONFIG_GROUP, CHARACTER_XP_KEY, character.getXp());
			updatePanel();
		}
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		// Logged out: the next login (maybe on another account) starts a fresh snapshot,
		// and no XP is counted until that account's character is loaded.
		if (event.getGameState() == GameState.LOGIN_SCREEN)
		{
			xpTracker.reset();
			character = null;
		}
	}

	/**
	 * Called from the panel (on the Swing thread) once the player confirms a class.
	 */
	private void evolve(CharacterClass newClass)
	{
		// The character is only changed on the client thread, like everywhere else.
		clientThread.invokeLater(() ->
		{
			if (character == null || !character.evolve(newClass))
			{
				return;
			}
			configManager.setRSProfileConfiguration(CONFIG_GROUP, CHARACTER_CLASS_KEY, newClass.name());
			configManager.setRSProfileConfiguration(CONFIG_GROUP, CHARACTER_XP_KEY, character.getXp());
			updatePanel();
		});
	}

	private void loadCharacter()
	{
		if (configManager.getRSProfileKey() == null)
		{
			// Not logged in yet, so we don't know which account's progress to load.
			character = null;
			return;
		}

		Long savedXp = configManager.getRSProfileConfiguration(CONFIG_GROUP, CHARACTER_XP_KEY, Long.class);
		String savedClass = configManager.getRSProfileConfiguration(CONFIG_GROUP, CHARACTER_CLASS_KEY);
		character = new FellowshipCharacter(parseClass(savedClass), savedXp == null ? 0 : savedXp);

		Long savedCoins = configManager.getRSProfileConfiguration(CONFIG_GROUP, COINS_KEY, Long.class);
		coins = savedCoins == null ? 0 : savedCoins;

		updatePanel();
	}

	private static CharacterClass parseClass(String savedClass)
	{
		if (savedClass == null)
		{
			return CharacterClass.ADVENTURER;
		}
		try
		{
			return CharacterClass.valueOf(savedClass);
		}
		catch (IllegalArgumentException e)
		{
			log.warn("Unknown saved class {}, using Adventurer", savedClass);
			return CharacterClass.ADVENTURER;
		}
	}

	private void updatePanel()
	{
		// Copy the values now, because the panel is updated later on the Swing thread.
		CharacterClass characterClass = character.getCharacterClass();
		long xp = character.getXp();
		boolean canEvolve = character.canEvolve();
		long coinsNow = coins;
		SwingUtilities.invokeLater(() ->
		{
			panel.showCharacter(characterClass, xp, canEvolve);
			panel.showCoins(coinsNow);
		});
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
