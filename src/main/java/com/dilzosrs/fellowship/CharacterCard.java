package com.dilzosrs.fellowship;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
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
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.components.ProgressBar;

/**
 * One character's card in the sidebar: name, level, XP bar, evolving and expeditions.
 */
public class CharacterCard extends JPanel
{
	private final Consumer<CharacterClass> onEvolve;
	private final Consumer<Expedition> onSendExpedition;
	private final Runnable onCollect;

	// The active card gets a green stripe down its left side; the others get plain padding.
	private static final Border ACTIVE_BORDER = new CompoundBorder(
		new MatteBorder(0, 3, 0, 0, ColorScheme.PROGRESS_COMPLETE_COLOR), new EmptyBorder(8, 5, 8, 8));
	private static final Border INACTIVE_BORDER = new EmptyBorder(8, 8, 8, 8);

	private final JLabel nameLabel = new JLabel();
	private final JLabel levelLabel = new JLabel();
	private final JLabel roleLabel = new JLabel();
	private final JButton makeActiveButton = new JButton("Make active");
	private final ProgressBar xpBar = new ProgressBar();
	private final JLabel xpLabel = new JLabel();
	// One button per row, because the sidebar is too narrow for the class names side by side.
	private final JPanel evolveButtons = new JPanel(new GridLayout(0, 1, 0, 4));
	private final JLabel statsLabel = new JLabel();
	private final JPanel gearRows = new JPanel(new GridLayout(0, 1, 0, 2));
	private final JLabel expeditionLabel = new JLabel();
	private final JPanel expeditionButtons = new JPanel(new GridLayout(0, 1, 0, 4));
	private final Consumer<GearType.Slot> onChangeGear;

	// Ticks every second while the character is away, to update the countdown.
	private final Timer countdownTimer = new Timer(1000, e -> updateCountdown());
	private String shownName;
	private Expedition shownExpedition;
	private Instant shownExpeditionEnd;
	/** What the last collected expedition gave, or null. */
	private String lastReward;

	/**
	 * The callbacks are called on the Swing thread once the player confirms the action.
	 */
	public CharacterCard(Consumer<CharacterClass> onEvolve, Consumer<Expedition> onSendExpedition, Runnable onCollect,
		Runnable onMakeActive, Consumer<GearType.Slot> onChangeGear)
	{
		this.onEvolve = onEvolve;
		this.onSendExpedition = onSendExpedition;
		this.onCollect = onCollect;
		this.onChangeGear = onChangeGear;

		setLayout(new BorderLayout(0, 6));
		setBackground(ColorScheme.DARKER_GRAY_COLOR);
		setBorder(INACTIVE_BORDER);

		JPanel header = new JPanel(new BorderLayout());
		header.setOpaque(false);
		nameLabel.setFont(FontManager.getRunescapeBoldFont());
		nameLabel.setForeground(Color.WHITE);
		header.add(nameLabel, BorderLayout.WEST);
		header.add(levelLabel, BorderLayout.EAST);

		JPanel roleRow = new JPanel(new BorderLayout(0, 4));
		roleRow.setOpaque(false);
		roleLabel.setFont(FontManager.getRunescapeSmallFont());
		roleRow.add(roleLabel, BorderLayout.NORTH);
		// Switching is free and easy to undo, so there's no confirm step.
		makeActiveButton.addActionListener(e -> onMakeActive.run());
		roleRow.add(makeActiveButton, BorderLayout.SOUTH);
		header.add(roleRow, BorderLayout.SOUTH);
		add(header, BorderLayout.NORTH);

		JPanel body = new JPanel(new BorderLayout(0, 4));
		body.setOpaque(false);

		xpBar.setPreferredSize(new Dimension(0, 16));
		xpBar.setBackground(ColorScheme.DARK_GRAY_COLOR);
		xpBar.setForeground(ColorScheme.PROGRESS_COMPLETE_COLOR);
		body.add(xpBar, BorderLayout.NORTH);

		// The XP numbers go under the bar, because the bar's own labels are too narrow for them.
		xpLabel.setFont(FontManager.getRunescapeSmallFont());
		body.add(xpLabel, BorderLayout.CENTER);

		// Only shown when the character can evolve.
		evolveButtons.setOpaque(false);
		evolveButtons.setVisible(false);
		body.add(evolveButtons, BorderLayout.SOUTH);

		add(body, BorderLayout.CENTER);

		JPanel gearArea = new JPanel(new BorderLayout(0, 4));
		gearArea.setOpaque(false);
		gearArea.setBorder(new EmptyBorder(6, 0, 0, 0));
		statsLabel.setForeground(Color.WHITE);
		gearArea.add(statsLabel, BorderLayout.NORTH);
		gearRows.setOpaque(false);
		gearArea.add(gearRows, BorderLayout.CENTER);

		JPanel expeditionArea = new JPanel(new BorderLayout(0, 4));
		expeditionArea.setOpaque(false);
		expeditionArea.setBorder(new EmptyBorder(6, 0, 0, 0));
		expeditionArea.add(expeditionLabel, BorderLayout.NORTH);
		expeditionButtons.setOpaque(false);
		expeditionArea.add(expeditionButtons, BorderLayout.SOUTH);

		JPanel bottom = new JPanel(new BorderLayout());
		bottom.setOpaque(false);
		bottom.add(gearArea, BorderLayout.NORTH);
		bottom.add(expeditionArea, BorderLayout.SOUTH);
		add(bottom, BorderLayout.SOUTH);
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
		nameLabel.setText(view.name);
		levelLabel.setText("Level " + level);
		if (view.expedition != null)
		{
			roleLabel.setText(view.active ? "Active, but away: no XP" : "Away: no XP");
		}
		else
		{
			roleLabel.setText(view.active ? "Active: 100% XP" : "Inactive: 10% XP");
		}
		roleLabel.setForeground(view.active ? ColorScheme.PROGRESS_COMPLETE_COLOR : ColorScheme.LIGHT_GRAY_COLOR);
		makeActiveButton.setVisible(!view.active);
		setBorder(view.active ? ACTIVE_BORDER : INACTIVE_BORDER);
		showEvolveButtons(view.canEvolve ? view.characterClass : null);
		showGear(view);
		showExpedition(view.expedition, view.expeditionEnd);

		if (level >= levelCap)
		{
			xpBar.setMaximumValue(1);
			xpBar.setValue(1);
			xpBar.setCenterLabel("Max");
			xpLabel.setText(view.canEvolve
				? "<html>Class advancement ready!<br>Choose a path:</html>"
				: String.format("Max level (%,d XP)", view.xp));
			return;
		}

		long levelStart = LevelCurve.xpForLevel(level);
		long levelEnd = LevelCurve.xpForLevel(level + 1);
		xpBar.setMaximumValue((int) (levelEnd - levelStart));
		xpBar.setValue((int) (view.xp - levelStart));
		xpBar.setCenterLabel(xpBar.getPercentage() + "%");
		xpLabel.setText(String.format("%,d / %,d XP", view.xp - levelStart, levelEnd - levelStart));
	}

