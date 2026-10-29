package com.dilzosrs.fellowship;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.time.Duration;
import java.time.Instant;
import java.util.function.Consumer;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.components.ProgressBar;
import net.runelite.client.ui.components.materialtabs.MaterialTab;
import net.runelite.client.ui.components.materialtabs.MaterialTabGroup;

/**
 * One character's card in the sidebar. A header that's always visible (class icon, name, level,
 * status, and the Make active / Collect buttons), then three tabs: Info (XP, class advancement and
 * stats), Gear (stats and gear slots) and Expedition.
 */
public class CharacterCard extends JPanel
{
	private final ItemManager itemManager;
	private final Consumer<CharacterClass> onEvolve;
	private final Consumer<Expedition> onSendExpedition;
	private final Consumer<GearType.Slot> onChangeGear;

	// The active card gets a green stripe down its left side; the others get plain padding.
	private static final Border ACTIVE_BORDER = new CompoundBorder(
		new MatteBorder(0, 3, 0, 0, ColorScheme.PROGRESS_COMPLETE_COLOR), new EmptyBorder(8, 5, 8, 8));
	private static final Border INACTIVE_BORDER = new EmptyBorder(8, 8, 8, 8);

	/** Width and height of the class icon box. Item icons are 36x32, so this leaves a small margin. */
	private static final int ICON_SIZE = 40;
	private static final int HEADER_XP_BAR_HEIGHT = 4;

	private final JLabel iconLabel = new JLabel();
	private final JLabel nameLabel = new JLabel();
	private final JLabel levelLabel = new JLabel();
	private final JLabel roleLabel = new JLabel();
	// Make active and Collect live in the header, so they're on hand whichever tab is open.
	private final JPanel headerButtons = new JPanel(new GridLayout(1, 0, 4, 0));
	private final JButton makeActiveButton = new JButton("Make active");
	private final JButton collectButton = new JButton("Collect");
	private final ProgressBar xpBar = new ProgressBar();
	// A thin copy of the XP bar in the header, for the tabs that don't show the big one.
	private final ProgressBar headerXpBar = new ProgressBar();
	private final JLabel xpLabel = new JLabel();
	// One button per row, because the sidebar is too narrow for the class names side by side.
	private final JPanel evolveButtons = new JPanel(new GridLayout(0, 1, 0, 4));
	// The stats show on both the Info and the Gear tab, and a label can only be in one place.
	private final JLabel infoStatsLabel = new JLabel();
	private final JLabel gearStatsLabel = new JLabel();
	private final JPanel gearRows = new JPanel(new GridLayout(0, 1, 0, 2));
	private final JLabel expeditionLabel = new JLabel();
	private final JPanel expeditionButtons = new JPanel(new GridLayout(0, 1, 0, 4));

	// Ticks every second while the character is away, to update the countdown.
	private final Timer countdownTimer = new Timer(1000, e -> updateCountdown());
	private String shownName;
	/** The class whose icon is showing, so the icon is only loaded again when the class changes. */
	private CharacterClass shownClass;
	private boolean shownActive;
	private Expedition shownExpedition;
	private Instant shownExpeditionEnd;
	/** True once the shown expedition's time is up and the rewards are waiting. */
	private boolean expeditionDone;
	/** When the shown character's injury heals, or null if it isn't resting. */
	private Instant shownRestingUntil;
	/** What the last collected expedition gave, or null. */
	private String lastReward;

