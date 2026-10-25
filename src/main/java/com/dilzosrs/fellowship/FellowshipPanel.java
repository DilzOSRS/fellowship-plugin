package com.dilzosrs.fellowship;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.function.Consumer;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
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
	private final Consumer<CharacterClass> onEvolve;

	private final JLabel nameLabel = new JLabel();
	private final JLabel levelLabel = new JLabel();
	private final ProgressBar xpBar = new ProgressBar();
	private final JLabel xpLabel = new JLabel();
	// One button per row, because the sidebar is too narrow for the class names side by side.
	private final JPanel evolveButtons = new JPanel(new GridLayout(0, 1, 0, 4));

	/**
	 * @param onEvolve called on the Swing thread when the player confirms evolving into a class
	 */
	public FellowshipPanel(Consumer<CharacterClass> onEvolve)
	{
		this.onEvolve = onEvolve;

		setBorder(new EmptyBorder(10, 10, 10, 10));
		setLayout(new BorderLayout(0, 10));

		JLabel title = new JLabel("Fellowship");
		title.setFont(FontManager.getRunescapeBoldFont());
		title.setForeground(Color.WHITE);
		add(title, BorderLayout.NORTH);

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
		add(card, BorderLayout.CENTER);

		// Shown until an account's character is loaded.
		nameLabel.setText("Log in to begin");
	}

	/**
	 * Show a character's class, level and XP bar. Must be called on the Swing thread.
	 */
	public void showCharacter(CharacterClass characterClass, long xp, boolean canEvolve)
	{
		int level = LevelCurve.levelForXp(xp);
		int levelCap = characterClass.getLevelCap();
		nameLabel.setText(characterClass.getDisplayName());
		levelLabel.setText("Level " + level);
		showEvolveButtons(canEvolve ? characterClass : null);

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
}
