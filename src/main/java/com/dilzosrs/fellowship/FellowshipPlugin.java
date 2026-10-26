package com.dilzosrs.fellowship;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import java.awt.image.BufferedImage;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
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
	private static final String TEAM_KEY = "team";
	private static final String COINS_KEY = "coins";

	// The old single-character save. Only read once, to move it into the team list.
	private static final String LEGACY_XP_KEY = "adventurerXp";
	private static final String LEGACY_CLASS_KEY = "characterClass";
	private static final String LEGACY_EXPEDITION_KEY = "expedition";
	private static final String LEGACY_EXPEDITION_START_KEY = "expeditionStart";

	@Inject
	private Client client;

	@Inject
	private ConfigManager configManager;

	@Inject
	private ClientThread clientThread;

	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private Gson gson;

	private final XpTracker xpTracker = new XpTracker();
	private final Random random = new Random();
	private FellowshipPanel panel;
	private NavigationButton navButton;
	/** The account's characters, or null when no account is loaded. For now there is only one. */
	private List<FellowshipCharacter> team;
	/** Coins belong to the account, not to one character. */
	private long coins;

	@Override
	protected void startUp() throws Exception
	{
		panel = new FellowshipPanel(this::evolve, this::sendOnExpedition, this::collectExpedition);
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
			loadTeam();
			snapshotXpIfLoggedIn();
		});

		log.debug("Fellowship started!");
	}

	@Override
	protected void shutDown() throws Exception
	{
		clientToolbar.removeNavigation(navButton);
		panel.stopTimers();
		team = null;
		log.debug("Fellowship stopped!");
	}

	@Subscribe
	public void onRuneScapeProfileChanged(RuneScapeProfileChanged event)
	{
		// Logged into an account (or a different one): load its saved team.
		loadTeam();
	}

	@Subscribe
	public void onStatChanged(StatChanged event)
	{
		int gained = xpTracker.update(event.getSkill(), event.getXp());
		FellowshipCharacter character = mainCharacter();
		if (gained > 0 && character != null)
		{
			character.gainXp(event.getSkill(), gained);
			// Save on every gain, so progress is never lost. This only updates RuneLite's
			// settings in memory; RuneLite writes them to disk itself.
			saveTeam();
			updatePanel();
		}
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		// Logged out: the next login (maybe on another account) starts a fresh snapshot,
		// and no XP is counted until that account's team is loaded.
		if (event.getGameState() == GameState.LOGIN_SCREEN)
		{
			xpTracker.reset();
			team = null;
		}
	}

	/**
	 * Called from the panel (on the Swing thread) once the player confirms a class.
	 */
	private void evolve(CharacterClass newClass)
	{
		// Characters are only changed on the client thread, like everywhere else.
		clientThread.invokeLater(() ->
		{
			FellowshipCharacter character = mainCharacter();
			if (character == null || !character.evolve(newClass))
			{
				return;
			}
			saveTeam();
			updatePanel();
		});
	}

	/**
	 * Called from the panel (on the Swing thread) once the player confirms sending the character away.
	 */
	private void sendOnExpedition(Expedition expedition)
	{
		clientThread.invokeLater(() ->
		{
			FellowshipCharacter character = mainCharacter();
			if (character == null || !character.startExpedition(expedition, Instant.now()))
			{
				return;
			}
			saveTeam();
			updatePanel();
		});
	}

	/**
	 * Called from the panel (on the Swing thread) when the player collects a finished expedition.
	 */
	private void collectExpedition()
	{
		clientThread.invokeLater(() ->
		{
			FellowshipCharacter character = mainCharacter();
			if (character == null)
			{
				return;
			}
			long xpBefore = character.getXp();
			Expedition finished = character.collectExpedition(Instant.now());
			if (finished == null)
			{
				return;
			}
			int coinsGained = finished.rollCoins(random);
			coins += coinsGained;

			saveTeam();
			configManager.setRSProfileConfiguration(CONFIG_GROUP, COINS_KEY, coins);

			long xpGained = character.getXp() - xpBefore;
			SwingUtilities.invokeLater(() -> panel.showReward(xpGained, coinsGained));
			updatePanel();
		});
	}

	/** The character shown in the panel, or null when no team is loaded. */
	private FellowshipCharacter mainCharacter()
	{
		return team == null ? null : team.get(0);
	}

	private void loadTeam()
	{
		team = null;
		if (configManager.getRSProfileKey() == null)
		{
			// Not logged in yet, so we don't know which account's progress to load.
			return;
		}

		String json = configManager.getRSProfileConfiguration(CONFIG_GROUP, TEAM_KEY);
		if (json == null)
		{
			team = moveOldSaveIntoTeam();
		}
		else
		{
			SavedCharacter[] saved;
			try
			{
				saved = gson.fromJson(json, SavedCharacter[].class);
			}
			catch (JsonSyntaxException e)
			{
				// Leave the team unloaded, so the broken save is never overwritten with a fresh one.
				log.warn("Couldn't read the saved Fellowship team", e);
				return;
			}
			List<FellowshipCharacter> loaded = new ArrayList<>();
			if (saved != null)
			{
				for (SavedCharacter savedCharacter : saved)
				{
					loaded.add(savedCharacter.toCharacter());
				}
			}
			if (loaded.isEmpty())
			{
				loaded.add(new FellowshipCharacter(CharacterClass.ADVENTURER, 0));
			}
			team = loaded;
		}

		Long savedCoins = configManager.getRSProfileConfiguration(CONFIG_GROUP, COINS_KEY, Long.class);
		coins = savedCoins == null ? 0 : savedCoins;

		updatePanel();
	}

	/**
	 * Build a one-character team from the old single-character save (or a fresh Adventurer if
	 * there isn't one), save it in the new format, then remove the old keys.
	 */
	private List<FellowshipCharacter> moveOldSaveIntoTeam()
	{
		SavedCharacter old = new SavedCharacter();
		old.characterClass = configManager.getRSProfileConfiguration(CONFIG_GROUP, LEGACY_CLASS_KEY);
		Long oldXp = configManager.getRSProfileConfiguration(CONFIG_GROUP, LEGACY_XP_KEY, Long.class);
		old.xp = oldXp == null ? 0 : oldXp;
		old.expedition = configManager.getRSProfileConfiguration(CONFIG_GROUP, LEGACY_EXPEDITION_KEY);
		old.expeditionStart = configManager.getRSProfileConfiguration(CONFIG_GROUP, LEGACY_EXPEDITION_START_KEY, Long.class);

		List<FellowshipCharacter> moved = new ArrayList<>();
		moved.add(old.toCharacter());
		team = moved;

		// Save the new format first, so progress is never lost if something goes wrong in between.
		saveTeam();
		configManager.unsetRSProfileConfiguration(CONFIG_GROUP, LEGACY_XP_KEY);
		configManager.unsetRSProfileConfiguration(CONFIG_GROUP, LEGACY_CLASS_KEY);
		configManager.unsetRSProfileConfiguration(CONFIG_GROUP, LEGACY_EXPEDITION_KEY);
		configManager.unsetRSProfileConfiguration(CONFIG_GROUP, LEGACY_EXPEDITION_START_KEY);
		log.debug("Moved the old Fellowship save into the team format");
		return moved;
	}

	private void saveTeam()
	{
		SavedCharacter[] saved = new SavedCharacter[team.size()];
		for (int i = 0; i < team.size(); i++)
		{
			saved[i] = SavedCharacter.from(team.get(i));
		}
		configManager.setRSProfileConfiguration(CONFIG_GROUP, TEAM_KEY, gson.toJson(saved));
	}

	private void updatePanel()
	{
		// Copy the values now, because the panel is updated later on the Swing thread.
		FellowshipCharacter character = mainCharacter();
		CharacterClass characterClass = character.getCharacterClass();
		long xp = character.getXp();
		boolean canEvolve = character.canEvolve();
		Expedition expedition = character.getExpedition();
		Instant expeditionEnd = character.getExpeditionEnd();
		long coinsNow = coins;
		SwingUtilities.invokeLater(() ->
		{
			panel.showCharacter(characterClass, xp, canEvolve, expedition, expeditionEnd);
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