	/**
	 * The callbacks are called on the Swing thread once the player confirms the action.
	 */
	public CharacterCard(ItemManager itemManager, Consumer<CharacterClass> onEvolve, Consumer<Expedition> onSendExpedition,
		Runnable onCollect, Runnable onMakeActive, Consumer<GearType.Slot> onChangeGear)
	{
		this.itemManager = itemManager;
		this.onEvolve = onEvolve;
		this.onSendExpedition = onSendExpedition;
		this.onChangeGear = onChangeGear;

		setLayout(new BorderLayout(0, 6));
		setBackground(ColorScheme.DARKER_GRAY_COLOR);
		setBorder(INACTIVE_BORDER);

		// Header: the class icon, with the name, level and status beside it.
		iconLabel.setPreferredSize(new Dimension(ICON_SIZE, ICON_SIZE));
		iconLabel.setHorizontalAlignment(JLabel.CENTER);
		iconLabel.setOpaque(true);
		iconLabel.setBackground(ColorScheme.DARK_GRAY_COLOR);

		JPanel nameRow = new JPanel(new BorderLayout());
		nameRow.setOpaque(false);
		nameLabel.setFont(FontManager.getRunescapeBoldFont());
		nameLabel.setForeground(Color.WHITE);
		nameRow.add(nameLabel, BorderLayout.WEST);
		nameRow.add(levelLabel, BorderLayout.EAST);

		JPanel identity = new JPanel(new BorderLayout(0, 2));
		identity.setOpaque(false);
		identity.add(nameRow, BorderLayout.NORTH);
		roleLabel.setFont(FontManager.getRunescapeSmallFont());
		identity.add(roleLabel, BorderLayout.CENTER);
		// The thin XP bar sits in a holder of fixed height, so hiding it on the Info tab
		// leaves its space behind and the card doesn't jump when switching tabs.
		headerXpBar.setBackground(ColorScheme.DARK_GRAY_COLOR);
		headerXpBar.setForeground(ColorScheme.PROGRESS_COMPLETE_COLOR);
		JPanel headerXpBarHolder = new JPanel(new BorderLayout());
		headerXpBarHolder.setOpaque(false);
		headerXpBarHolder.setPreferredSize(new Dimension(0, HEADER_XP_BAR_HEIGHT));
		headerXpBarHolder.add(headerXpBar, BorderLayout.CENTER);
		identity.add(headerXpBarHolder, BorderLayout.SOUTH);

		JPanel header = new JPanel(new BorderLayout(8, 6));
		header.setOpaque(false);
		header.add(iconLabel, BorderLayout.WEST);
		header.add(identity, BorderLayout.CENTER);
		headerButtons.setOpaque(false);
		// Switching is free and easy to undo, so there's no confirm step.
		makeActiveButton.addActionListener(e -> onMakeActive.run());
		collectButton.addActionListener(e -> onCollect.run());
		header.add(headerButtons, BorderLayout.SOUTH);
		add(header, BorderLayout.NORTH);

		// Info tab: the XP bar, then class advancement and stats underneath.
		JPanel infoTab = new JPanel(new BorderLayout(0, 4));
		infoTab.setOpaque(false);
		xpBar.setPreferredSize(new Dimension(0, 16));
		xpBar.setBackground(ColorScheme.DARK_GRAY_COLOR);
		xpBar.setForeground(ColorScheme.PROGRESS_COMPLETE_COLOR);
		infoTab.add(xpBar, BorderLayout.NORTH);
		// The XP numbers go under the bar, because the bar's own labels are too narrow for them.
		xpLabel.setFont(FontManager.getRunescapeSmallFont());
		infoTab.add(xpLabel, BorderLayout.CENTER);
		JPanel infoBottom = new JPanel(new BorderLayout(0, 6));
		infoBottom.setOpaque(false);
		// Only shown when the character can evolve.
		evolveButtons.setOpaque(false);
		evolveButtons.setVisible(false);
		infoBottom.add(evolveButtons, BorderLayout.NORTH);
		infoStatsLabel.setForeground(Color.WHITE);
		infoBottom.add(infoStatsLabel, BorderLayout.SOUTH);
		infoTab.add(infoBottom, BorderLayout.SOUTH);

		// Gear tab: the stats, then one row per slot.
		JPanel gearTab = new JPanel(new BorderLayout(0, 4));
		gearTab.setOpaque(false);
		gearStatsLabel.setForeground(Color.WHITE);
		gearTab.add(gearStatsLabel, BorderLayout.NORTH);
		gearRows.setOpaque(false);
		gearTab.add(gearRows, BorderLayout.CENTER);

		// Expedition tab: where the character is, or the expeditions it can go on.
		JPanel expeditionTab = new JPanel(new BorderLayout(0, 4));
		expeditionTab.setOpaque(false);
		expeditionTab.add(expeditionLabel, BorderLayout.NORTH);
		expeditionButtons.setOpaque(false);
		expeditionTab.add(expeditionButtons, BorderLayout.CENTER);

		// The tab group swaps the selected tab's content into this panel.
		JPanel tabContent = new JPanel();
		tabContent.setOpaque(false);
		MaterialTabGroup tabs = new MaterialTabGroup(tabContent);
		// Tighter gaps than the default, so all three tabs fit on one line in the narrow sidebar.
		tabs.setLayout(new FlowLayout(FlowLayout.CENTER, 2, 0));
		MaterialTab infoTabButton = new MaterialTab("Info", tabs, infoTab);
		MaterialTab gearTabButton = new MaterialTab("Gear", tabs, gearTab);
		MaterialTab expeditionTabButton = new MaterialTab("Expedition", tabs, expeditionTab);
		// The Info tab has the big XP bar, so the thin one only shows on the other tabs.
		infoTabButton.setOnSelectEvent(() -> showHeaderXpBar(false));
		gearTabButton.setOnSelectEvent(() -> showHeaderXpBar(true));
		expeditionTabButton.setOnSelectEvent(() -> showHeaderXpBar(true));
		tabs.addTab(infoTabButton);
		tabs.addTab(gearTabButton);
		tabs.addTab(expeditionTabButton);
		tabs.select(infoTabButton);

		JPanel tabArea = new JPanel(new BorderLayout(0, 6));
		tabArea.setOpaque(false);
		tabArea.add(tabs, BorderLayout.NORTH);
		tabArea.add(tabContent, BorderLayout.CENTER);
		add(tabArea, BorderLayout.CENTER);
	}

