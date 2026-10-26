package com.dilzosrs.fellowship;

import java.time.Duration;
import java.time.Instant;
import net.runelite.api.Skill;

/**
 * A Fellowship character. Its level comes from its total Fellowship XP, and its class
 * sets its level cap. It can be away on an expedition, and earns no OSRS-based XP while away.
 */
public class FellowshipCharacter
{
	private CharacterClass characterClass;
	private long xp;
	/** Hundredths of an XP not yet added. Kept in memory only, so under 1 XP can be lost on a restart. */
	private long xpRemainder;

	/** The expedition the character is on, or null when it's home. */
	private Expedition expedition;
	private Instant expeditionStart;

	public FellowshipCharacter(CharacterClass characterClass, long xp)
	{
		this.characterClass = characterClass;
		this.xp = Math.min(xp, maxXp());
	}

	public CharacterClass getCharacterClass()
	{
		return characterClass;
	}

	public long getXp()
	{
		return xp;
	}

	public int getLevelCap()
	{
		return characterClass.getLevelCap();
	}

	/**
	 * Add Fellowship XP for OSRS XP gained in a skill, at full share.
	 */
	public void gainXp(Skill skill, long osrsXp)
	{
		gainXp(skill, osrsXp, 100);
	}

	/**
	 * Add a percentage of the OSRS XP gained in a skill, applying the class's bonus for that skill.
	 * Fractions of an XP are carried over to the next gain, so small drops still add up.
	 * A character who is away on an expedition gets nothing.
	 */
	public void gainXp(Skill skill, long osrsXp, int percent)
	{
		if (isAway())
		{
			return;
		}
		// Work in hundredths of an XP so 10% of a small drop isn't rounded away.
		long hundredths = osrsXp * characterClass.getXpMultiplier(skill) * percent + xpRemainder;
		addXp(hundredths / 100);
		xpRemainder = hundredths % 100;
	}

	public void addXp(long amount)
	{
		xp = Math.min(xp + amount, maxXp());
	}

	public int getLevel()
	{
		return LevelCurve.levelForXp(xp);
	}

	public boolean isAtLevelCap()
	{
		return getLevel() >= getLevelCap();
	}

	public boolean canEvolve()
	{
		return !isAway() && isAtLevelCap() && !characterClass.getEvolutions().isEmpty();
	}

	/**
	 * Evolve into a new class. The character restarts at level 1.
	 *
	 * @return false if the character can't evolve into that class right now
	 */
	public boolean evolve(CharacterClass newClass)
	{
		if (!canEvolve() || !characterClass.getEvolutions().contains(newClass))
		{
			return false;
		}
		characterClass = newClass;
		xp = 0;
		return true;
	}

	public boolean isAway()
	{
		return expedition != null;
	}

	/** The expedition the character is on, or null when it's home. */
	public Expedition getExpedition()
	{
		return expedition;
	}

	/** When the current expedition finishes, or null when the character is home. */
	public Instant getExpeditionEnd()
	{
		return isAway() ? expeditionStart.plus(expedition.getDuration()) : null;
	}

	/**
	 * Send the character on an expedition, or restore one loaded from a save.
	 *
	 * @return false if the character is already away
	 */
	public boolean startExpedition(Expedition newExpedition, Instant start)
	{
		if (isAway())
		{
			return false;
		}
		expedition = newExpedition;
		expeditionStart = start;
		return true;
	}

	public Instant getExpeditionStart()
	{
		return expeditionStart;
	}

	/** True once the expedition's time is up. The character stays away until it's collected. */
	public boolean isExpeditionDone(Instant now)
	{
		return isAway() && !now.isBefore(getExpeditionEnd());
	}

	/**
	 * Bring the character home from a finished expedition and add its XP reward.
	 * Coins belong to the account, so the caller handles those.
	 *
	 * @return the expedition that was collected, or null if there's nothing to collect yet
	 */
	public Expedition collectExpedition(Instant now)
	{
		if (!isExpeditionDone(now))
		{
			return null;
		}
		Expedition finished = expedition;
		expedition = null;
		expeditionStart = null;
		addXp(finished.getXpReward());
		return finished;
	}

	/** How long until the expedition is done (zero once it is), or null when the character is home. */
	public Duration getTimeLeft(Instant now)
	{
		if (!isAway())
		{
			return null;
		}
		Duration left = Duration.between(now, getExpeditionEnd());
		return left.isNegative() ? Duration.ZERO : left;
	}

	private long maxXp()
	{
		return LevelCurve.xpForLevel(getLevelCap());
	}
}
