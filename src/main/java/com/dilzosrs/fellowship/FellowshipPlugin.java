package com.dilzosrs.fellowship;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import javax.inject.Inject;
import javax.swing.SwingUtilities;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Skill;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.StatChanged;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.chat.ChatColorType;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.RuneScapeProfileChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.util.Text;

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
	private static final String ACTIVE_INDEX_KEY = "activeIndex";
	private static final String GUILD_XP_KEY = "guildXp";
	private static final String HIRES_USED_KEY = "hiresUsed";
	private static final String VAULT_KEY = "vault";

	private static final Color CHAT_TAG_COLOR = new Color(255, 200, 0);

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

	@Inject
	private ChatMessageManager chatMessageManager;

	private final XpTracker xpTracker = new XpTracker();
	private final Random random = new Random();
	private FellowshipPanel panel;
	private VaultPanel vaultPanel;
	private NavigationButton navButton;
	/** The account's characters, or null when no account is loaded. */
	private Team team;
	/** Coins belong to the account, not to one character. */
	private long coins;
	/** The account's Fellowship Guild, loaded with the team. */
	private Guild guild;
	/** The account's unopened bundles and gear storage, loaded with the team. */
	private Vault vault;
	/** Characters whose finished expedition has already been announced in chat, so it's said only once. */
	private final Set<FellowshipCharacter> announcedReturns = new HashSet<>();

	@Override
	protected void startUp() throws Exception
	{
		vaultPanel = new VaultPanel(this::openBundle, this::salvage);
		panel = new FellowshipPanel(this::evolve, this::sendOnExpedition, this::collectExpedition, this::makeActive,
			this::hire, this::equip, this::unequip, vaultPanel);
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
		if (gained > 0 && team != null)
		{
			int[] levelsBefore = currentLevels();
			int levelsGained = team.gainXp(event.getSkill(), gained);
			if (levelsGained > 0)
			{
				announceLevelUps(levelsBefore);
				// Every character level-up is worth 1 Guild XP.
				addGuildXp(levelsGained);
			}
			// Every 100,000 OSRS XP (all skills together) gives a chance of a Generic Bundle.
			int bundlesFound = vault.addXpForGenericBundles(gained, random);
			for (int i = 0; i < bundlesFound; i++)
			{
				sendChatMessage("You found a " + BundleType.GENERIC.getDisplayName() + "! Open it in the Fellowship panel.");
			}
			// Save on every gain, so progress is never lost. This only updates RuneLite's
			// settings in memory; RuneLite writes them to disk itself.
			saveTeam();
			saveVault();
			updatePanel();
		}
	}

	@Subscribe
	public void onChatMessage(ChatMessage event)
	{
		// Only the game's own messages count, so other players can't trigger loot by typing them.
		if (vault == null || team == null || event.getType() != ChatMessageType.GAMEMESSAGE)
		{
			return;
		}
		BundleType bundle = ChatLoot.bundleFor(Text.removeTags(event.getMessage()));
		if (bundle == null || !bundle.rollDrop(random))
		{
			return;
		}
		vault.addBundle(bundle);
		saveVault();
		sendChatMessage("You found a " + bundle.getDisplayName() + "! Open it in the Fellowship panel.");
		updatePanel();
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		// Announce each finished expedition once. The team is small, so checking every tick is cheap.
		if (team == null)
		{
			return;
		}
		Instant now = Instant.now();
		for (int i = 0; i < team.size(); i++)
		{
			FellowshipCharacter character = team.get(i);
			if (character.isExpeditionDone(now) && !announcedReturns.contains(character))
			{
				// Announce the whole party in one message.
				List<Integer> party = team.partyOf(i);
				for (int member : party)
				{
					announcedReturns.add(team.get(member));
				}
				sendChatMessage(joinNames(party) + (party.size() == 1 ? " is" : " are") + " back from "
					+ character.getExpedition().getDisplayName() + "! Collect the rewards in the Fellowship panel.");
			}
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
	private void evolve(int index, CharacterClass newClass)
	{
		// Characters are only changed on the client thread, like everywhere else.
		clientThread.invokeLater(() ->
		{
			FellowshipCharacter character = characterAt(index);
			if (character == null)
			{
				return;
			}
			String oldName = team.getDisplayName(index);
			if (!character.evolve(newClass))
			{
				return;
			}
			sendChatMessage(oldName + " has been trained in the ways of the " + newClass.getDisplayName() + "!");
			saveTeam();
			updatePanel();
		});
	}

	/**
	 * Called from the panel (on the Swing thread) once the player confirms sending one character
	 * or a whole party away.
	 */
	private void sendOnExpedition(List<Integer> party, Expedition expedition)
	{
		clientThread.invokeLater(() ->
		{
			if (team == null || !team.sendParty(party, expedition, Instant.now()))
			{
				return;
			}
			saveTeam();
			updatePanel();
		});
	}

	/**
	 * Called from the panel (on the Swing thread) when the player collects a finished expedition.
	 * The whole party comes home: XP for each member, coins and the bonus bundle rolled once.
	 */
	private void collectExpedition(int index)
	{
		clientThread.invokeLater(() ->
		{
			FellowshipCharacter character = characterAt(index);
			if (character == null || !character.isExpeditionDone(Instant.now()))
			{
				return;
			}
			Expedition finished = character.getExpedition();
			int[] levelsBefore = currentLevels();
			long[] xpBefore = new long[team.size()];
			for (int i = 0; i < team.size(); i++)
			{
				xpBefore[i] = team.get(i).getXp();
			}

			// Dangerous expeditions can fail: the party's average ATK against the threat level.
			boolean success = Danger.rollSuccess(finished.getThreatLevel(), team.averageAtk(team.partyOf(index)), random);
			List<Integer> party = team.collectParty(index, Instant.now(), success);
			if (party.isEmpty())
			{
				return;
			}
			if (!success)
			{
				sendChatMessage(joinNames(party) + (party.size() == 1 ? " was" : " were") + " overwhelmed in "
					+ finished.getDisplayName() + " and came back empty-handed.");
				for (int member : party)
				{
					announcedReturns.remove(team.get(member));
					SwingUtilities.invokeLater(() -> panel.showFailure(member, finished));
				}
				saveTeam();
				updatePanel();
				return;
			}
			int coinsGained = finished.rollCoins(random);
			coins += coinsGained;
			BundleType bonus = finished.rollBonusBundle(random);
			if (bonus != null)
			{
				vault.addBundle(bonus);
				saveVault();
			}

			long xpEach = finished.getXpReward();
			sendChatMessage(joinNames(party) + " brought back "
				+ String.format("%,d XP", xpEach) + (party.size() > 1 ? " each" : "") + " and "
				+ coinsGained + " coins from " + finished.getDisplayName() + "!"
				+ (bonus != null ? " They also found a " + bonus.getDisplayName() + "!" : ""));
			announceLevelUps(levelsBefore);

			int levelsGained = 0;
			for (int member : party)
			{
				announcedReturns.remove(team.get(member));
				levelsGained += team.get(member).getLevel() - levelsBefore[member];
				long gained = team.get(member).getXp() - xpBefore[member];
				SwingUtilities.invokeLater(() -> panel.showReward(member, gained, coinsGained));
			}
			// 1 Guild XP for the expedition, plus 1 for each level its XP reward gave.
			addGuildXp(1 + levelsGained);

			saveTeam();
			saveCoins();
			updatePanel();
		});
	}

	/** e.g. "Knight", "Knight and Archer 2", or "Knight, Archer 2 and Wizard 3". */
	private String joinNames(List<Integer> indexes)
	{
		StringBuilder names = new StringBuilder();
		for (int i = 0; i < indexes.size(); i++)
		{
			if (i > 0)
			{
				names.append(i == indexes.size() - 1 ? " and " : ", ");
			}
			names.append(team.getDisplayName(indexes.get(i)));
		}
		return names.toString();
	}

	/**
	 * Called from the panel (on the Swing thread) once the player confirms hiring.
	 */
	private void hire()
	{
		clientThread.invokeLater(() ->
		{
			if (team == null || !guild.canHire(coins) || !guild.useHire())
			{
				return;
			}
			coins -= Guild.HIRE_COST;
			team.hire();
			sendChatMessage(team.getDisplayName(team.size() - 1) + " joined your Fellowship!");
			saveTeam();
			saveCoins();
			saveGuild();
			updatePanel();
		});
	}

	/**
	 * Called from the panel (on the Swing thread) when the player makes a character active.
	 */
	private void makeActive(int index)
	{
		clientThread.invokeLater(() ->
		{
			if (team == null || !team.setActive(index))
			{
				return;
			}
			configManager.setRSProfileConfiguration(CONFIG_GROUP, ACTIVE_INDEX_KEY, index);
			updatePanel();
		});
	}

	/**
	 * Called from the panel (on the Swing thread) when the player opens a bundle.
	 */
	private void openBundle(BundleType bundle)
	{
		clientThread.invokeLater(() ->
		{
			if (vault == null)
			{
				return;
			}
			Vault.Opened opened = vault.open(bundle, random);
			if (opened == null)
			{
				return;
			}
			coins += opened.coins;
			saveVault();
			saveCoins();
			sendChatMessage("You opened a " + bundle.getDisplayName() + ": " + opened.item.getName()
				+ " (" + opened.item.getDescription() + ") and " + opened.coins + " coins!");
			SwingUtilities.invokeLater(() -> vaultPanel.showOpened(bundle, opened.item, opened.coins));
			updatePanel();
		});
	}

	/**
	 * Called from the panel (on the Swing thread) once the player confirms salvaging an item.
	 */
	private void salvage(int index)
	{
		clientThread.invokeLater(() ->
		{
			if (vault == null || index >= vault.getItems().size())
			{
				return;
			}
			GearItem item = vault.getItems().get(index);
			int coinsGained = vault.salvage(index);
			coins += coinsGained;
			saveVault();
			saveCoins();
			sendChatMessage("You salvaged the " + item.getName() + " for " + coinsGained + " coins.");
			updatePanel();
		});
	}

	/**
	 * Called from the panel (on the Swing thread) when the player picks an item from storage
	 * for a character. Whatever was in that slot goes back to storage.
	 */
	private void equip(int characterIndex, int storageIndex)
	{
		clientThread.invokeLater(() ->
		{
			FellowshipCharacter character = characterAt(characterIndex);
			if (character == null || vault == null || storageIndex >= vault.getItems().size()
				|| !character.canChangeGear() || !character.canEquip(vault.getItems().get(storageIndex)))
			{
				return;
			}
			GearItem item = vault.takeItem(storageIndex);
			GearItem old = character.equip(item);
			if (old != null)
			{
				// Always fits: the new item just freed a storage slot.
				vault.storeItem(old);
			}
			saveTeam();
			saveVault();
			updatePanel();
		});
	}

	/**
	 * Called from the panel (on the Swing thread) when the player takes off a character's gear.
	 */
	private void unequip(int characterIndex, GearType.Slot slot)
	{
		clientThread.invokeLater(() ->
		{
			FellowshipCharacter character = characterAt(characterIndex);
			if (character == null || vault == null || vault.isFull() || !character.canChangeGear()
				|| character.getEquipped(slot) == null)
			{
				return;
			}
			vault.storeItem(character.unequip(slot));
			saveTeam();
			saveVault();
			updatePanel();
		});
	}

	/** Each character's level right now, to compare against after XP is added. */
	private int[] currentLevels()
	{
		int[] levels = new int[team.size()];
		for (int i = 0; i < team.size(); i++)
		{
			levels[i] = team.get(i).getLevel();
		}
		return levels;
	}

	private void announceLevelUps(int[] levelsBefore)
	{
		for (int i = 0; i < levelsBefore.length; i++)
		{
			FellowshipCharacter character = team.get(i);
			if (character.getLevel() > levelsBefore[i])
			{
				String name = team.getDisplayName(i);
				sendChatMessage(name + " reached level " + character.getLevel() + "!"
					+ (character.canEvolve() ? " " + name + "'s class advancement is ready." : ""));
			}
		}
	}

	private void addGuildXp(long amount)
	{
		int levelBefore = guild.getLevel();
		guild.addXp(amount);
		saveGuild();
		if (guild.getLevel() > levelBefore)
		{
			sendChatMessage("Your Fellowship Guild reached level " + guild.getLevel() + "! You earned a hire.");
		}
	}

	/**
	 * Add a message to the player's own chatbox. Only they see it; nothing is sent to the server.
	 */
	private void sendChatMessage(String message)
	{
		// The tag is gold so Fellowship messages stand out; the rest uses the chatbox's normal
		// colour, so it stays readable on both the transparent and the solid chatbox.
		String formatted = new ChatMessageBuilder()
			.append(CHAT_TAG_COLOR, "[Fellowship] ")
			.append(ChatColorType.NORMAL)
			.append(message)
			.build();
		chatMessageManager.queue(QueuedMessage.builder()
			.type(ChatMessageType.GAMEMESSAGE)
			.runeLiteFormattedMessage(formatted)
			.build());
	}

	/** The character at that position in the team, or null if there isn't one. */
	private FellowshipCharacter characterAt(int index)
	{
		return team == null || index >= team.size() ? null : team.get(index);
	}

	private void loadTeam()
	{
		team = null;
		announcedReturns.clear();
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
			Integer savedActive = configManager.getRSProfileConfiguration(CONFIG_GROUP, ACTIVE_INDEX_KEY, Integer.class);
			team = new Team(loaded, savedActive == null ? 0 : savedActive);
		}

		Long savedCoins = configManager.getRSProfileConfiguration(CONFIG_GROUP, COINS_KEY, Long.class);
		coins = savedCoins == null ? 0 : savedCoins;

		Long savedGuildXp = configManager.getRSProfileConfiguration(CONFIG_GROUP, GUILD_XP_KEY, Long.class);
		Integer savedHiresUsed = configManager.getRSProfileConfiguration(CONFIG_GROUP, HIRES_USED_KEY, Integer.class);
		guild = new Guild(savedGuildXp == null ? 0 : savedGuildXp, savedHiresUsed == null ? 0 : savedHiresUsed);

		String vaultJson = configManager.getRSProfileConfiguration(CONFIG_GROUP, VAULT_KEY);
		vault = Vault.empty();
		if (vaultJson != null)
		{
			try
			{
				SavedVault savedVault = gson.fromJson(vaultJson, SavedVault.class);
				if (savedVault != null)
				{
					vault = savedVault.toVault();
				}
			}
			catch (JsonSyntaxException e)
			{
				// Leave the whole account unloaded, so the broken save is never overwritten.
				log.warn("Couldn't read the saved Fellowship storage", e);
				team = null;
				return;
			}
		}

		updatePanel();
	}

	/**
	 * Build a one-character team from the old single-character save (or a fresh Adventurer if
	 * there isn't one), save it in the new format, then remove the old keys.
	 */
	private Team moveOldSaveIntoTeam()
	{
		SavedCharacter old = new SavedCharacter();
		old.characterClass = configManager.getRSProfileConfiguration(CONFIG_GROUP, LEGACY_CLASS_KEY);
		Long oldXp = configManager.getRSProfileConfiguration(CONFIG_GROUP, LEGACY_XP_KEY, Long.class);
		old.xp = oldXp == null ? 0 : oldXp;
		old.expedition = configManager.getRSProfileConfiguration(CONFIG_GROUP, LEGACY_EXPEDITION_KEY);
		old.expeditionStart = configManager.getRSProfileConfiguration(CONFIG_GROUP, LEGACY_EXPEDITION_START_KEY, Long.class);

		List<FellowshipCharacter> moved = new ArrayList<>();
		moved.add(old.toCharacter());
		team = new Team(moved);

		// Save the new format first, so progress is never lost if something goes wrong in between.
		saveTeam();
		configManager.unsetRSProfileConfiguration(CONFIG_GROUP, LEGACY_XP_KEY);
		configManager.unsetRSProfileConfiguration(CONFIG_GROUP, LEGACY_CLASS_KEY);
		configManager.unsetRSProfileConfiguration(CONFIG_GROUP, LEGACY_EXPEDITION_KEY);
		configManager.unsetRSProfileConfiguration(CONFIG_GROUP, LEGACY_EXPEDITION_START_KEY);
		log.debug("Moved the old Fellowship save into the team format");
		return team;
	}

	private void saveTeam()
	{
		List<FellowshipCharacter> characters = team.getCharacters();
		SavedCharacter[] saved = new SavedCharacter[characters.size()];
		for (int i = 0; i < characters.size(); i++)
		{
			saved[i] = SavedCharacter.from(characters.get(i));
		}
		configManager.setRSProfileConfiguration(CONFIG_GROUP, TEAM_KEY, gson.toJson(saved));
	}

	private void saveCoins()
	{
		configManager.setRSProfileConfiguration(CONFIG_GROUP, COINS_KEY, coins);
	}

	private void saveVault()
	{
		configManager.setRSProfileConfiguration(CONFIG_GROUP, VAULT_KEY, gson.toJson(SavedVault.from(vault)));
	}

	private void saveGuild()
	{
		configManager.setRSProfileConfiguration(CONFIG_GROUP, GUILD_XP_KEY, guild.getXp());
		configManager.setRSProfileConfiguration(CONFIG_GROUP, HIRES_USED_KEY, guild.getHiresUsed());
	}

	private void updatePanel()
	{
		// Copy the values now, because the panel is updated later on the Swing thread.
		List<CharacterView> views = new ArrayList<>();
		for (int i = 0; i < team.size(); i++)
		{
			views.add(new CharacterView(team, i));
		}
		long coinsNow = coins;
		long guildXp = guild.getXp();
		int hiresAvailable = guild.getHiresAvailable();
		boolean canHire = guild.canHire(coins);
		List<BundleType> bundles = new ArrayList<>(vault.getBundles());
		List<GearItem> items = new ArrayList<>(vault.getItems());
		int capacity = vault.getCapacity();
		SwingUtilities.invokeLater(() ->
		{
			panel.showTeam(views, coinsNow, guildXp, hiresAvailable, canHire, items, capacity);
			vaultPanel.show(bundles, items, capacity);
			vaultPanel.setVisible(true);
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