	/** Show or hide the thin header XP bar. Returns true so the tab switch goes ahead. */
	private boolean showHeaderXpBar(boolean visible)
	{
		headerXpBar.setVisible(visible);
		return true;
	}

	/**
	 * Stop the countdown timer. Call when the card is thrown away or the plugin shuts down.
	 */
	public void stopTimer()
	{
		countdownTimer.stop();
	}

	/**
	 * Show a character's name, level, XP bar and expedition. Must be called on the Swing thread.
	 */
	public void show(CharacterView view)
	{
		int level = LevelCurve.levelForXp(view.xp);
		int levelCap = view.characterClass.getLevelCap();
		shownName = view.name;
		shownActive = view.active;
		if (view.characterClass != shownClass)
		{
			shownClass = view.characterClass;
			// The icon loads in the background, and the label repaints itself once it's ready.
			itemManager.getImage(view.characterClass.getIconItemId()).addTo(iconLabel);
		}
		nameLabel.setText(view.name);
		levelLabel.setText("Lvl " + level);
		if (view.expedition != null)
		{
			roleLabel.setText(view.active ? "Active, but away: no XP" : "Away: no XP");
		}
		else if (view.restingUntil != null)
		{
			roleLabel.setText(view.active ? "Active, but injured: no XP" : "Injured: no XP");
		}
		else
		{
			roleLabel.setText(view.active ? "Active: 100% XP" : "Inactive: 10% XP");
		}
		roleLabel.setForeground(view.restingUntil != null ? ColorScheme.PROGRESS_ERROR_COLOR
			: view.active ? ColorScheme.PROGRESS_COMPLETE_COLOR : ColorScheme.LIGHT_GRAY_COLOR);
		setBorder(view.active ? ACTIVE_BORDER : INACTIVE_BORDER);
		showEvolveButtons(view.canEvolve ? view.characterClass : null);
		showGear(view);
		shownRestingUntil = view.restingUntil;
		showExpedition(view.expedition, view.expeditionEnd);

		if (level >= levelCap)
		{
			xpBar.setMaximumValue(1);
			xpBar.setValue(1);
			xpBar.setCenterLabel("Max");
			headerXpBar.setMaximumValue(1);
			headerXpBar.setValue(1);
			xpLabel.setText(view.canEvolve
				? "<html>Class advancement ready!<br>"
					+ (view.characterClass == CharacterClass.ACOLYTE ? "Choose your god:" : "Choose a path:") + "</html>"
				: String.format("Max level (%,d XP)", view.xp));
			return;
		}

		long levelStart = LevelCurve.xpForLevel(level);
		long levelEnd = LevelCurve.xpForLevel(level + 1);
		int levelSize = (int) (levelEnd - levelStart);
		int progress = (int) (view.xp - levelStart);
		xpBar.setMaximumValue(levelSize);
		xpBar.setValue(progress);
		xpBar.setCenterLabel(xpBar.getPercentage() + "%");
		headerXpBar.setMaximumValue(levelSize);
		headerXpBar.setValue(progress);
		xpLabel.setText(String.format("%,d / %,d XP", progress, levelSize));
	}

	/**
	 * Show what the last expedition gave. Must be called on the Swing thread, before the
	 * character is shown at home again.
	 */
	public void showReward(long xpGained, String loot)
	{
		if (loot == null)
		{
			lastReward = xpGained > 0 ? String.format("Back with +%,d XP!", xpGained) : "Back!";
			return;
		}
		lastReward = xpGained > 0
			? String.format("Back with +%,d XP<br>and %s!", xpGained, loot)
			// An Adventurer at the level cap wastes the XP, like any other XP.
			: "Back with " + loot + "!<br>(No XP: level cap reached.)";
	}

