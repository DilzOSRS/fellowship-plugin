package com.dilzosrs.fellowship;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.inject.Provides;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import javax.inject.Inject;
import javax.swing.SwingUtilities;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.Experience;
import net.runelite.api.GameState;
import net.runelite.api.Player;
import net.runelite.api.Skill;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.events.StatChanged;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.gameval.AnimationID;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.VarPlayerID;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.chat.ChatColorType;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.events.RuneScapeProfileChanged;
import net.runelite.client.task.Schedule;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.overlay.OverlayManager;
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
	static final String CONFIG_GROUP = "fellowship";
	private static final String TEAM_KEY = "team";
	/** The last account played (not per account), so the game can be played from the login screen. */
	private static final String LAST_PROFILE_KEY = "lastProfile";
	private static final String COINS_KEY = "coins";
	private static final String ACTIVE_INDEX_KEY = "activeIndex";
	private static final String GUILD_XP_KEY = "guildXp";
	/** Saved since Guild levels started costing coins. Older saves work it out from the XP. */
	private static final String GUILD_LEVEL_KEY = "guildLevel";
	private static final String VAULT_KEY = "vault";
	private static final String RESCUED_KEY = "rescuedFromSwampCaves";
	private static final String FORTIFIED_KEY = "fortifiedEncampment";

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

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private ItemManager itemManager;

	@Inject
	private SpriteManager spriteManager;

	@Inject
	private PopupOverlay popup;

	private final XpTracker xpTracker = new XpTracker();
	private final Random random = new Random();
	private final List<RareUnlock> rareUnlocks = List.of(
		RareUnlock.fromSkills(CharacterClass.THIEF, Skill.THIEVING, Skill.AGILITY),
		RareUnlock.fromSkills(CharacterClass.ACOLYTE, Skill.PRAYER),
		RareUnlock.fromSkills(CharacterClass.ALCHEMIST, Skill.HERBLORE),
		RareUnlock.fromSkills(CharacterClass.SAILOR, Skill.SAILING),
		RareUnlock.fromMorytania(CharacterClass.VYRE));
	/**
	 * Skillers join from skilling XP once the first Swamp Caves run has rescued someone: 10% per
	 * 1,000 XP and per level-up for the 1st (about 10k XP), 1% for the 2nd (about 100k) and 0.4% for
	 * the 3rd (about 250k). No more than 3.
	 */
	private final RareUnlock skillerUnlock = RareUnlock.fromSkilling(CharacterClass.SKILLER, 100_000, 10_000, 4_000);
	/**
	 * One more Adventurer joins from combat XP once the encampment has been fortified: 1% per
	 * 1,000 XP and per level-up (about 100k XP). The first Adventurer is the one every game starts
	 * with, so only the 2nd can join this way.
	 */
	private final RareUnlock adventurerUnlock = RareUnlock.fromCombat(CharacterClass.ADVENTURER, 0, 10_000);
	/**
	 * The account whose save is loaded. While logged out it's the last account played, so the
	 * player can still collect and send followers from the login screen.
	 */
	private String profileKey;
	/** Kills left on the Slayer task, or -1 until the game sends it after logging in. */
	private int slayerKillsLeft = -1;
	private FellowshipPanel panel;
	private VaultPanel vaultPanel;
	private NavigationButton navButton;
	/** The account's characters, or null when no account is loaded. */
	private Team team;
	/** Coins belong to the account, not to one character. */
	private long coins;
	/** The account's Fellowship Guild, loaded with the team. */
	private Guild guild;
	/** Whether the first Swamp Caves run has rescued someone, which lets Skillers join. Loaded with the team. */
	private boolean rescued;
	/** Whether the first Fortify run is done, which lets one more Adventurer join. Loaded with the team. */
	private boolean fortified;
	/** The account's unopened bundles and gear storage, loaded with the team. */
	private Vault vault;
	/** Characters whose finished expedition has already been announced in chat, so it's said only once. */
	private final Set<FellowshipCharacter> announcedReturns = new HashSet<>();
	/** Characters resting from an injury, so their recovery is announced once. */
	private final Set<FellowshipCharacter> restingCharacters = new HashSet<>();
	/** Expeditions the team has unlocked, so each new one is announced once. */
	private Set<Expedition> unlockedExpeditions = new HashSet<>();

	@Override
	protected void startUp() throws Exception
	{
		vaultPanel = new VaultPanel(this::openBundle, this::salvage, itemManager, spriteManager);
		panel = new FellowshipPanel(this::evolve, this::sendOnExpedition, this::collectExpedition, this::makeActive,
			this::equip, this::unequip, this::transmute, this::levelUpGuild, vaultPanel, itemManager, spriteManager);
		BufferedImage icon = ImageUtil.loadImageResource(getClass(), "icon.png");

		navButton = NavigationButton.builder()
			.tooltip("Fellowship")
			.icon(icon)
			.priority(10)
			.panel(panel)
			.build();

		clientToolbar.addNavigation(navButton);
		overlayManager.add(popup);

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
		overlayManager.remove(popup);
		panel.stopTimers();
		team = null;
		log.debug("Fellowship stopped!");
	}

	@Provides
	FellowshipConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(FellowshipConfig.class);
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		// The "Start a new game" setting works like a button: RuneLite has already asked the player to
		// confirm, so wipe the account's progress and untick it again.
		if (!CONFIG_GROUP.equals(event.getGroup()) || !FellowshipConfig.RESET_KEY.equals(event.getKey())
			|| !Boolean.parseBoolean(event.getNewValue()))
		{
			return;
		}
		configManager.setConfiguration(CONFIG_GROUP, FellowshipConfig.RESET_KEY, false);
		clientThread.invokeLater(this::startNewGame);
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
			// Every Vault.GENERIC_BUNDLE_XP of OSRS XP (all skills together) gives a chance of a Basic Bundle.
			int bundlesFound = vault.addXpForGenericBundles(gained, random);
			for (int i = 0; i < bundlesFound; i++)
			{
				announceBundle(BundleType.GENERIC);
			}
			// XP gives a rare chance of a character joining (a Thief from Thieving and Agility,
			// an Acolyte from Prayer, an Alchemist from Herblore, a Sailor from Sailing, a Vyre from any XP in Morytania).
			int osrsLevelsGained = Experience.getLevelForXp(event.getXp())
				- Experience.getLevelForXp(event.getXp() - gained);
			boolean inMorytania = isInMorytania();
			for (RareUnlock unlock : rareUnlocks)
			{
				CharacterClass found = unlock.getCharacterClass();
				if (unlock.isMorytaniaOnly() && !inMorytania)
				{
					continue;
				}
				if (unlock.roll(event.getSkill(), gained, osrsLevelsGained, team.countStartedAs(found), random))
				{
					team.add(found);
					announceNewCharacter();
				}
			}
			if (rescued && skillerUnlock.roll(event.getSkill(), gained, osrsLevelsGained,
				team.countStartedAs(CharacterClass.SKILLER), random))
			{
				team.add(CharacterClass.SKILLER);
				announceNewCharacter();
				if (team.countStartedAs(CharacterClass.SKILLER) == 1)
				{
					sendChatMessage("The man you rescued from the Lumbridge Swamp Caves has decided to join your Fellowship.");
				}
			}
			if (fortified && adventurerUnlock.roll(event.getSkill(), gained, osrsLevelsGained,
				team.countStartedAs(CharacterClass.ADVENTURER), random))
			{
				team.add(CharacterClass.ADVENTURER);
				announceNewCharacter();
				sendChatMessage("An adventurer who heard about your encampment has decided to join your Fellowship.");
			}
			// Save on every gain, so progress is never lost. This only updates RuneLite's
			// settings in memory; RuneLite writes them to disk itself.
			saveTeam();
			saveVault();
			updatePanel();
		}
	}

	@Subscribe
	public void onAnimationChanged(AnimationChanged event)
	{
		// Casting Low or High Level Alchemy gives a rare chance of an Alchemist joining.
		// Only the player's own casts count, read from their animation.
		if (team == null || event.getActor() != client.getLocalPlayer())
		{
			return;
		}
		int animation = event.getActor().getAnimation();
		if (animation != AnimationID.HUMAN_CASTHIGHLVLALCHEMY && animation != AnimationID.HUMAN_CASTHIGHLVLALCHEMY_FIRE
			&& animation != AnimationID.HUMAN_CASTLOWLVLALCHEMY && animation != AnimationID.HUMAN_CASTLOWLVLALCHEMY_FIRE)
		{
			return;
		}
		if (RareUnlock.rollAlch(team.countStartedAs(CharacterClass.ALCHEMIST), random))
		{
			team.add(CharacterClass.ALCHEMIST);
			announceNewCharacter();
			saveTeam();
			updatePanel();
		}
	}

	@Subscribe
	public void onWidgetLoaded(WidgetLoaded event)
	{
		// Opening a shop or the Grand Exchange gives a rare chance of a Merchant joining.
		int group = event.getGroupId();
		if (team == null || (group != InterfaceID.SHOPMAIN && group != InterfaceID.GE_OFFERS))
		{
			return;
		}
		if (RareUnlock.rollInteraction(team.countStartedAs(CharacterClass.MERCHANT), random))
		{
			team.add(CharacterClass.MERCHANT);
			announceNewCharacter();
			saveTeam();
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
		announceBundle(bundle);
		updatePanel();
	}

	@Subscribe
	public void onVarbitChanged(VarbitChanged event)
	{
		// Slayer task kills: the game's count of kills left goes down by one per kill.
		if (event.getVarpId() != VarPlayerID.SLAYER_COUNT)
		{
			return;
		}
		int before = slayerKillsLeft;
		slayerKillsLeft = event.getValue();
		if (vault == null || team == null || before < 0)
		{
			return;
		}
		int bundles = SlayerLoot.bundlesFor(before, slayerKillsLeft, random);
		for (int i = 0; i < bundles; i++)
		{
			vault.addBundle(BundleType.SLAYER);
			announceBundle(BundleType.SLAYER);
		}
		if (bundles > 0)
		{
			saveVault();
			updatePanel();
		}
	}

	/** Every game tick's length, logged in or not, so returns and recoveries also show on the login screen. */
	@Schedule(period = 600, unit = ChronoUnit.MILLIS)
	public void checkReturns()
	{
		clientThread.invokeLater(this::announceReturns);
	}

	private void announceReturns()
	{
		// Announce each finished expedition once. The team is small, so checking often is cheap.
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
					+ character.getExpedition().getOngoingName() + "! Collect the rewards in the Fellowship panel.");
			}
			if (restingCharacters.contains(character) && !character.isResting(now))
			{
				restingCharacters.remove(character);
				sendChatMessage(team.getDisplayName(i) + " has recovered and is ready for action!");
				updatePanel();
			}
		}
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		// Logged out: the next login (maybe on another account) starts a fresh snapshot. The team
		// stays loaded, so the game can still be played from the login screen.
		if (event.getGameState() == GameState.LOGIN_SCREEN)
		{
			xpTracker.reset();
			slayerKillsLeft = -1;
		}
		// Logging back into the same account doesn't count as a profile change, so
		// onRuneScapeProfileChanged won't load the team again. Load it here if it's missing, or if the
		// login screen was showing a different account.
		else if (event.getGameState() == GameState.LOGGED_IN
			&& (team == null || !Objects.equals(configManager.getRSProfileKey(), profileKey)))
		{
			loadTeam();
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
			Blessing blessing = newClass.getBlessing();
			String message = blessing != null
				? oldName + " has devoted themselves to " + blessing.getGodName() + "!"
				: oldName + " has been trained in the ways of the " + newClass.getDisplayName() + "!";
			sendChatMessage(message);
			showClassPopup(newClass, message);
			announceNewExpeditions();
			addGuildXp(1);
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
			if (team == null)
			{
				return;
			}
			// Expeditions that take an item along are sent with transmute() instead.
			if (expedition.needsItem())
			{
				return;
			}
			int cost = expedition.getCost();
			if (coins < cost)
			{
				sendChatMessage(expedition.getDisplayName() + " costs " + cost + " coins, but you only have "
					+ coins + ".");
				return;
			}
			if (!team.sendParty(party, expedition, Instant.now()))
			{
				return;
			}
			if (cost > 0)
			{
				coins -= cost;
				saveCoins();
			}
			saveTeam();
			updatePanel();
		});
	}

	/**
	 * Called from the panel (on the Swing thread) when the player picks a stored item for an
	 * Alchemist to transmute: it leaves storage and goes with them on Transmuting metals.
	 */
	private void transmute(int characterIndex, int storageIndex)
	{
		clientThread.invokeLater(() ->
		{
			FellowshipCharacter character = characterAt(characterIndex);
			if (character == null || vault == null || storageIndex >= vault.getItems().size()
				|| !team.sendParty(List.of(characterIndex), Expedition.TRANSMUTING_METALS, Instant.now()))
			{
				return;
			}
			GearItem item = vault.takeItem(storageIndex);
			character.setCarrying(item);
			sendChatMessage(team.getDisplayName(characterIndex) + " sets off to transmute the " + item.getName() + ".");
			saveVault();
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
			// An item taken along to be transmuted turns into coins below.
			GearItem transmuted = character.getCarrying();
			character.setCarrying(null);
			int[] levelsBefore = currentLevels();
			long[] xpBefore = new long[team.size()];
			for (int i = 0; i < team.size(); i++)
			{
				xpBefore[i] = team.get(i).getXp();
			}

			// Dangerous expeditions can fail (the party's average ATK against the threat level), and
			// members can be injured (DEF against the threat level). Gear can't change while away,
			// so these are worked out from what the party set off with.
			Instant now = Instant.now();
			List<Integer> partyBefore = team.partyOf(index);
			double[] memberDefs = team.memberDefs(partyBefore);
			boolean success = Danger.rollSuccess(finished.getThreatLevel(), team.averageAtk(partyBefore), random);
			List<Integer> party = team.collectParty(index, now, success);
			if (party.isEmpty())
			{
				return;
			}
			List<Integer> injured = new ArrayList<>();
			for (int position : Danger.rollInjuries(finished.getThreatLevel(), success, memberDefs, random))
			{
				injured.add(partyBefore.get(position));
			}
			Duration rest = finished.getDuration(partyBefore.size()).dividedBy(Danger.REST_DIVISOR);
			for (int member : injured)
			{
				team.get(member).injure(now.plus(rest));
				restingCharacters.add(team.get(member));
			}

			if (!success)
			{
				sendChatMessage(joinNames(party) + (party.size() == 1 ? " was" : " were") + " overwhelmed in "
					+ finished.getOngoingName() + " and came back empty-handed.");
				announceInjuries(injured, rest);
				rescueIfFirstSwampCavesRun(finished, party);
				noticeIfFirstFortifyRun(finished);
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
			if (transmuted != null)
			{
				coinsGained += transmuted.getRarity().getSalvageValue() * Expedition.TRANSMUTE_MULTIPLIER;
			}
			coins += coinsGained;
			BundleType bonus = finished.rollBonusBundle(random);
			String bundles = null;
			if (bonus != null)
			{
				int count = finished.getBonusBundleCount();
				for (int i = 0; i < count; i++)
				{
					vault.addBundle(bonus);
				}
				saveVault();
				bundles = count == 1 ? "a " + bonus.getDisplayName() : count + " " + bonus.getDisplayName() + "s";
			}
			// The main haul is coins, or the bundles on expeditions that give no coins (the trade run).
			String haul = coinsGained > 0 ? coinsGained + " coins" : bundles;

			long xpEach = finished.getXpReward();
			sendChatMessage(joinNames(party) + " brought back "
				+ String.format("%,d XP", xpEach) + (party.size() > 1 ? " each" : "")
				+ (haul != null ? " and " + haul : "") + " from " + finished.getOngoingName() + "!"
				+ (bundles != null && coinsGained > 0 ? " They also found " + bundles + "!" : ""));
			if (bundles != null)
			{
				String finder = party.size() > 1 ? "Your party" : team.getDisplayName(party.get(0));
				showBundlePopup(bonus, finder + " brought back " + bundles + "!");
			}
			announceInjuries(injured, rest);
			rescueIfFirstSwampCavesRun(finished, party);
			noticeIfFirstFortifyRun(finished);
			announceLevelUps(levelsBefore);

			int levelsGained = 0;
			for (int member : party)
			{
				announcedReturns.remove(team.get(member));
				levelsGained += team.get(member).getLevel() - levelsBefore[member];
				long gained = team.get(member).getXp() - xpBefore[member];
				String loot = coinsGained > 0 ? "+" + coinsGained + " coins" : bundles;
				SwingUtilities.invokeLater(() -> panel.showReward(member, gained, loot));
			}
			// 1 Guild XP for the expedition, plus 1 for each level its XP reward gave.
			addGuildXp(1 + levelsGained);

			saveTeam();
			saveCoins();
			updatePanel();
		});
	}

	/** Tell the player a bundle dropped: a chat message and an on-screen pop-up. */
	private void announceBundle(BundleType bundle)
	{
		sendChatMessage("You found a " + bundle.getDisplayName() + "! Open it in the Fellowship panel.");
		showBundlePopup(bundle, "You found a " + bundle.getDisplayName() + "!");
	}

	/** Show a pop-up on the game screen with the bundle's casket beside the message. */
	private void showBundlePopup(BundleType bundle, String message)
	{
		popup.show(message, itemManager.getImage(bundle.getIconItemId()));
	}

	private void announceInjuries(List<Integer> injured, Duration rest)
	{
		if (injured.isEmpty())
		{
			return;
		}
		sendChatMessage(joinNames(injured) + (injured.size() == 1 ? " was" : " were") + " injured and must rest for "
			+ Danger.describeRest(rest) + ".");
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

	/** Whether the player is standing in Morytania (or an instance there). Call on the client thread. */
	private boolean isInMorytania()
	{
		Player player = client.getLocalPlayer();
		if (player == null)
		{
			return false;
		}
		// In an instance (like the Theatre of Blood), this gives the real map tile it copies.
		WorldPoint location = WorldPoint.fromLocalInstance(client, player.getLocalLocation());
		return location != null && Morytania.contains(location.getX(), location.getY());
	}

	/**
	 * Announce the newest team member in chat and with a pop-up.
	 * Call on the client thread.
	 */
	private void announceNewCharacter()
	{
		int index = team.size() - 1;
		String message = team.getDisplayName(index) + " has joined your Fellowship!";
		sendChatMessage(message);
		showClassPopup(team.get(index).getCharacterClass(), message);
		announceNewExpeditions();
	}

	/**
	 * Tell the player about expeditions the team has just unlocked. Unlocks only change when someone
	 * joins, takes a class advancement or the Guild levels up, so this is only checked then (and
	 * remembered on load).
	 */
	private void announceNewExpeditions()
	{
		for (Expedition expedition : team.unlockedExpeditions(guild.getLevel()))
		{
			if (unlockedExpeditions.add(expedition))
			{
				sendChatMessage(expedition.getDisplayName() + " expedition has been unlocked.");
				if (expedition == Expedition.DORGESHUUN_CAVES)
				{
					sendChatMessage("You can now send a team of " + expedition.getMinPartySize()
						+ " followers into a dungeon.");
				}
				if (expedition == Expedition.FORTIFYING_ENCAMPMENT)
				{
					sendChatMessage("Your encampment can now be fortified by your Skillers.");
				}
			}
		}
	}

	/** Show a pop-up on the game screen with the class's icon beside the message. */
	private void showClassPopup(CharacterClass characterClass, String message)
	{
		popup.show(message, itemManager.getImage(characterClass.getIconItemId()));
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
			configManager.setConfiguration(CONFIG_GROUP, profileKey, ACTIVE_INDEX_KEY, index);
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
			if (opened.item == null)
			{
				// The Basic Bundle holds an item only half the time. Just a chat message, no pop-up.
				sendChatMessage("You opened a " + bundle.getDisplayName() + " and find " + opened.coins
					+ " coins but no items this time.");
				SwingUtilities.invokeLater(() -> vaultPanel.showOpened(bundle, null, opened.coins));
				updatePanel();
				return;
			}
			sendChatMessage("You opened a " + bundle.getDisplayName() + ": " + opened.item.getName()
				+ " (" + opened.item.getStatText() + ")" + (opened.coins > 0 ? " and " + opened.coins + " coins" : "") + "!");
			Rarity rarity = opened.item.getRarity();
			String article = "AEIOU".indexOf(Character.toUpperCase(opened.item.getName().charAt(0))) >= 0 ? "an " : "a ";
			popup.show("You pulled " + article + opened.item.getName() + "!",
				itemManager.getImage(opened.item.getIconItemId()), rarity.getColor());
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
					// Said once, when it first becomes ready, not on every level after.
					+ (character.canEvolve() && levelsBefore[i] < CharacterClass.ADVANCEMENT_LEVEL
						? " " + name + "'s class advancement is ready." : ""));
			}
		}
	}

	private void addGuildXp(long amount)
	{
		boolean wasReady = guild.isReadyToLevelUp();
		guild.addXp(amount);
		saveGuild();
		if (!wasReady && guild.isReadyToLevelUp())
		{
			sendChatMessage(String.format("Your Fellowship Guild is ready to reach level %d, for %,d coins.",
				guild.getLevel() + 1, guild.getLevelUpCost()));
		}
	}

	/**
	 * Called from the panel (on the Swing thread) when the player pays to level up the Guild.
	 */
	private void levelUpGuild()
	{
		clientThread.invokeLater(() ->
		{
			long cost = guild.getLevelUpCost();
			if (coins < cost || !guild.levelUp())
			{
				return;
			}
			coins -= cost;
			saveCoins();
			saveGuild();
			sendChatMessage("Your Fellowship Guild reached level " + guild.getLevel() + "!");
			announceNewExpeditions();
			updatePanel();
		});
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
		profileKey = configManager.getRSProfileKey();
		if (profileKey == null)
		{
			// Not logged in (e.g. the client was just opened), so load the last account played.
			profileKey = configManager.getConfiguration(CONFIG_GROUP, LAST_PROFILE_KEY);
			if (profileKey == null)
			{
				SwingUtilities.invokeLater(panel::showLoggedOut);
				return;
			}
		}
		else
		{
			configManager.setConfiguration(CONFIG_GROUP, LAST_PROFILE_KEY, profileKey);
		}

		String json = configManager.getConfiguration(CONFIG_GROUP, profileKey, TEAM_KEY);
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
			Integer savedActive = configManager.getConfiguration(CONFIG_GROUP, profileKey, ACTIVE_INDEX_KEY, Integer.class);
			team = new Team(loaded, savedActive == null ? 0 : savedActive);
		}

		restingCharacters.clear();
		for (FellowshipCharacter character : team.getCharacters())
		{
			if (character.isResting(Instant.now()))
			{
				restingCharacters.add(character);
			}
		}

		Long savedCoins = configManager.getConfiguration(CONFIG_GROUP, profileKey, COINS_KEY, Long.class);
		coins = savedCoins == null ? 0 : savedCoins;

		Long savedGuildXp = configManager.getConfiguration(CONFIG_GROUP, profileKey, GUILD_XP_KEY, Long.class);
		long guildXp = savedGuildXp == null ? 0 : savedGuildXp;
		Integer savedGuildLevel = configManager.getConfiguration(CONFIG_GROUP, profileKey, GUILD_LEVEL_KEY, Integer.class);
		guild = new Guild(guildXp, savedGuildLevel == null ? Guild.levelForXp(guildXp) : savedGuildLevel);

		// Expeditions already unlocked aren't announced again.
		unlockedExpeditions = team.unlockedExpeditions(guild.getLevel());

		rescued = Boolean.TRUE.equals(configManager.getConfiguration(CONFIG_GROUP, profileKey, RESCUED_KEY, Boolean.class));
		fortified = Boolean.TRUE.equals(configManager.getConfiguration(CONFIG_GROUP, profileKey, FORTIFIED_KEY, Boolean.class));

		String vaultJson = configManager.getConfiguration(CONFIG_GROUP, profileKey, VAULT_KEY);
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
	 * Wipe the logged-in account's Fellowship and load a fresh one: a single Adventurer, no coins,
	 * Guild XP, bundles or gear. Must be called on the client thread.
	 */
	private void startNewGame()
	{
		if (configManager.getRSProfileKey() == null)
		{
			log.debug("Can't start a new Fellowship game while logged out");
			return;
		}
		for (String key : new String[]{TEAM_KEY, COINS_KEY, ACTIVE_INDEX_KEY, GUILD_XP_KEY, GUILD_LEVEL_KEY, VAULT_KEY,
			RESCUED_KEY, FORTIFIED_KEY})
		{
			configManager.unsetRSProfileConfiguration(CONFIG_GROUP, key);
		}
		restingCharacters.clear();
		loadTeam();
		sendChatMessage("Your Fellowship has started a new game.");
	}

	/**
	 * Build a one-character team from the old single-character save (or a fresh Adventurer if
	 * there isn't one), save it in the new format, then remove the old keys.
	 */
	private Team moveOldSaveIntoTeam()
	{
		SavedCharacter old = new SavedCharacter();
		old.characterClass = configManager.getConfiguration(CONFIG_GROUP, profileKey, LEGACY_CLASS_KEY);
		Long oldXp = configManager.getConfiguration(CONFIG_GROUP, profileKey, LEGACY_XP_KEY, Long.class);
		old.xp = oldXp == null ? 0 : oldXp;
		old.expedition = configManager.getConfiguration(CONFIG_GROUP, profileKey, LEGACY_EXPEDITION_KEY);
		old.expeditionStart = configManager.getConfiguration(CONFIG_GROUP, profileKey, LEGACY_EXPEDITION_START_KEY, Long.class);

		List<FellowshipCharacter> moved = new ArrayList<>();
		moved.add(old.toCharacter());
		team = new Team(moved);

		// Save the new format first, so progress is never lost if something goes wrong in between.
		saveTeam();
		configManager.unsetConfiguration(CONFIG_GROUP, profileKey, LEGACY_XP_KEY);
		configManager.unsetConfiguration(CONFIG_GROUP, profileKey, LEGACY_CLASS_KEY);
		configManager.unsetConfiguration(CONFIG_GROUP, profileKey, LEGACY_EXPEDITION_KEY);
		configManager.unsetConfiguration(CONFIG_GROUP, profileKey, LEGACY_EXPEDITION_START_KEY);
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
		configManager.setConfiguration(CONFIG_GROUP, profileKey, TEAM_KEY, gson.toJson(saved));
	}

	private void saveCoins()
	{
		configManager.setConfiguration(CONFIG_GROUP, profileKey, COINS_KEY, coins);
	}

	private void saveVault()
	{
		configManager.setConfiguration(CONFIG_GROUP, profileKey, VAULT_KEY, gson.toJson(SavedVault.from(vault)));
	}

	/**
	 * The first Swamp Caves run, whether it succeeds or fails, rescues someone trapped in the cave.
	 * It only happens once per account, and from then on Skillers can join.
	 */
	private void rescueIfFirstSwampCavesRun(Expedition finished, List<Integer> party)
	{
		if (finished != Expedition.LUMBRIDGE_SWAMP_CAVES || rescued)
		{
			return;
		}
		rescued = true;
		configManager.setConfiguration(CONFIG_GROUP, profileKey, RESCUED_KEY, true);
		boolean solo = party.size() == 1;
		sendChatMessage(joinNames(party) + (solo ? " finds" : " find") + " someone trapped in the cave and "
			+ (solo ? "helps" : "help") + " them out.");
	}

	/**
	 * The first Fortify run gets the encampment noticed. It only happens once per account, and
	 * from then on one more Adventurer can join.
	 */
	private void noticeIfFirstFortifyRun(Expedition finished)
	{
		if (finished != Expedition.FORTIFYING_ENCAMPMENT || fortified)
		{
			return;
		}
		fortified = true;
		configManager.setConfiguration(CONFIG_GROUP, profileKey, FORTIFIED_KEY, true);
		sendChatMessage("Adventurers have started noticing your encampment.");
	}

	private void saveGuild()
	{
		configManager.setConfiguration(CONFIG_GROUP, profileKey, GUILD_XP_KEY, guild.getXp());
		configManager.setConfiguration(CONFIG_GROUP, profileKey, GUILD_LEVEL_KEY, guild.getLevel());
	}

	private void updatePanel()
	{
		// Copy the values now, because the panel is updated later on the Swing thread.
		List<CharacterView> views = new ArrayList<>();
		for (int i = 0; i < team.size(); i++)
		{
			views.add(new CharacterView(team, i, guild.getLevel()));
		}
		long coinsNow = coins;
		long guildXp = guild.getXp();
		int guildLevel = guild.getLevel();
		List<BundleType> bundles = new ArrayList<>(vault.getBundles());
		List<GearItem> items = new ArrayList<>(vault.getItems());
		int capacity = vault.getCapacity();
		SwingUtilities.invokeLater(() ->
		{
			panel.showTeam(views, coinsNow, guildXp, guildLevel, items, capacity);
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
		slayerKillsLeft = client.getVarpValue(VarPlayerID.SLAYER_COUNT);
	}
}
