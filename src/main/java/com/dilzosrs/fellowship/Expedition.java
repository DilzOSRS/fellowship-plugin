package com.dilzosrs.fellowship;

import java.time.Duration;
import java.util.Collection;
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
	// Threat 3 since 2026-11-02 (was 1): with class stats, a fresh t2 plus one Common weapon is about safe.
	LUMBRIDGE_SWAMP_CAVES("Explore the Lumbridge Swamp Caves", Duration.ofMinutes(15), 1_000, 10, 50,
		3, 1, BundleType.GENERIC, 100),
	// 30 minutes since 2026-11-03 (was 2 hours); the user kept its rewards the same.
	DORGESHUUN_CAVES("Loot the Dorgeshuun Caves", Duration.ofMinutes(30), 3_000, 40, 100,
		8, 3, BundleType.BOSS, 100),
	// Only a Merchant can go, alone. It costs coins and brings back no coins, just bundles.
	GRAND_EXCHANGE_TRADE("Traveling to trade at the Grand Exchange", Duration.ofMinutes(30), 1_000, 0, 0,
		0, 1, BundleType.TRADE, 100, 5, 100, CharacterClass.MERCHANT),
	// Only an Alchemist can go, alone, taking one stored item, which comes back as coins
	// (TRANSMUTE_MULTIPLIER times its salvage value). Gives no other coins.
	TRANSMUTING_METALS("Transmute metals", Duration.ofMinutes(10), 500, 0, 0,
		0, 1, null, 0, 1, 0, CharacterClass.ALCHEMIST),
	// Skillers only (the whole Skiller tree), 1 to 3 of them. Safe. 1.5 hours before the Skiller
	// speed-up (since 2026-11-03; it used to be 30 minutes shorter per extra Skiller).
	// Unlocks at Guild level 2 once a Skiller has joined.
	FORTIFYING_ENCAMPMENT("Secure and Fortify the Encampment", Duration.ofMinutes(90), 5_000, 0, 0,
		0, 1, 3, BundleType.GENERIC, 25, 1, 0, CharacterClass.SKILLER);

	/** Transmuting an item gives this many times its salvage value in coins. */
	static final int TRANSMUTE_MULTIPLIER = 2;
	/** The Guild level Fortifying the Encampment needs. */
	static final int FORTIFY_GUILD_LEVEL = 2;
	/** Each Skiller-tree follower in the party takes this percent off the trip, on every expedition. */
	static final int SKILLER_PERCENT_OFF = 25;
	/** Only this many Skillers count, so a trip never gets shorter than a quarter of its length. */
	static final int MAX_SKILLERS_COUNTED = 3;

	private final String displayName;
	private final Duration duration;
	private final int xpReward;
	private final int minCoins;
	private final int maxCoins;
	private final int threatLevel;
	private final int minPartySize;
	private final int maxPartySize;
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
		this(displayName, duration, xpReward, minCoins, maxCoins, threatLevel, partySize, partySize,
			bonusBundle, bonusBundleChance, bonusBundleCount, cost, requiredClass);
	}

	Expedition(String displayName, Duration duration, int xpReward, int minCoins, int maxCoins,
		int threatLevel, int minPartySize, int maxPartySize, BundleType bonusBundle, int bonusBundleChance, int bonusBundleCount, int cost,
		CharacterClass requiredClass)
	{
		this.minPartySize = minPartySize;
		this.maxPartySize = maxPartySize;
		this.bonusBundleCount = bonusBundleCount;
		this.cost = cost;
		this.requiredClass = requiredClass;
		this.displayName = displayName;
		this.duration = duration;
		this.xpReward = xpReward;
		this.minCoins = minCoins;
		this.maxCoins = maxCoins;
		this.threatLevel = threatLevel;
		this.bonusBundle = bonusBundle;
		this.bonusBundleChance = bonusBundleChance;
	}

	public String getDisplayName()
	{
		return displayName;
	}

	/**
	 * The name while the expedition is under way, e.g. "Exploring the Lumbridge Swamp Caves" instead of
	 * "Explore the Lumbridge Swamp Caves". Names that already read as under way stay the same.
	 */
	public String getOngoingName()
	{
		switch (this)
		{
			case LUMBRIDGE_SWAMP_CAVES:
				return "Exploring the Lumbridge Swamp Caves";
			case DORGESHUUN_CAVES:
				return "Looting the Dorgeshuun Caves";
			case TRANSMUTING_METALS:
				return "Transmuting metals";
			case FORTIFYING_ENCAMPMENT:
				return "Fortifying the Encampment";
			default:
				return displayName;
		}
	}

	/** How long it takes with no Skillers along (the longest it can take). */
	public Duration getDuration()
	{
		return duration;
	}

	/** How long it takes with this many Skiller-tree followers in the party: 25% less for each, up to 3. */
	public Duration getDuration(int skillers)
	{
		int counted = Math.max(0, Math.min(skillers, MAX_SKILLERS_COUNTED));
		return duration.multipliedBy(100 - SKILLER_PERCENT_OFF * counted).dividedBy(100);
	}

	/** How many of these classes are in the Skiller tree (an advanced Harvester still counts). */
	public static int countSkillers(Collection<CharacterClass> classes)
	{
		return (int) classes.stream().filter(c -> c.getBaseClass() == CharacterClass.SKILLER).count();
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

	/** The fewest characters that can go together. */
	public int getMinPartySize()
	{
		return minPartySize;
	}

	/** The most characters that can go together. */
	public int getMaxPartySize()
	{
		return maxPartySize;
	}

	/** Whether this many characters can go together. */
	public boolean allowsPartySize(int size)
	{
		return size >= minPartySize && size <= maxPartySize;
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
	 * Whether the team could ever go: it has enough characters allowed on it for the smallest
	 * party. The Swamp Caves also wait for the first class advancement, and Fortifying for Guild
	 * level 2. Characters never leave, and the Guild never loses a level, so once unlocked it
	 * stays unlocked.
	 */
	public boolean isUnlockedFor(Team team, int guildLevel)
	{
		if (this == LUMBRIDGE_SWAMP_CAVES && !team.hasAdvancedCharacter())
		{
			return false;
		}
		if (this == FORTIFYING_ENCAMPMENT && guildLevel < FORTIFY_GUILD_LEVEL)
		{
			return false;
		}
		int allowed = requiredClass == null ? team.size() : team.countStartedAs(requiredClass);
		return allowed >= minPartySize;
	}

	/** Whether the character takes a stored item along (to transmute it into coins). */
	public boolean needsItem()
	{
		return this == TRANSMUTING_METALS;
	}

	/** Whether a character of this class is allowed to go. Advanced classes count as where they started. */
	public boolean allows(CharacterClass characterClass)
	{
		return requiredClass == null || requiredClass == characterClass.getBaseClass();
	}
}