	/**
	 * Show that the last expedition failed. Must be called on the Swing thread, before the
	 * character is shown at home again.
	 */
	public void showFailure(Expedition expedition)
	{
		lastReward = "Failed to clear<br>" + expedition.getDisplayName() + ".<br>No rewards.";
	}

	/** Show only the header buttons that apply right now, or hide the row if none do. */
	private void updateHeaderButtons()
	{
		headerButtons.removeAll();
		if (expeditionDone)
		{
			headerButtons.add(collectButton);
		}
		if (!shownActive)
		{
			headerButtons.add(makeActiveButton);
		}
		headerButtons.setVisible(headerButtons.getComponentCount() > 0);
		headerButtons.revalidate();
		headerButtons.repaint();
	}

	/**
	 * Show the character's stats and one row per gear slot, each with a button to change it.
	 */
	private void showGear(CharacterView view)
	{
		String stats = "ATK " + view.atk + "   DEF " + view.def + "   MAG " + view.mag;
		Blessing blessing = view.characterClass.getBlessing();
		infoStatsLabel.setText(blessing == null ? stats
			: "<html>" + stats + "<br>Blessing of " + blessing.getGodName() + ":<br>"
				+ blessing.describe() + " on every expedition</html>");
		gearStatsLabel.setText(stats);
		gearRows.removeAll();
		for (GearType.Slot slot : GearType.Slot.values())
		{
			GearItem item = view.getEquipped(slot);
			JPanel row = new JPanel(new BorderLayout());
			row.setOpaque(false);
			JLabel label = new JLabel(item == null ? "No " + slotName(slot) : item.getName());
			label.setFont(FontManager.getRunescapeSmallFont());
			label.setForeground(item == null ? ColorScheme.LIGHT_GRAY_COLOR : Color.WHITE);
			row.add(label, BorderLayout.CENTER);
			JButton change = new JButton(item == null ? "Equip" : "Change");
			change.addActionListener(e -> onChangeGear.accept(slot));
			row.add(change, BorderLayout.EAST);
			gearRows.add(row);
		}
		gearRows.revalidate();
		gearRows.repaint();
	}

	/** e.g. "weapon", for "No weapon". */
	static String slotName(GearType.Slot slot)
	{
		return slot.name().toLowerCase();
	}

	/**
	 * Show one button per class the character can evolve into, or hide them if evolvingFrom is null.
	 */
	private void showEvolveButtons(CharacterClass evolvingFrom)
	{
		evolveButtons.removeAll();
		if (evolvingFrom != null)
		{
			for (CharacterClass newClass : evolvingFrom.getEvolutions())
			{
				Blessing blessing = newClass.getBlessing();
				JButton button = new JButton(blessing != null ? blessing.getGodName() : newClass.getDisplayName());
				button.addActionListener(e -> confirmEvolve(newClass));
				evolveButtons.add(button);
			}
		}
		evolveButtons.setVisible(evolvingFrom != null);
		evolveButtons.revalidate();
		evolveButtons.repaint();
	}

	private void confirmEvolve(CharacterClass newClass)
	{
		Blessing blessing = newClass.getBlessing();
		int choice = JOptionPane.showConfirmDialog(
			this,
			(blessing != null
				? shownName + " will worship " + blessing.getGodName() + ", giving "
					+ blessing.describe() + " to everyone you send on expeditions.\n\n"
				: "Train " + shownName + " in the ways of the " + newClass.getDisplayName() + "?\n\n")
				+ "This is permanent, and the character restarts at level 1.",
			"Class advancement",
			JOptionPane.YES_NO_OPTION);
		if (choice == JOptionPane.YES_OPTION)
		{
			onEvolve.accept(newClass);
		}
	}

