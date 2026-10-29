package com.dilzosrs.fellowship;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.IntConsumer;
import java.util.function.Predicate;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.ui.components.materialtabs.MaterialTab;
import net.runelite.client.ui.components.materialtabs.MaterialTabGroup;

/**
 * The Fellowship sidebar panel: the coin balance and Guild, then three tabs. Followers has one card
 * per character and recruiting; Events has the expeditions that are out; Bank has bundles and storage.
 */
public class FellowshipPanel extends PluginPanel
{
	private static final String LOGGED_OUT_TEXT = "Log in to see your Fellowship";

	private final BiConsumer<Integer, CharacterClass> onEvolve;
	private final BiConsumer<List<Integer>, Expedition> onSendExpedition;
	private final IntConsumer onCollect;
	private final IntConsumer onMakeActive;
	private final Runnable onHire;
	private final BiConsumer<Integer, Integer> onEquip;
	private final BiConsumer<Integer, GearType.Slot> onUnequip;

	// The latest team and storage shown, so choosing gear knows what can be picked.
	private List<CharacterView> shownTeam = new ArrayList<>();
	private List<GearItem> shownStorage = new ArrayList<>();
	private int shownCapacity;

	private final JLabel coinsLabel = new JLabel();
	private final JLabel guildLabel = new JLabel();
	private final JPanel cardsPanel = new JPanel();
	private final List<CharacterCard> cards = new ArrayList<>();
	private final JPanel hireArea = new JPanel(new GridLayout(0, 1, 0, 4));
	private final JLabel hiresLabel = new JLabel();
	private final JButton hireButton = new JButton("Hire Adventurer (" + Guild.HIRE_COST + " coins)");
	private final JLabel skillersLabel = new JLabel();
	private final JButton skillerButton = new JButton();
	private final Runnable onHireSkiller;
	private int shownSkillerCost;
	private final VaultPanel vaultPanel;
	private final ExpeditionsPanel expeditionsPanel;
	private final MaterialTabGroup tabs;
	private final MaterialTab followersTabButton;
	private final MaterialTab bankTabButton;
	/** Draws the in-game item icons the cards use for each class. */
	private final ItemManager itemManager;
	/** Draws the OSRS gear slot tiles on the cards. */
	private final SpriteManager spriteManager;

