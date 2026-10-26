package com.dilzosrs.fellowship;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.time.Duration;
import java.time.Instant;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

/**
 * A short pop-up at the top of the game screen, e.g. when a bundle drops, so the player notices
 * even while AFK. It only draws; it doesn't take clicks or affect the game.
 */
public class PopupOverlay extends OverlayPanel
{
	private static final Duration SHOW_FOR = Duration.ofSeconds(6);
	private static final Color GOLD = new Color(255, 200, 0);

	// Set on the client thread and read while drawing (also the client thread), so plain fields are fine.
	private String message;
	private Instant hideAt;

	public PopupOverlay()
	{
		setPosition(OverlayPosition.TOP_CENTER);
	}

	/** Show a message for a few seconds, replacing any message already showing. */
	public void show(String message)
	{
		this.message = message;
		this.hideAt = Instant.now().plus(SHOW_FOR);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (message == null || Instant.now().isAfter(hideAt))
		{
			message = null;
			return null;
		}
		panelComponent.getChildren().add(TitleComponent.builder().text("Fellowship").color(GOLD).build());
		panelComponent.getChildren().add(LineComponent.builder().left(message).build());
		return super.render(graphics);
	}
}
