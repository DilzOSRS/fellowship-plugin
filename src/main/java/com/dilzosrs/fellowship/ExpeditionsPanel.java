package com.dilzosrs.fellowship;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.image.BufferedImage;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.IntConsumer;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import net.runelite.api.gameval.ItemID;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.util.AsyncBufferedImage;
import net.runelite.client.util.ImageUtil;

/**
 * The Events tab: every expedition that's out right now, with the class icons of who went and a
 * clock counting down until they're back. Finished trips get a Collect button.
 */
public class ExpeditionsPanel extends JPanel
{
	/** Item icons are 36x32; these are smaller copies so a party of 3 fits on one line. */
	private static final int MEMBER_ICON_WIDTH = 27;
	private static final int MEMBER_ICON_HEIGHT = 24;
	private static final int CLOCK_ICON_WIDTH = 18;
	private static final int CLOCK_ICON_HEIGHT = 16;

	private final ItemManager itemManager;
	private final IntConsumer onCollect;
	private final JPanel rows = new JPanel();
	private final List<Trip> trips = new ArrayList<>();
	// Ticks every second while a trip is out, to update the countdowns.
	private final Timer countdownTimer = new Timer(1000, e -> updateCountdowns());
	/** The trips last shown, so the rows are only rebuilt when a trip starts or ends. */
	private String shownTrips = "";

	/**
	 * @param onCollect called on the Swing thread with the position of one party member
	 */
	public ExpeditionsPanel(ItemManager itemManager, IntConsumer onCollect)
	{
		this.itemManager = itemManager;
		this.onCollect = onCollect;
		setLayout(new BorderLayout());
		setOpaque(false);
		rows.setLayout(new BoxLayout(rows, BoxLayout.Y_AXIS));
		rows.setOpaque(false);
		add(rows, BorderLayout.NORTH);
	}

	/**
	 * Show the trips the team has out. Party members share an expedition and return time, which
	 * is how they're grouped. Must be called on the Swing thread.
	 */
	public void show(List<CharacterView> team)
	{
		Map<String, List<Integer>> parties = new LinkedHashMap<>();
		for (int i = 0; i < team.size(); i++)
		{
			CharacterView view = team.get(i);
			if (view.expedition != null)
			{
				parties.computeIfAbsent(view.expedition.name() + "@" + view.expeditionEnd, k -> new ArrayList<>()).add(i);
			}
		}
		// Names can shift after a class advancement, so they're part of what's compared too.
		StringBuilder summary = new StringBuilder();
		parties.forEach((key, members) ->
		{
			summary.append(key);
			members.forEach(i -> summary.append(',').append(team.get(i).name).append(team.get(i).characterClass));
			summary.append(';');
		});
		if (summary.toString().equals(shownTrips))
		{
			return;
		}
		shownTrips = summary.toString();

		trips.clear();
		rows.removeAll();
		if (parties.isEmpty())
		{
			JLabel empty = new JLabel("Nobody is away on an expedition.");
			empty.setFont(FontManager.getRunescapeSmallFont());
			empty.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
			rows.add(empty);
			countdownTimer.stop();
		}
		else
		{
			for (List<Integer> members : parties.values())
			{
				if (!trips.isEmpty())
				{
					rows.add(Box.createVerticalStrut(8));
				}
				Trip trip = new Trip(team, members);
				trips.add(trip);
				rows.add(trip.panel);
			}
			countdownTimer.start();
			updateCountdowns();
		}
		rows.revalidate();
		rows.repaint();
	}

	/** Stop the countdown timer. Call when the plugin shuts down. */
	public void stopTimer()
	{
		countdownTimer.stop();
	}

	private void updateCountdowns()
	{
		Instant now = Instant.now();
		trips.forEach(trip -> trip.update(now));
	}

	/** Load an item icon at a smaller size into a label, once RuneLite has drawn it. */
	private void setSmallIcon(JLabel label, int itemId, int width, int height)
	{
		AsyncBufferedImage image = itemManager.getImage(itemId);
		image.onLoaded(() ->
		{
			BufferedImage small = ImageUtil.resizeImage(image, width, height);
			SwingUtilities.invokeLater(() -> label.setIcon(new ImageIcon(small)));
		});
	}

	/** One trip's row: the expedition, who went, and the countdown or a Collect button. */
	private final class Trip
	{
		final JPanel panel = new JPanel(new BorderLayout(0, 4));
		final Instant end;
		final JLabel countdown = new JLabel();
		final JButton collect = new JButton("Collect");

		Trip(List<CharacterView> team, List<Integer> members)
		{
			CharacterView first = team.get(members.get(0));
			end = first.expeditionEnd;

			panel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
			panel.setBorder(new EmptyBorder(6, 6, 6, 6));
			panel.setAlignmentX(LEFT_ALIGNMENT);

			JLabel name = new JLabel("<html>" + first.expedition.getOngoingName() + "</html>");
			name.setForeground(Color.WHITE);
			panel.add(name, BorderLayout.NORTH);

			JPanel middle = new JPanel(new BorderLayout(4, 0));
			middle.setOpaque(false);
			JPanel icons = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
			icons.setOpaque(false);
			for (int index : members)
			{
				CharacterView member = team.get(index);
				JLabel icon = new JLabel();
				icon.setToolTipText(member.name);
				setSmallIcon(icon, member.characterClass.getIconItemId(), MEMBER_ICON_WIDTH, MEMBER_ICON_HEIGHT);
				icons.add(icon);
			}
			middle.add(icons, BorderLayout.WEST);
			setSmallIcon(countdown, ItemID.TRAIL_WATCH, CLOCK_ICON_WIDTH, CLOCK_ICON_HEIGHT);
			countdown.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
			middle.add(countdown, BorderLayout.EAST);
			panel.add(middle, BorderLayout.CENTER);

			int someone = members.get(0);
			collect.addActionListener(e -> onCollect.accept(someone));
			collect.setVisible(false);
			panel.add(collect, BorderLayout.SOUTH);
		}

		void update(Instant now)
		{
			Duration left = Duration.between(now, end);
			boolean done = left.isNegative() || left.isZero();
			countdown.setText(done ? "Back!" : CharacterCard.formatCountdown(left));
			countdown.setForeground(done ? ColorScheme.PROGRESS_COMPLETE_COLOR : ColorScheme.LIGHT_GRAY_COLOR);
			if (collect.isVisible() != done)
			{
				collect.setVisible(done);
				panel.revalidate();
			}
		}
	}
}
