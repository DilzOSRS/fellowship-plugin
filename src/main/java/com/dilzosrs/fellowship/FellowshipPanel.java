package com.dilzosrs.fellowship;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import javax.swing.JLabel;
import javax.swing.JPanel;
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
	private final JLabel nameLabel = new JLabel();
	private final JLabel levelLabel = new JLabel();
	private final ProgressBar xpBar = new ProgressBar();
	private final JLabel xpLabel = new JLabel();

	public FellowshipPanel()
	{
		setBorder(new EmptyBorder(10, 10, 10, 10));
		setLayout(new BorderLayout(0, 10));

		JLabel title = new JLabel("Fellowship");
		title.setFont(FontManager.getRunescapeBoldFont());
		title.setForeground(Color.WHITE);
		add(title, BorderLayout.NORTH);

		// One card per character. For now there is only the Adventurer.
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

		xpBar.setPreferredSize(new Dimension(0, 16));
		xpBar.setBackground(ColorScheme.DARK_GRAY_COLOR);
		xpBar.setForeground(ColorScheme.PROGRESS_COMPLETE_COLOR);
		card.add(xpBar, BorderLayout.CENTER);

		// The XP numbers go under the bar, because the bar's own labels are too narrow for them.
		xpLabel.setFont(FontManager.getRunescapeSmallFont());
		card.add(xpLabel, BorderLayout.SOUTH);

		add(card, BorderLayout.CENTER);
	}

	/**
	 * Show a character's name, level and XP bar. Must be called on the Swing thread.
	 */
	public void showAdventurer(String name, long xp)
	{
		int level = LevelCurve.levelForXp(xp);
		nameLabel.setText(name);
		levelLabel.setText("Level " + level);

		if (level >= LevelCurve.MAX_LEVEL)
		{
			xpBar.setMaximumValue(1);
			xpBar.setValue(1);
			xpBar.setCenterLabel("Max");
			xpLabel.setText(String.format("Max level (%,d XP)", xp));
			return;
		}

		long levelStart = LevelCurve.xpForLevel(level);
		long levelEnd = LevelCurve.xpForLevel(level + 1);
		xpBar.setMaximumValue((int) (levelEnd - levelStart));
		xpBar.setValue((int) (xp - levelStart));
		xpBar.setCenterLabel(xpBar.getPercentage() + "%");
		xpLabel.setText(String.format("%,d / %,d XP", xp - levelStart, levelEnd - levelStart));
	}
}
