package com.dilzosrs.fellowship;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.border.EmptyBorder;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;

/**
 * The bundles and storage section of the sidebar.
 */
public class VaultPanel extends JPanel
{
	private final Consumer<BundleType> onOpen;
	private final IntConsumer onSalvage;
	private final ItemManager itemManager;
	private final SpriteManager spriteManager;

	private final JLabel bundlesTitle = new JLabel();
	private final JPanel bundleRows = new JPanel(new GridLayout(0, 1, 0, 4));
	private final JLabel lastOpenedLabel = new JLabel();
	private final JLabel storageTitle = new JLabel();
	private final JPanel itemRows = new JPanel();
	private final JPanel bundleSection = new JPanel(new BorderLayout(0, 4));

	// While a character is picking gear: a banner on top, and only the items for that slot.
	private final JPanel chooseBanner = new JPanel(new BorderLayout(0, 4));
	private final JLabel choosePrompt = new JLabel();
	private final JButton takeOffButton = new JButton("Take off");
	private Predicate<GearItem> chooseShows;
	/** Why the character can't use an item, or null if it can. Those items show faded. */
	private Function<GearItem, String> chooseWhyNot;
	/** What clicking an item does while choosing, e.g. "equip". */
	private String chooseAction;
	private IntConsumer onPick;
	private Runnable onTakeOff;
	private Runnable onChooseDone;

	// The latest bundles and storage, so the panel can be redrawn when choosing starts or stops.
	private List<BundleType> shownBundles = new ArrayList<>();
	private List<GearItem> shownItems = new ArrayList<>();
	private int shownCapacity;

	/**
	 * The callbacks are called on the Swing thread once the player confirms the action.
	 */
	public VaultPanel(Consumer<BundleType> onOpen, IntConsumer onSalvage, ItemManager itemManager,
		SpriteManager spriteManager)
	{
		this.onOpen = onOpen;
		this.onSalvage = onSalvage;
		this.itemManager = itemManager;
		this.spriteManager = spriteManager;

		setLayout(new BorderLayout(0, 6));
		setBackground(ColorScheme.DARKER_GRAY_COLOR);
		setBorder(new EmptyBorder(8, 8, 8, 8));

		chooseBanner.setOpaque(false);
		choosePrompt.setForeground(Color.WHITE);
		chooseBanner.add(choosePrompt, BorderLayout.CENTER);
		JPanel chooseButtons = new JPanel(new GridLayout(1, 0, 4, 0));
		chooseButtons.setOpaque(false);
		takeOffButton.addActionListener(e ->
		{
			Runnable takeOff = onTakeOff;
			stopChoosing();
			takeOff.run();
		});
		chooseButtons.add(takeOffButton);
		JButton cancel = new JButton("Cancel");
		cancel.addActionListener(e -> stopChoosing());
		chooseButtons.add(cancel);
		chooseBanner.add(chooseButtons, BorderLayout.SOUTH);
		chooseBanner.setVisible(false);

		bundleSection.setOpaque(false);
		bundlesTitle.setFont(FontManager.getRunescapeBoldFont());
		bundlesTitle.setForeground(Color.WHITE);
		bundleSection.add(bundlesTitle, BorderLayout.NORTH);
		bundleRows.setOpaque(false);
		bundleSection.add(bundleRows, BorderLayout.CENTER);
		lastOpenedLabel.setFont(FontManager.getRunescapeSmallFont());
		lastOpenedLabel.setForeground(ColorScheme.GRAND_EXCHANGE_PRICE);
		bundleSection.add(lastOpenedLabel, BorderLayout.SOUTH);
		JPanel top = new JPanel(new BorderLayout());
		top.setOpaque(false);
		top.add(chooseBanner, BorderLayout.NORTH);
		top.add(bundleSection, BorderLayout.CENTER);
		add(top, BorderLayout.NORTH);

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
		shownBundles = bundles;
		shownItems = items;
		shownCapacity = capacity;
		boolean full = items.size() >= capacity;
		boolean choosing = chooseShows != null;
		chooseBanner.setVisible(choosing);
		bundleSection.setVisible(!choosing);

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
			JLabel label = new JLabel(type.getDisplayName() + " x" + count);
			label.setIconTextGap(6);
			itemManager.getImage(type.getIconItemId()).addTo(label);
			row.add(label, BorderLayout.WEST);
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
		// While choosing, only the items for that slot are shown. Each keeps its real storage position.
		List<Integer> shown = new ArrayList<>();
		for (int i = 0; i < items.size(); i++)
		{
			if (!choosing || chooseShows.test(items.get(i)))
			{
				shown.add(i);
			}
		}
		// Three squares across like the Gear tab, or one line for the empty message.
		itemRows.setLayout(shown.isEmpty() ? new GridLayout(0, 1) : new GridLayout(0, 3, 4, 6));
		for (int index : shown)
		{
			itemRows.add(buildItemCell(items.get(index), index, choosing));
		}
		if (shown.isEmpty() && !choosing)
		{
			itemRows.add(smallLabel("Empty. Open bundles to find gear."));
		}

		revalidate();
		repaint();
	}

