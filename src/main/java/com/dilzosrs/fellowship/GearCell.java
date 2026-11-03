package com.dilzosrs.fellowship;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;

/**
 * A gear slot square with the item's name under it, and something to do when it's clicked.
 * Used for the slots on the Gear tab and the items in the Bank.
 */
public class GearCell extends JPanel
{
	private final GearType.Slot slot;
	private final GearSlot square;
	private final JLabel nameLabel = new JLabel();

	public GearCell(ItemManager itemManager, SpriteManager spriteManager, GearType.Slot slot, Runnable onClick)
	{
		this.slot = slot;
		setLayout(new BorderLayout(0, 2));
		setOpaque(false);
		square = new GearSlot(itemManager, spriteManager, slot);
		// The square keeps its own size, centred in the column.
		JPanel squareHolder = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		squareHolder.setOpaque(false);
		squareHolder.add(square);
		add(squareHolder, BorderLayout.NORTH);
		nameLabel.setFont(FontManager.getRunescapeSmallFont());
		nameLabel.setHorizontalAlignment(JLabel.CENTER);
		add(nameLabel, BorderLayout.CENTER);
		// A right-click menu (if one is set) shows over the square and the name too.
		squareHolder.setInheritsPopupMenu(true);
		square.setInheritsPopupMenu(true);
		nameLabel.setInheritsPopupMenu(true);
		setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				// Right-clicks are for the menu.
				if (SwingUtilities.isLeftMouseButton(e))
				{
					onClick.run();
				}
			}
		});
	}

	/**
	 * Show an item (or null for an empty slot) and the tooltip for clicking it.
	 * Must be called on the Swing thread.
	 */
	public void show(GearItem item, String tooltip)
	{
		square.showItem(item);
		// The name goes on two lines, split at its first space, since the columns are narrow.
		nameLabel.setText("<html><center>" + (item == null
			? "Empty<br>" + slot.getDisplayName()
			: twoLines(item.getName())) + "</center></html>");
		nameLabel.setForeground(item == null ? ColorScheme.LIGHT_GRAY_COLOR : item.getRarity().getColor());
		setToolTipText(tooltip);
	}

	/** e.g. "Mithril<br>sword", or "Staff<br>&nbsp;" so one-word names keep the same height. */
	private static String twoLines(String name)
	{
		int space = name.indexOf(' ');
		return space < 0 ? name + "<br>&nbsp;" : name.substring(0, space) + "<br>" + name.substring(space + 1);
	}

	/**
	 * Fade the square and name, e.g. for gear the follower can't use. Call after show(), since
	 * show() sets the name's colour. Must be called on the Swing thread.
	 */
	public void setDimmed(boolean dimmed)
	{
		square.setDimmed(dimmed);
		if (dimmed)
		{
			nameLabel.setForeground(ColorScheme.MEDIUM_GRAY_COLOR);
		}
	}
}
