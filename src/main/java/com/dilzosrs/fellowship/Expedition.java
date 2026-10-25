package com.dilzosrs.fellowship;

import java.time.Duration;
import java.util.Random;

/**
 * The expeditions a character can be sent on. Timers run in real time, even while logged out.
 * <p>
 * The enum names are saved per account, so never rename them without a migration.
 * The display names are safe to change.
 */
public enum Expedition
{
	LUMBRIDGE_SWAMP_CAVES("Explore Lumbridge Swamp Caves", Duration.ofMinutes(30), 1_000, 10, 50);

	private final String displayName;
	private final Duration duration;
	private final int xpReward;
	private final int minCoins;
	private final int maxCoins;

	Expedition(String displayName, Duration duration, int xpReward, int minCoins, int maxCoins)
	{
		this.displayName = displayName;
		this.duration = duration;
		this.xpReward = xpReward;
		this.minCoins = minCoins;
		this.maxCoins = maxCoins;
	}

	public String getDisplayName()
	{
		return displayName;
	}

	public Duration getDuration()
	{
		return duration;
	}

	/** Fellowship XP for completing it. Class bonuses don't apply, but the level cap does. */
	public int getXpReward()
	{
		return xpReward;
	}

	/** A random number of coins between the minimum and maximum, both included. */
	public int rollCoins(Random random)
	{
		return minCoins + random.nextInt(maxCoins - minCoins + 1);
	}
}