	/**
	 * The callbacks are called on the Swing thread once the player confirms the action.
	 * The first Integer/int is the character's position in the team; onEquip's second is the
	 * item's position in storage. onSendExpedition gets everyone going (one character, or a party).
	 */
	public FellowshipPanel(BiConsumer<Integer, CharacterClass> onEvolve, BiConsumer<List<Integer>, Expedition> onSendExpedition,
		IntConsumer onCollect, IntConsumer onMakeActive, Runnable onHire, Runnable onHireSkiller,
		BiConsumer<Integer, Integer> onEquip, BiConsumer<Integer, GearType.Slot> onUnequip, VaultPanel vaultPanel,
		ItemManager itemManager, SpriteManager spriteManager)
	{
		this.itemManager = itemManager;
		this.spriteManager = spriteManager;
		this.vaultPanel = vaultPanel;
		this.onHireSkiller = onHireSkiller;
		this.onEquip = onEquip;
		this.onUnequip = onUnequip;
		this.onEvolve = onEvolve;
		this.onSendExpedition = onSendExpedition;
		this.onCollect = onCollect;
		this.onMakeActive = onMakeActive;
		this.onHire = onHire;

		setBorder(new EmptyBorder(10, 10, 10, 10));
		setLayout(new BorderLayout(0, 10));

		JPanel titleRow = new JPanel(new BorderLayout());
		titleRow.setOpaque(false);
		JLabel title = new JLabel("Fellowship");
		title.setFont(FontManager.getRunescapeBoldFont());
		title.setForeground(Color.WHITE);
		titleRow.add(title, BorderLayout.WEST);
		coinsLabel.setForeground(ColorScheme.GRAND_EXCHANGE_PRICE);
		titleRow.add(coinsLabel, BorderLayout.EAST);
		guildLabel.setFont(FontManager.getRunescapeSmallFont());
		guildLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
		titleRow.add(guildLabel, BorderLayout.SOUTH);
		add(titleRow, BorderLayout.NORTH);

		cardsPanel.setLayout(new BoxLayout(cardsPanel, BoxLayout.Y_AXIS));
		cardsPanel.setOpaque(false);
		// Shown until an account's team is loaded.
		cardsPanel.add(new JLabel(LOGGED_OUT_TEXT));

		// Three tabs: Followers (the cards and hiring), Events (expeditions that are out) and
		// Bank (bundles and storage).
		expeditionsPanel = new ExpeditionsPanel(itemManager, onCollect);
		JPanel followersTab = new JPanel(new BorderLayout(0, 10));
		followersTab.setOpaque(false);
		followersTab.add(cardsPanel, BorderLayout.CENTER);
		followersTab.add(hireArea, BorderLayout.SOUTH);

		// Storage stays hidden until an account is loaded.
		vaultPanel.setVisible(false);
		JPanel inventoryTab = new JPanel(new BorderLayout());
		inventoryTab.setOpaque(false);
		inventoryTab.add(vaultPanel, BorderLayout.NORTH);

		JPanel tabContent = new JPanel(new BorderLayout());
		tabContent.setOpaque(false);
		tabs = new MaterialTabGroup(tabContent);
		// Tighter gaps than the default, so all three tabs fit on one line in the narrow sidebar.
		tabs.setLayout(new FlowLayout(FlowLayout.CENTER, 2, 0));
		followersTabButton = new MaterialTab("Followers", tabs, followersTab);
		tabs.addTab(followersTabButton);
		// Short names, because longer ones don't fit side by side in the narrow sidebar.
		MaterialTab eventsTabButton = new MaterialTab("Events", tabs, expeditionsPanel);
		tabs.addTab(eventsTabButton);
		bankTabButton = new MaterialTab("Bank", tabs, inventoryTab);
		tabs.addTab(bankTabButton);
		tabs.select(followersTabButton);
		// Leaving the Bank while picking gear cancels the pick.
		followersTabButton.setOnSelectEvent(() ->
		{
			vaultPanel.stopChoosing();
			return true;
		});
		eventsTabButton.setOnSelectEvent(() ->
		{
			vaultPanel.stopChoosing();
			return true;
		});

		JPanel tabArea = new JPanel(new BorderLayout(0, 10));
		tabArea.setOpaque(false);
		tabArea.add(tabs, BorderLayout.NORTH);
		tabArea.add(tabContent, BorderLayout.CENTER);
		add(tabArea, BorderLayout.CENTER);

		hireArea.setOpaque(false);
		hiresLabel.setFont(FontManager.getRunescapeSmallFont());
		hiresLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
		hireArea.add(hiresLabel);
		hireButton.addActionListener(e -> confirmHire());
		hireArea.add(hireButton);
		skillersLabel.setFont(FontManager.getRunescapeSmallFont());
		skillersLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
		hireArea.add(skillersLabel);
		skillerButton.addActionListener(e -> confirmHireSkiller());
		hireArea.add(skillerButton);
		hireArea.setVisible(false);
	}

	/**
	 * Stop all countdown timers. Call when the plugin shuts down.
	 */
	public void stopTimers()
	{
		cards.forEach(CharacterCard::stopTimer);
		expeditionsPanel.stopTimer();
	}

	/**
	 * Show the whole team, the coin balance, the Guild and hiring. Must be called on the Swing thread.
	 */
	public void showTeam(List<CharacterView> team, long coins, long guildXp, int hiresAvailable, boolean canHire,
		List<GearItem> storage, int capacity)
	{
		shownTeam = team;
		shownStorage = storage;
		shownCapacity = capacity;
		coinsLabel.setText(String.format("%,d coins", coins));
		int guildLevel = Guild.levelForXp(guildXp);
		long levelStart = Guild.xpForLevel(guildLevel);
		long levelEnd = Guild.xpForLevel(guildLevel + 1);
		guildLabel.setText(String.format("Guild level %d (%d / %d XP)",
			guildLevel, guildXp - levelStart, levelEnd - levelStart));

		if (cards.size() > team.size() || cards.isEmpty())
		{
			// A different account (or the first load): start the cards over.
			stopTimers();
			cards.clear();
			cardsPanel.removeAll();
		}
		while (cards.size() < team.size())
		{
			addCard();
		}
		for (int i = 0; i < team.size(); i++)
		{
			cards.get(i).show(team.get(i));
		}
		expeditionsPanel.show(team);

		hiresLabel.setText(hiresAvailable > 0
			? "Hires available: " + hiresAvailable
			: "Next hire at Guild level " + (guildLevel + 1));
		hireButton.setEnabled(canHire);
		hireArea.setVisible(true);

		cardsPanel.revalidate();
		cardsPanel.repaint();
	}