	/**
	 * Show what the last bundle gave (item is null if it held none). Must be called on the Swing thread.
	 */
	public void showOpened(BundleType bundle, GearItem item, int coins)
	{
		// Trade Bundles give no coins, so there's nothing to add for them.
		lastOpenedLabel.setText("<html>" + bundle.getDisplayName() + ":<br>" + (item == null
			? coins + " coins, but no item this time."
			: item.getName() + (coins > 0 ? " and " + coins + " coins" : "") + "!") + "</html>");
	}

	/**
	 * Show only the items that shows accepts, with a prompt on top (action says what a click does,
	 * e.g. "equip"), until the player picks one, takes off what's worn (takeOff, or null if nothing
	 * is worn), or cancels. Items whyNot gives a reason for are faded and can't be picked, and the
	 * reason shows on hover. onDone runs in every case. Must be called on the Swing thread.
	 */
	public void startChoosing(String prompt, String action, Predicate<GearItem> shows,
		Function<GearItem, String> whyNot, IntConsumer pick, Runnable takeOff, Runnable onDone)
	{
		chooseAction = action;
		chooseShows = shows;
		chooseWhyNot = whyNot;
		onPick = pick;
		onTakeOff = takeOff;
		onChooseDone = onDone;
		choosePrompt.setText("<html>" + prompt + "</html>");
		takeOffButton.setVisible(takeOff != null);
		show(shownBundles, shownItems, shownCapacity);
	}

	/** Leave choosing and show the whole Bank again. Safe to call when not choosing. */
	public void stopChoosing()
	{
		if (chooseShows == null)
		{
			return;
		}
		Runnable done = onChooseDone;
		chooseShows = null;
		chooseWhyNot = null;
		onPick = null;
		onTakeOff = null;
		onChooseDone = null;
		show(shownBundles, shownItems, shownCapacity);
		done.run();
	}

	/** One stored item: its slot square with the name under it. Click to equip it while choosing, else salvage. */
	private GearCell buildItemCell(GearItem item, int index, boolean choosing)
	{
		String whyNot = choosing ? chooseWhyNot.apply(item) : null;
		GearCell cell = new GearCell(itemManager, spriteManager, item.getType().getSlot(), () ->
		{
			if (whyNot != null)
			{
				return;
			}
			if (choosing)
			{
				IntConsumer pick = onPick;
				stopChoosing();
				pick.accept(index);
			}
			else
			{
				confirmSalvage(item, index);
			}
		});
		cell.show(item, item.getTooltip(whyNot, whyNot != null ? null : choosing ? "Click to " + chooseAction
			: "Click to salvage for " + item.getRarity().getSalvageValue() + " coins"));
		cell.setDimmed(whyNot != null);
		if (whyNot != null)
		{
			// Can't be picked, so no hand cursor.
			cell.setCursor(Cursor.getDefaultCursor());
		}
		if (!choosing)
		{
			// Right-click menu, like OSRS. Picking from it is deliberate enough, so no confirm step.
			JPopupMenu menu = new JPopupMenu();
			JMenuItem salvage = new JMenuItem("Salvage (" + item.getRarity().getSalvageValue() + " coins)");
			salvage.addActionListener(e -> onSalvage.accept(index));
			menu.add(salvage);
			cell.setComponentPopupMenu(menu);
		}
		return cell;
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
