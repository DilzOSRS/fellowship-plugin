package com.dilzosrs.fellowship;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.time.Duration;
import java.time.Instant;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.SoundEffectID;
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
 * so the player notices even while AFK. It slides in, plays a chime, then fades out.
 * It only draws and plays a sound on this computer; it doesn't take clicks or affect the game.
 */
public class PopupOverlay extends OverlayPanel
{
	private static final Duration SHOW_FOR = Duration.ofSeconds(6);
	/** How long the slide and fade in take, and how long the fade out takes, in milliseconds. */
	private static final long FADE_IN_MS = 300;
	private static final long FADE_OUT_MS = 600;
	/** How far above its spot the pop-up starts before sliding down into place. */
	private static final int SLIDE_DISTANCE = 16;
	/** Plays at the player's own sound effect volume, so it's silent if they've muted sound effects. */
	private static final int CHIME = SoundEffectID.GE_ADD_OFFER_DINGALING;
	private static final Color GOLD = new Color(255, 200, 0);
	/** Wider than usual when there's an icon, so the text beside it doesn't wrap onto lots of lines. */
	private static final int WIDTH_WITH_ICON = 180;
	private static final int ICON_GAP = 6;

	private final Client client;

	// Set on the client thread and read while drawing (also the client thread), so plain fields are fine.
	private String message;
	private BufferedImage icon;
	private Color color;
	private Instant shownAt;

	@Inject
	public PopupOverlay(Client client)
	{
		this.client = client;
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
		show(message, icon, Color.WHITE);
	}

	/** Like {@link #show(String, BufferedImage)}, with the message in a colour (e.g. an item's rarity). */
	public void show(String message, BufferedImage icon, Color color)
	{
		this.message = message;
		this.icon = icon;
		this.color = color;
		this.shownAt = Instant.now();
		client.playSoundEffect(CHIME);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		long age = message == null ? 0 : Duration.between(shownAt, Instant.now()).toMillis();
		long timeLeft = SHOW_FOR.toMillis() - age;
		if (message == null || timeLeft <= 0)
		{
			message = null;
			icon = null;
			return null;
		}

		// 0 → 1 while appearing, 1 while showing, 1 → 0 while disappearing.
		float appear = Math.min(1f, (float) age / FADE_IN_MS);
		float opacity = Math.min(appear, Math.min(1f, (float) timeLeft / FADE_OUT_MS));
		int slide = Math.round((1f - appear) * -SLIDE_DISTANCE);

		panelComponent.getChildren().add(TitleComponent.builder().text("Fellowship").color(GOLD).build());
		LineComponent line = LineComponent.builder().left(message).leftColor(color).build();
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

		Composite oldComposite = graphics.getComposite();
		graphics.setComposite(AlphaComposite.SrcOver.derive(opacity));
		graphics.translate(0, slide);
		Dimension size = super.render(graphics);
		graphics.translate(0, -slide);
		graphics.setComposite(oldComposite);
		return size;
	}
}