	/**
	 * Show what the last expedition gave. Must be called on the Swing thread, before the
	 * character is shown at home again.
	 */
	public void showReward(long xpGained, int coinsGained)
	{
		lastReward = xpGained > 0
			? String.format("Back with +%,d XP<br>and +%d coins!", xpGained, coinsGained)
			// An Adventurer at the level cap wastes the XP, like any other XP.
			: String.format("Back with +%d coins!<br>(No XP: level cap reached.)", coinsGained);
	}

	/**
	 * Show that the last expedition failed. Must be called on the Swing thread, before the
	 * character is shown at home again.
	 */
	public void showFailure(Expedition expedition)
	{
		lastReward = "Failed to clear<br>" + expedition.getDisplayName() + ".<br>No rewards.";
	}

	/**
	 * Show the character's stats and one row per gear slot, each with a button to change it.
	 */
	private void showGear(CharacterView view)
	{
		statsLabel.setText("ATK " + view.atk + "   DEF " + view.def + "   MAG " + view.mag);
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
				JButton button = new JButton(newClass.getDisplayName());
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
		int choice = JOptionPane.showConfirmDialog(
			this,
			"Train " + shownName + " in the ways of the " + newClass.getDisplayName() + "?\n\n"
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

		expeditionButtons.removeAll();
		if (expedition == null)
		{
			countdownTimer.stop();
			expeditionLabel.setText(lastReward == null
				? "Send on an expedition:"
				: "<html>" + lastReward + "<br><br>Send on an expedition:</html>");
			for (Expedition option : Expedition.values())
			{
				// The name gets its own line, because it's too long to fit inside a button.
				JPanel row = new JPanel(new BorderLayout(0, 2));
				row.setOpaque(false);
				JLabel name = new JLabel(option.getDisplayName());
				name.setForeground(Color.WHITE);
				row.add(name, BorderLayout.NORTH);
				// Every row gets an info line, so all rows are the same height.
				JLabel info = new JLabel(
					(option.getThreatLevel() > 0 ? "Threat " + option.getThreatLevel() : "Safe") + ", "
						+ (option.getPartySize() > 1 ? "party of " + option.getPartySize() : "solo"));
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
	}

	private void updateCountdown()
	{
		if (shownExpedition == null)
		{
			return;
		}
		Duration left = Duration.between(Instant.now(), shownExpeditionEnd);
		if (left.isNegative() || left.isZero())
		{
			countdownTimer.stop();
			expeditionLabel.setText("<html>Back from<br>" + shownExpedition.getDisplayName() + "!</html>");
			if (expeditionButtons.getComponentCount() == 0)
			{
				JButton collect = new JButton("Collect");
				collect.addActionListener(e -> onCollect.run());
				expeditionButtons.add(collect);
				expeditionButtons.setVisible(true);
				expeditionButtons.revalidate();
				expeditionButtons.repaint();
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
