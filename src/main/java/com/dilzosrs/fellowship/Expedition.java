package com.dilzosrs.fellowship;

import java.time.Duration;
import java.util.Random;

/**
 * The expeditions a character can be sent on. Timers run in real time, even while logged out.
 * Some need a party of several characters, who go and come back together.
 * <p>
 * The enum names are saved per account, so never rename them without a migration.
 * The display names are safe to change.
 */
public enum Expedition
{
	LUMBRIDGE_SWAMP_CAVES("Explore Lumbridge Swamp Caves", Duration.ofMinutes(30), 1_000, 10, 50,
		0, 1, null, 0),
	DORGESHUUN_CAVES("Looting the Dorgeshuun Caves", Duration.ofHours(2), 3_000, 40, 100,
		8, 3, BundleType.BOSS, 20),
	// Only a Merchant can go, alone. It costs coins and brings back no coins, just bundles.
	GRAND_EXCHANGE_TRADE("Traveling to trade at the Grand Exchange", Duration.ofMinutes(30), 1_000, 0, 0,
		0, 1, BundleType.TRADE, 100, 5, 100, CharacterClass.MERCHANT);

	private final String displayName;
	private final Duration duration;
	private final int xpReward;
	private final int minCoins;
	private final int maxCoins;
	private final int threatLevel;
	private final int partySize;
	/** A bundle that can come back too, or null. */
	private final BundleType bonusBundle;
	private final int bonusBundleChance;
	/** How many of the bonus bundle come back when its chance hits. */
	private final int bonusBundleCount;
	/** Coins paid when the party sets off. */
	private final int cost;
	/** Only this class can go, or null if anyone can. */
	private final CharacterClass requiredClass;

	Expedition(String displayName, Duration duration, int xpReward, int minCoins, int maxCoins,
		int threatLevel, int partySize, BundleType bonusBundle, int bonusBundleChance)
	{
		this(displayName, duration, xpReward, minCoins, maxCoins, threatLevel, partySize,
			bonusBundle, bonusBundleChance, 1, 0, null);
	}

	Expedition(String displayName, Duration duration, int xpReward, int minCoins, int maxCoins,
		int threatLevel, int partySize, BundleType bonusBundle, int bonusBundleChance,
		int bonusBundleCount, int cost, CharacterClass requiredClass)
	{
		this.bonusBundleCount = bonusBundleCount;
		this.cost = cost;
		this.requiredClass = requiredClass;
		this.displayName = displayName;
		this.duration = duration;
		this.xpReward = xpReward;
		this.minCoins = minCoins;
		this.maxCoins = maxCoins;
		this.threatLevel = threatLevel;
		this.partySize = partySize;
		this.bonusBundle = bonusBundle;
		this.bonusBundleChance = bonusBundleChance;
	}

	public String getDisplayName()
	{
		return displayName;
	}

	public Duration getDuration()
	{
		return duration;
	}

	/** Fellowship XP for each party member. Class bonuses don't apply, but the level cap does. */
	public int getXpReward()
	{
		return xpReward;
	}

	/** A random number of coins between the minimum and maximum, both included. Rolled once per party. */
	public int rollCoins(Random random)
	{
		return minCoins + random.nextInt(maxCoins - minCoins + 1);
	}

	/** How dangerous it is. 0 means safe. */
	public int getThreatLevel()
	{
		return threatLevel;
	}

	/** How many characters must go together. */
	public int getPartySize()
	{
		return partySize;
	}

	/** The bonus bundle if its chance hits (rolled once per party), or null. */
	public BundleType rollBonusBundle(Random random)
	{
		return bonusBundle != null && random.nextInt(100) < bonusBundleChance ? bonusBundle : null;
	}

	/** How many of the bonus bundle come back when its chance hits. */
	public int getBonusBundleCount()
	{
		return bonusBundleCount;
	}

	/** Coins paid when the party sets off. 0 means free. */
	public int getCost()
	{
		return cost;
	}

	/**
	 * Whether the team could ever go: it has enough characters for the party, and one of the
	 * required class if there is one. Characters never leave, so once unlocked it stays unlocked.
	 */
	public boolean isUnlockedFor(Team team)
	{
		return team.size() >= partySize && (requiredClass == null || team.count(requiredClass) > 0);
	}

	/** Whether a character of this class is allowed to go. */
	public boolean allows(CharacterClass characterClass)
	{
		return requiredClass == null || requiredClass == characterClass;
	}
}
