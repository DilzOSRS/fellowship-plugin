package com.dilzosrs.fellowship;

import java.awt.BorderLayout;
import java.awt.Color;
import javax.swing.JLabel;
import javax.swing.border.EmptyBorder;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.PluginPanel;

/**
 * The Fellowship sidebar panel. For now it only shows a title.
 */
public class FellowshipPanel extends PluginPanel
{
	public FellowshipPanel()
	{
		setBorder(new EmptyBorder(10, 10, 10, 10));
		setLayout(new BorderLayout());

		JLabel title = new JLabel("Fellowship");
		title.setFont(FontManager.getRunescapeBoldFont());
		title.setForeground(Color.WHITE);
		add(title, BorderLayout.NORTH);

		JLabel placeholder = new JLabel("Your Adventurer will appear here.");
		add(placeholder, BorderLayout.CENTER);
	}
}