	/**
	 * Hide the team while logged out, since the next login might be another account and
	 * the buttons would do nothing. Must be called on the Swing thread.
	 */
	public void showLoggedOut()
	{
		stopTimers();
		cards.clear();
		cardsPanel.removeAll();
		cardsPanel.add(new JLabel(LOGGED_OUT_TEXT));
		shownTeam = new ArrayList<>();
		shownStorage = new ArrayList<>();
		vaultPanel.stopChoosing();
		expeditionsPanel.show(shownTeam);
		coinsLabel.setText("");
		guildLabel.setText("");
		hireArea.setVisible(false);
		vaultPanel.setVisible(false);
		revalidate();
		repaint();
	}

	/**
	 * Show on one character's card that its last expedition failed. Must be called on the Swing thread.
	 */
	public void showFailure(int index, Expedition expedition)
	{
		if (index < cards.size())
		{
			cards.get(index).showFailure(expedition);
		}
	}

	/**
	 * Show what the last expedition gave on one character's card. Must be called on the Swing thread.
	 */
	public void showReward(int index, long xpGained, String loot)
	{
		if (index < cards.size())
		{
			cards.get(index).showReward(xpGained, loot);
		}
	}

	private void addCard()
	{
		int index = cards.size();
		CharacterCard card = new CharacterCard(
			itemManager,
			spriteManager,
			newClass -> onEvolve.accept(index, newClass),
			expedition -> send(index, expedition),
			() -> onCollect.accept(index),
			() -> onMakeActive.accept(index),
			slot -> chooseGear(index, slot));
		card.setAlignmentX(LEFT_ALIGNMENT);
		if (index > 0)
		{
			cardsPanel.add(Box.createVerticalStrut(8));
		}
		cardsPanel.add(card);
		cards.add(card);
	}

	/**
	 * Send a character away. Solo expeditions were already confirmed on the card; party
	 * expeditions need the player to pick the rest of the party first.
	 */
	private void send(int characterIndex, Expedition expedition)
	{
		if (expedition.getPartySize() <= 1)
		{
			onSendExpedition.accept(List.of(characterIndex), expedition);
			return;
		}
		if (characterIndex >= shownTeam.size())
		{
			return;
		}

		int needed = expedition.getPartySize() - 1;
		List<Integer> candidates = new ArrayList<>();
		for (int i = 0; i < shownTeam.size(); i++)
		{
			if (i != characterIndex && shownTeam.get(i).expedition == null && shownTeam.get(i).restingUntil == null)
			{
				candidates.add(i);
			}
		}
		if (candidates.size() < needed)
		{
			JOptionPane.showMessageDialog(this, expedition.getDisplayName() + " needs a party of "
				+ expedition.getPartySize() + ", but only " + (candidates.size() + 1)
				+ " of your characters are home and healthy.",
				"Expedition", JOptionPane.INFORMATION_MESSAGE);
			return;
		}

		JPanel picker = new JPanel(new GridLayout(0, 1, 0, 2));
		picker.add(new JLabel("<html>" + shownTeam.get(characterIndex).name + " is going to "
			+ expedition.getDisplayName() + " (threat " + expedition.getThreatLevel() + ").<br>"
			+ "Pick " + needed + " more to go with them:</html>"));
		List<JCheckBox> boxes = new ArrayList<>();
		for (int index : candidates)
		{
			CharacterView view = shownTeam.get(index);
			JCheckBox box = new JCheckBox(view.name + "  (ATK " + view.atk + ", DEF " + view.def + ", MAG " + view.mag + ")");
			boxes.add(box);
			picker.add(box);
		}
		picker.add(new JLabel("<html><br>They won't earn XP from your OSRS play while they're away.</html>"));

		int choice = JOptionPane.showConfirmDialog(this, picker, "Choose a party", JOptionPane.OK_CANCEL_OPTION);
		if (choice != JOptionPane.OK_OPTION)
		{
			return;
		}
		List<Integer> party = new ArrayList<>();
		party.add(characterIndex);
		for (int i = 0; i < boxes.size(); i++)
		{
			if (boxes.get(i).isSelected())
			{
				party.add(candidates.get(i));
			}
		}
		if (party.size() != expedition.getPartySize())
		{
			JOptionPane.showMessageDialog(this, "Pick exactly " + needed + " more characters.", "Expedition",
				JOptionPane.INFORMATION_MESSAGE);
			return;
		}

		// Show the odds before they go: party ATK is the average of each member's ATK + half their MAG.
		double partyAtk = party.stream().mapToDouble(i -> shownTeam.get(i).atk + shownTeam.get(i).mag / 2.0)
			.average().orElse(0);
		int failPercent = (int) Math.round(Danger.failChance(expedition.getThreatLevel(), partyAtk) * 100);
		String odds = String.format("Party ATK %.1f vs threat %d: ", partyAtk, expedition.getThreatLevel())
			+ (failPercent == 0 ? "they'll clear it." : failPercent + "% chance of failing.\nA failed expedition gives no XP or loot.");
		int go = JOptionPane.showConfirmDialog(this, odds + "\n\nSend them?", "Expedition", JOptionPane.YES_NO_OPTION);
		if (go != JOptionPane.YES_OPTION)
		{
			return;
		}
		onSendExpedition.accept(party, expedition);
	}

