package com.dilzosrs.fellowship;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;

/**
 * The bundles and storage section of the sidebar.
 */
public class VaultPanel extends JPanel
{
	private final Consumer<BundleType> onOpen;
	private final IntConsumer onSalvage;

	private final JLabel bundlesTitle = new JLabel();
	private final JPanel bundleRows = new JPanel(new GridLayout(0, 1, 0, 4));
	private final JLabel lastOpenedLabel = new JLabel();
	private final JLabel storageTitle = new JLabel();
	private final JPanel itemRows = new JPanel(new GridLayout(0, 1, 0, 4));

	/**
	 * The callbacks are called on the Swing thread once the player confirms the action.
	 */
	public VaultPanel(Consumer<BundleType> onOpen, IntConsumer onSalvage)
	{
		this.onOpen = onOpen;
		this.onSalvage = onSalvage;

		setLayout(new BorderLayout(0, 6));
		setBackground(ColorScheme.DARKER_GRAY_COLOR);
		setBorder(new EmptyBorder(8, 8, 8, 8));

		JPanel bundleSection = new JPanel(new BorderLayout(0, 4));
		bundleSection.setOpaque(false);
		bundlesTitle.setFont(FontManager.getRunescapeBoldFont());
		bundlesTitle.setForeground(Color.WHITE);
		bundleSection.add(bundlesTitle, BorderLayout.NORTH);
		bundleRows.setOpaque(false);
		bundleSection.add(bundleRows, BorderLayout.CENTER);
		lastOpenedLabel.setFont(FontManager.getRunescapeSmallFont());
		lastOpenedLabel.setForeground(ColorScheme.GRAND_EXCHANGE_PRICE);
		bundleSection.add(lastOpenedLabel, BorderLayout.SOUTH);
		add(bundleSection, BorderLayout.NORTH);

		JPanel storageSection = new JPanel(new BorderLayout(0, 4));
		storageSection.setOpaque(false);
		storageTitle.setFont(FontManager.getRunescapeBoldFont());
		storageTitle.setForeground(Color.WHITE);
		storageSection.add(storageTitle, BorderLayout.NORTH);
		itemRows.setOpaque(false);
		storageSection.add(itemRows, BorderLayout.CENTER);
		add(storageSection, BorderLayout.CENTER);
	}

	/**
	 * Show the bundles and storage. Must be called on the Swing thread.
	 */
	public void show(List<BundleType> bundles, List<GearItem> items, int capacity)
	{
		boolean full = items.size() >= capacity;

		bundlesTitle.setText("Bundles (" + bundles.size() + ")");
		bundleRows.removeAll();
		for (BundleType type : BundleType.values())
		{
			int count = (int) bundles.stream().filter(b -> b == type).count();
			if (count == 0)
			{
				continue;
			}
			JPanel row = new JPanel(new BorderLayout());
			row.setOpaque(false);
			row.add(new JLabel(type.getDisplayName() + " x" + count), BorderLayout.WEST);
			JButton open = new JButton("Open");
			open.setEnabled(!full);
			open.addActionListener(e -> onOpen.accept(type));
			row.add(open, BorderLayout.EAST);
			bundleRows.add(row);
		}
		if (bundles.isEmpty())
		{
			bundleRows.add(smallLabel("Complete things in OSRS to find bundles."));
		}
		else if (full)
		{
			bundleRows.add(smallLabel("<html>Storage is full. Salvage something<br>to open bundles.</html>"));
		}

		storageTitle.setText("Storage (" + items.size() + "/" + capacity + ")");
		itemRows.removeAll();
		for (int i = 0; i < items.size(); i++)
		{
			GearItem item = items.get(i);
			int index = i;
			// The name gets its own line; the stats share the next line with the button.
			JPanel row = new JPanel(new BorderLayout(0, 2));
			row.setOpaque(false);
			JLabel name = new JLabel(item.getName());
			name.setForeground(Color.WHITE);
			row.add(name, BorderLayout.NORTH);
			JPanel statsLine = new JPanel(new BorderLayout());
			statsLine.setOpaque(false);
			statsLine.add(smallLabel(item.getDescription()), BorderLayout.WEST);
			JButton salvage = new JButton("Salvage");
			salvage.addActionListener(e -> confirmSalvage(item, index));
			statsLine.add(salvage, BorderLayout.EAST);
			row.add(statsLine, BorderLayout.SOUTH);
			itemRows.add(row);
		}
		if (items.isEmpty())
		{
			itemRows.add(smallLabel("Empty. Open bundles to find gear."));
		}

		revalidate();
		repaint();
	}

	/**
	 * Show what the last bundle gave. Must be called on the Swing thread.
	 */
	public void showOpened(BundleType bundle, GearItem item, int coins)
	{
		lastOpenedLabel.setText("<html>" + bundle.getDisplayName() + ":<br>" + item.getName()
			+ " and " + coins + " coins!</html>");
	}

	private void confirmSalvage(GearItem item, int index)
	{
		int choice = JOptionPane.showConfirmDialog(
			this,
			"Salvage the " + item.getName() + " for " + item.getRarity().getSalvageValue() + " coins?\n\n"
				+ "The item is gone for good.",
			"Salvage",
			JOptionPane.YES_NO_OPTION);
		if (choice == JOptionPane.YES_OPTION)
		{
			onSalvage.accept(index);
		}
	}

	private static JLabel smallLabel(String text)
	{
		JLabel label = new JLabel(text);
		label.setFont(FontManager.getRunescapeSmallFont());
		label.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
		return label;
	}
}