	/**
	 * Show the expeditions the character can go on, or a countdown if it's away.
	 */
	private void showExpedition(Expedition expedition, Instant expeditionEnd)
	{
		shownExpedition = expedition;
		shownExpeditionEnd = expeditionEnd;
		expeditionDone = false;

		expeditionButtons.removeAll();
		if (expedition == null && shownRestingUntil != null)
		{
			// Injured: no expeditions until healed. The timer counts down the rest.
			expeditionButtons.setVisible(false);
			countdownTimer.start();
			updateCountdown();
		}
		else if (expedition == null)
		{
			countdownTimer.stop();
			expeditionLabel.setText(lastReward == null
				? "Send on an expedition:"
				: "<html>" + lastReward + "<br><br>Send on an expedition:</html>");
			for (Expedition option : Expedition.values())
			{
				if (!option.allows(shownClass))
				{
					continue;
				}
				// The name gets its own line, because it's too long to fit inside a button.
				JPanel row = new JPanel(new BorderLayout(0, 2));
				row.setOpaque(false);
				JLabel name = new JLabel(option.getDisplayName());
				name.setForeground(Color.WHITE);
				row.add(name, BorderLayout.NORTH);
				// Every row gets an info line, so all rows are the same height.
				JLabel info = new JLabel(
					(option.getThreatLevel() > 0 ? "Threat " + option.getThreatLevel() : "Safe") + ", "
						+ (option.getPartySize() > 1 ? "party of " + option.getPartySize() : "solo")
						+ (option.getCost() > 0 ? ", " + option.getCost() + " coins" : ""));
				info.setFont(FontManager.getRunescapeSmallFont());
				info.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
				row.add(info, BorderLayout.CENTER);
				JButton button = new JButton("Send (" + formatDuration(option.getDuration()) + ")");
				button.addActionListener(e -> confirmSend(option));
				row.add(button, BorderLayout.SOUTH);
				expeditionButtons.add(row);
			}
			expeditionButtons.setVisible(true);
		}
		else
		{
			// The reward message is only shown until the next expedition starts.
			lastReward = null;
			expeditionButtons.setVisible(false);
			countdownTimer.start();
			updateCountdown();
		}
		expeditionButtons.revalidate();
		expeditionButtons.repaint();
		updateHeaderButtons();
	}

	private void updateCountdown()
	{
		if (shownExpedition == null)
		{
			if (shownRestingUntil != null)
			{
				Duration rest = Duration.between(Instant.now(), shownRestingUntil);
				if (rest.isNegative() || rest.isZero())
				{
					// Healed: the plugin announces it and refreshes the card with the Send buttons.
					countdownTimer.stop();
					expeditionLabel.setText("Recovered!");
					return;
				}
				expeditionLabel.setText("<html>" + (lastReward == null ? "" : lastReward + "<br><br>")
					+ "Injured and resting.<br>Healed in " + formatCountdown(rest) + "</html>");
			}
			return;
		}
		Duration left = Duration.between(Instant.now(), shownExpeditionEnd);
		if (left.isNegative() || left.isZero())
		{
			countdownTimer.stop();
			expeditionLabel.setText("<html>Back from<br>" + shownExpedition.getDisplayName() + "!</html>");
			roleLabel.setText("Back! Rewards waiting");
			roleLabel.setForeground(ColorScheme.PROGRESS_COMPLETE_COLOR);
			if (!expeditionDone)
			{
				expeditionDone = true;
				updateHeaderButtons();
			}
			return;
		}
		// Explicit line breaks: letting Swing wrap long HTML text cuts off the last line.
		expeditionLabel.setText("<html>Away on an expedition:<br>" + shownExpedition.getDisplayName()
			+ "<br>Back in " + formatCountdown(left) + "</html>");
	}

	private void confirmSend(Expedition expedition)
	{
		if (expedition.getPartySize() > 1)
		{
			// The panel shows a party picker instead, which has its own confirm step.
			onSendExpedition.accept(expedition);
			return;
		}
		int choice = JOptionPane.showConfirmDialog(
			this,
			"Send " + shownName + " to " + expedition.getDisplayName() + " for "
				+ formatDuration(expedition.getDuration()) + "?\n\n"
				+ (expedition.getCost() > 0 ? "It costs " + expedition.getCost() + " coins.\n" : "")
				+ "It won't earn XP from your OSRS play while it's away.",
			"Expedition",
			JOptionPane.YES_NO_OPTION);
		if (choice == JOptionPane.YES_OPTION)
		{
			onSendExpedition.accept(expedition);
		}
	}

	/** e.g. "30 min" or "2h 30m". */
	private static String formatDuration(Duration duration)
	{
		long hours = duration.toHours();
		long minutes = duration.toMinutesPart();
		if (hours == 0)
		{
			return minutes + " min";
		}
		return minutes == 0 ? hours + "h" : hours + "h " + minutes + "m";
	}

	/** e.g. "29:41", or "1:05:00" when an hour or more is left. */
	private static String formatCountdown(Duration left)
	{
		long hours = left.toHours();
		int minutes = left.toMinutesPart();
		int seconds = left.toSecondsPart();
		return hours > 0
			? String.format("%d:%02d:%02d", hours, minutes, seconds)
			: String.format("%d:%02d", minutes, seconds);
	}
}