	/**
	 * Let the player pick what a character wears in a slot: opens the Bank showing only the items
	 * that fit. Clicking one equips it; the Bank's banner can also take off what's worn, or cancel.
	 * Either way it comes back to the Followers tab.
	 */
	private void chooseGear(int characterIndex, GearType.Slot slot)
	{
		if (characterIndex >= shownTeam.size())
		{
			return;
		}
		CharacterView character = shownTeam.get(characterIndex);
		if (character.expedition != null)
		{
			JOptionPane.showMessageDialog(this, character.name + " is away. Gear can't be changed until they're back.",
				"Gear", JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		GearItem equipped = character.getEquipped(slot);

		// Storage items that go in this slot and that this character's class can use.
		Predicate<GearItem> fits = item -> item.getType().getSlot() == slot
			&& item.getType().canBeUsedBy(character.characterClass);
		String prompt = "Choose a " + slot.getDisplayName().toLowerCase() + " for " + character.name + ":";
		if (shownStorage.stream().noneMatch(fits))
		{
			prompt += "<br><br>" + (character.characterClass == CharacterClass.ADVENTURER && slot != GearType.Slot.TRINKET
				? "Adventurers can only use Generic gear (trinkets) until their class advancement."
				: "There's no " + slot.getDisplayName().toLowerCase() + " in storage that " + character.name + " can use.");
		}
		Runnable takeOff = equipped == null ? null : () ->
		{
			if (shownStorage.size() >= shownCapacity)
			{
				JOptionPane.showMessageDialog(this, "Storage is full. Salvage something first.", "Gear",
					JOptionPane.INFORMATION_MESSAGE);
				return;
			}
			onUnequip.accept(characterIndex, slot);
		};

		vaultPanel.startChoosing(prompt, fits, storageIndex -> onEquip.accept(characterIndex, storageIndex), takeOff,
			() -> tabs.select(followersTabButton));
		tabs.select(bankTabButton);
	}

	/**
	 * Show how many Skillers are hired and what the next costs. Must be called on the Swing thread.
	 */
	public void showSkillerHiring(int skillersHired, int nextCost, boolean canHire)
	{
		shownSkillerCost = nextCost;
		skillersLabel.setText("Skillers: " + skillersHired + "/" + Guild.MAX_SKILLERS);
		boolean full = skillersHired >= Guild.MAX_SKILLERS;
		skillerButton.setText(full ? "All Skillers hired" : String.format("Hire Skiller (%,d coins)", nextCost));
		skillerButton.setEnabled(canHire);
	}

	private void confirmHireSkiller()
	{
		int choice = JOptionPane.showConfirmDialog(
			this,
			String.format("Hire a new Skiller for %,d coins?", shownSkillerCost) + "\n\n"
				+ "Skillers don't need a Guild hire. They start at level 1 and, at level 10,\n"
				+ "can become a Harvester, Artisan or Explorer. Each Skiller costs double the last.",
			"Hire Skiller",
			JOptionPane.YES_NO_OPTION);
		if (choice == JOptionPane.YES_OPTION)
		{
			onHireSkiller.run();
		}
	}

	private void confirmHire()
	{
		int choice = JOptionPane.showConfirmDialog(
			this,
			"Hire a new Adventurer for " + Guild.HIRE_COST + " coins? This uses one of your hires.\n\n"
				+ "It starts at level 1 and gets 10% of your XP while another character is active.",
			"Hire",
			JOptionPane.YES_NO_OPTION);
		if (choice == JOptionPane.YES_OPTION)
		{
			onHire.run();
		}
	}
}
