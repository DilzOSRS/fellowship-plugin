package com.dilzosrs.fellowship;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.IntConsumer;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.PluginPanel;

/**
 * The Fellowship sidebar panel: the coin balance, one card per character, and recruiting.
 */
public class FellowshipPanel extends PluginPanel
{
	private final BiConsumer<Integer, CharacterClass> onEvolve;
	private final BiConsumer<Integer, Expedition> onSendExpedition;
	private final IntConsumer onCollect;
	private final IntConsumer onMakeActive;
	private final Runnable onHire;

	private final JLabel coinsLabel = new JLabel();
	private final JLabel guildLabel = new JLabel();
	private final JPanel cardsPanel = new JPanel();
	private final List<CharacterCard> cards = new ArrayList<>();
	private final JPanel hireArea = new JPanel(new BorderLayout(0, 4));
	private final JLabel hiresLabel = new JLabel();
	private final JButton hireButton = new JButton("Hire Adventurer (" + Guild.HIRE_COST + " coins)");

	/**
	 * The callbacks are called on the Swing thread once the player confirms the action.
	 * The Integer/int is the character's position in the team.
	 */
	public FellowshipPanel(BiConsumer<Integer, CharacterClass> onEvolve, BiConsumer<Integer, Expedition> onSendExpedition,
		IntConsumer onCollect, IntConsumer onMakeActive, Runnable onHire, VaultPanel vaultPanel)
	{
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
		cardsPanel.add(new JLabel("Log in to begin"));

		// The cards, then bundles and storage underneath. Storage stays hidden until an account is loaded.
		JPanel middle = new JPanel(new BorderLayout(0, 10));
		middle.setOpaque(false);
		middle.add(cardsPanel, BorderLayout.CENTER);
		vaultPanel.setVisible(false);
		middle.add(vaultPanel, BorderLayout.SOUTH);
		add(middle, BorderLayout.CENTER);

		hireArea.setOpaque(false);
		hiresLabel.setFont(FontManager.getRunescapeSmallFont());
		hiresLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
		hireArea.add(hiresLabel, BorderLayout.NORTH);
		hireButton.addActionListener(e -> confirmHire());
		hireArea.add(hireButton, BorderLayout.SOUTH);
		hireArea.setVisible(false);
		add(hireArea, BorderLayout.SOUTH);
	}

	/**
	 * Stop all countdown timers. Call when the plugin shuts down.
	 */
	public void stopTimers()
	{
		cards.forEach(CharacterCard::stopTimer);
	}

	/**
	 * Show the whole team, the coin balance, the Guild and hiring. Must be called on the Swing thread.
	 */
	public void showTeam(List<CharacterView> team, long coins, long guildXp, int hiresAvailable, boolean canHire)
	{
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

		hiresLabel.setText(hiresAvailable > 0
			? "Hires available: " + hiresAvailable
			: "Next hire at Guild level " + (guildLevel + 1));
		hireButton.setEnabled(canHire);
		hireArea.setVisible(true);

		cardsPanel.revalidate();
		cardsPanel.repaint();
	}

	/**
	 * Show what the last expedition gave on one character's card. Must be called on the Swing thread.
	 */
	public void showReward(int index, long xpGained, int coinsGained)
	{
		if (index < cards.size())
		{
			cards.get(index).showReward(xpGained, coinsGained);
		}
	}

	private void addCard()
	{
		int index = cards.size();
		CharacterCard card = new CharacterCard(
			newClass -> onEvolve.accept(index, newClass),
			expedition -> onSendExpedition.accept(index, expedition),
			() -> onCollect.accept(index),
			() -> onMakeActive.accept(index));
		card.setAlignmentX(LEFT_ALIGNMENT);
		if (index > 0)
		{
			cardsPanel.add(Box.createVerticalStrut(8));
		}
		cardsPanel.add(card);
		cards.add(card);
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
