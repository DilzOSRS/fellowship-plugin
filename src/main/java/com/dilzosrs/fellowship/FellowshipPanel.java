package com.dilzosrs.fellowship;

import java.awt.BorderLayout;
import java.awt.Color;
import javax.swing.JLabel;
import javax.swing.border.EmptyBorder;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.PluginPanel;

/**
 * The Fellowship sidebar panel.
 */
public class FellowshipPanel extends PluginPanel
{
	private final JLabel sessionXpLabel = new JLabel();

	public FellowshipPanel()
	{
		setBorder(new EmptyBorder(10, 10, 10, 10));
		setLayout(new BorderLayout());

		JLabel title = new JLabel("Fellowship");
		title.setFont(FontManager.getRunescapeBoldFont());
		title.setForeground(Color.WHITE);
		add(title, BorderLayout.NORTH);

		add(sessionXpLabel, BorderLayout.CENTER);
		setSessionXp(0);
	}

	/**
	 * Must be called on the Swing thread.
	 */
	public void setSessionXp(long xp)
	{
		sessionXpLabel.setText(String.format("XP gained this session: %,d", xp));
	}
}
