package com.dilzosrs.fellowship;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.time.Duration;
import java.time.Instant;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.ComponentConstants;
import net.runelite.client.ui.overlay.components.ComponentOrientation;
import net.runelite.client.ui.overlay.components.ImageComponent;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.SplitComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

/**
 * A short pop-up at the top of the game screen, e.g. when a bundle drops or a character joins,
 * so the player notices even while AFK. It only draws; it doesn't take clicks or affect the game.
 */
public class PopupOverlay extends OverlayPanel
{
	private static final Duration SHOW_FOR = Duration.ofSeconds(6);
	private static final Color GOLD = new Color(255, 200, 0);
	/** Wider than usual when there's an icon, so the text beside it doesn't wrap onto lots of lines. */
	private static final int WIDTH_WITH_ICON = 180;
	private static final int ICON_GAP = 6;

	// Set on the client thread and read while drawing (also the client thread), so plain fields are fine.
	private String message;
	private BufferedImage icon;
	private Instant hideAt;

	public PopupOverlay()
	{
		setPosition(OverlayPosition.TOP_CENTER);
	}

	/** Show a message for a few seconds, replacing any message already showing. */
	public void show(String message)
	{
		show(message, null);
	}

	/**
	 * Show a message with an icon beside it for a few seconds, replacing any message already showing.
	 * The icon may still be loading; it's drawn once it's ready.
	 */
	public void show(String message, BufferedImage icon)
	{
		this.message = message;
		this.icon = icon;
		this.hideAt = Instant.now().plus(SHOW_FOR);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (message == null || Instant.now().isAfter(hideAt))
		{
			message = null;
			icon = null;
			return null;
		}
		panelComponent.getChildren().add(TitleComponent.builder().text("Fellowship").color(GOLD).build());
		LineComponent line = LineComponent.builder().left(message).build();
		if (icon == null)
		{
			panelComponent.setPreferredSize(new Dimension(ComponentConstants.STANDARD_WIDTH, 0));
			panelComponent.getChildren().add(line);
		}
		else
		{
			panelComponent.setPreferredSize(new Dimension(WIDTH_WITH_ICON, 0));
			panelComponent.getChildren().add(SplitComponent.builder()
				.first(new ImageComponent(icon))
				.second(line)
				.orientation(ComponentOrientation.HORIZONTAL)
				.gap(new Point(ICON_GAP, 0))
				.build());
		}
		return super.render(graphics);
	}
}
