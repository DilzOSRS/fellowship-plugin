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
import javax.swing.border.EmptyBorder;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.ui.components.ProgressBar;

/**
 * The Fellowship sidebar panel.
 */
public class FellowshipPanel extends PluginPanel
{
	private final Consumer<CharacterClass> onEvolve;
	private final Consumer<Expedition> onSendExpedition;
	private final Runnable onCollect;

	private final JLabel coinsLabel = new JLabel();
	private final JLabel nameLabel = new JLabel();
	private final JLabel levelLabel = new JLabel();
	private final ProgressBar xpBar = new ProgressBar();
	private final JLabel xpLabel = new JLabel();
	// One button per row, because the sidebar is too narrow for the class names side by side.
	private final JPanel evolveButtons = new JPanel(new GridLayout(0, 1, 0, 4));
	private final JLabel expeditionLabel = new JLabel();
	private final JPanel expeditionButtons = new JPanel(new GridLayout(0, 1, 0, 4));

	// Ticks every second while the character is away, to update the countdown.
	private final Timer countdownTimer = new Timer(1000, e -> updateCountdown());
	private Expedition shownExpedition;
	private Instant shownExpeditionEnd;
	/** What the last collected expedition gave, or null. */
	private String lastReward;

	/**
	 * @param onEvolve called on the Swing thread when the player confirms evolving into a class
	 * @param onSendExpedition called on the Swing thread when the player confirms sending the character away
	 * @param onCollect called on the Swing thread when the player collects a finished expedition
	 */
	public FellowshipPanel(Consumer<CharacterClass> onEvolve, Consumer<Expedition> onSendExpedition,
		Runnable onCollect)
	{
		this.onEvolve = onEvolve;
		this.onSendExpedition = onSendExpedition;
		this.onCollect = onCollect;

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
		add(titleRow, BorderLayout.NORTH);

		// One card per character. For now there is only one character.
		JPanel card = new JPanel(new BorderLayout(0, 6));
		card.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		card.setBorder(new EmptyBorder(8, 8, 8, 8));

		JPanel header = new JPanel(new BorderLayout());
		header.setOpaque(false);
		nameLabel.setFont(FontManager.getRunescapeBoldFont());
		nameLabel.setForeground(Color.WHITE);
		header.add(nameLabel, BorderLayout.WEST);
		header.add(levelLabel, BorderLayout.EAST);
		card.add(header, BorderLayout.NORTH);

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

		card.add(body, BorderLayout.CENTER);

		JPanel expeditionArea = new JPanel(new BorderLayout(0, 4));
		expeditionArea.setOpaque(false);
		expeditionArea.setBorder(new EmptyBorder(6, 0, 0, 0));
		expeditionArea.add(expeditionLabel, BorderLayout.NORTH);
		expeditionButtons.setOpaque(false);
		expeditionArea.add(expeditionButtons, BorderLayout.SOUTH);
		card.add(expeditionArea, BorderLayout.SOUTH);

		add(card, BorderLayout.CENTER);

		// Shown until an account's character is loaded.
		nameLabel.setText("Log in to begin");
	}

	/**
	 * Stop the countdown timer. Call when the plugin shuts down.
	 */
	public void stopTimers()
	{
		countdownTimer.stop();
	}

	/**
	 * Show the account's coin balance. Must be called on the Swing thread.
	 */
	public void showCoins(long coins)
	{
		coinsLabel.setText(String.format("%,d coins", coins));
	}

	/**
	 * Show a character's class, level, XP bar and expedition. Must be called on the Swing thread.
	 *
	 * @param expedition the expedition the character is on, or null when it's home
	 * @param expeditionEnd when that expedition finishes, or null when the character is home
	 */
	public void showCharacter(CharacterClass characterClass, long xp, boolean canEvolve,
		Expedition expedition, Instant expeditionEnd)
	{
		int level = LevelCurve.levelForXp(xp);
		int levelCap = characterClass.getLevelCap();
		nameLabel.setText(characterClass.getDisplayName());
		levelLabel.setText("Level " + level);
		showEvolveButtons(canEvolve ? characterClass : null);
		showExpedition(expedition, expeditionEnd);

		if (level >= levelCap)
		{
			xpBar.setMaximumValue(1);
			xpBar.setValue(1);
			xpBar.setCenterLabel("Max");
			xpLabel.setText(canEvolve
				? "Level cap reached. Choose a class:"
				: String.format("Max level (%,d XP)", xp));
			return;
		}

		long levelStart = LevelCurve.xpForLevel(level);
		long levelEnd = LevelCurve.xpForLevel(level + 1);
		xpBar.setMaximumValue((int) (levelEnd - levelStart));
		xpBar.setValue((int) (xp - levelStart));
		xpBar.setCenterLabel(xpBar.getPercentage() + "%");
		xpLabel.setText(String.format("%,d / %,d XP", xp - levelStart, levelEnd - levelStart));
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
			"Evolve into the " + newClass.getDisplayName() + " class?\n\n"
				+ "This is permanent, and your character restarts at level 1.",
			"Evolve",
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
		int choice = JOptionPane.showConfirmDialog(
			this,
			"Send your character to " + expedition.getDisplayName() + " for "
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
