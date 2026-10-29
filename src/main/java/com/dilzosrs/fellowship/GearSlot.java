package com.dilzosrs.fellowship;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import net.runelite.api.gameval.SpriteID;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.util.AsyncBufferedImage;

/**
 * One gear slot square, like OSRS's equipment tab: the grey slot tile with the item's icon on it,
 * or the slot's faded outline when it's empty. The border shows the item's rarity colour.
 */
public class GearSlot extends JPanel
{
	/** The OSRS slot tile is 36x36; the border goes around it. */
	private static final int TILE_SIZE = 36;
	private static final int BORDER = 2;

	private final ItemManager itemManager;
	private BufferedImage tile;
	private BufferedImage outline;
	private BufferedImage itemImage;
	private GearItem shownItem;

	public GearSlot(ItemManager itemManager, SpriteManager spriteManager, GearType.Slot slot)
	{
		this.itemManager = itemManager;
		setOpaque(false);
		int size = TILE_SIZE + 2 * BORDER;
		setPreferredSize(new Dimension(size, size));
		setMinimumSize(getPreferredSize());
		setMaximumSize(getPreferredSize());
		showItem(null);
		// Sprites load on the client thread, so the results are handed back to the Swing thread.
		spriteManager.getSpriteAsync(SpriteID.Miscgraphics.EQUIPMENT_SLOT_TILE, 0,
			image -> SwingUtilities.invokeLater(() -> setTile(image)));
		spriteManager.getSpriteAsync(outlineSprite(slot), 0,
			image -> SwingUtilities.invokeLater(() -> setOutline(image)));
	}

	/** The faded outline OSRS shows in an empty slot. */
	private static int outlineSprite(GearType.Slot slot)
	{
		switch (slot)
		{
			case WEAPON:
				return SpriteID.Wornicons.WEAPON;
			case ARMOUR:
				return SpriteID.Wornicons.TORSO;
			default:
				return SpriteID.Wornicons.NECK;
		}
	}

	/** Show an item in the slot, or null for an empty slot. Must be called on the Swing thread. */
	public void showItem(GearItem item)
	{
		if (item != null && item.equals(shownItem))
		{
			return;
		}
		shownItem = item;
		itemImage = null;
		setBorder(BorderFactory.createLineBorder(
			item == null ? ColorScheme.DARK_GRAY_COLOR : item.getRarity().getColor(), BORDER));
		if (item != null)
		{
			AsyncBufferedImage image = itemManager.getImage(item.getIconItemId());
			image.onLoaded(() -> SwingUtilities.invokeLater(() ->
			{
				// Only if the slot still shows this item; it may have changed while loading.
				if (item.equals(shownItem))
				{
					itemImage = image;
					repaint();
				}
			}));
		}
		repaint();
	}

	private void setTile(BufferedImage image)
	{
		tile = image;
		repaint();
	}

	private void setOutline(BufferedImage image)
	{
		outline = image;
		repaint();
	}

	@Override
	protected void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		if (tile != null)
		{
			g.drawImage(tile, BORDER, BORDER, TILE_SIZE, TILE_SIZE, null);
		}
		BufferedImage icon = shownItem != null ? itemImage : outline;
		if (icon != null)
		{
			int x = BORDER + (TILE_SIZE - icon.getWidth()) / 2;
			int y = BORDER + (TILE_SIZE - icon.getHeight()) / 2;
			g.drawImage(icon, x, y, null);
		}
	}
}
