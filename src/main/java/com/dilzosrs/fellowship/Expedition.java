package com.dilzosrs.fellowship;

import java.time.Duration;

/**
 * The expeditions a character can be sent on. Timers run in real time, even while logged out.
 * <p>
 * The enum names are saved per account, so never rename them without a migration.
 * The display names are safe to change.
 */
public enum Expedition
{
	LUMBRIDGE_SWAMP_CAVES("Explore Lumbridge Swamp Caves", Duration.ofMinutes(30));

	private final String displayName;
	private final Duration duration;

	Expedition(String displayName, Duration duration)
	{
		this.displayName = displayName;
		this.duration = duration;
	}

	public String getDisplayName()
	{
		return displayName;
	}

	public Duration getDuration()
	{
		return duration;
	}
}
