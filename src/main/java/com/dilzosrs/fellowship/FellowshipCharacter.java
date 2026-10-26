package com.dilzosrs.fellowship;

import java.time.Duration;
import java.time.Instant;
import java.util.EnumMap;
import java.util.Map;
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

	/** Gear worn in each slot. A missing slot is empty. */
	private final Map<GearType.Slot, GearItem> equipment = new EnumMap<>(GearType.Slot.class);

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
	 *
	 * @return how many levels the character gained
	 */
	public int gainXp(Skill skill, long osrsXp)
	{
		return gainXp(skill, osrsXp, 100);
	}

	/**
	 * Add a percentage of the OSRS XP gained in a skill, applying the class's bonus for that skill.
	 * Fractions of an XP are carried over to the next gain, so small drops still add up.
	 * A character who is away on an expedition gets nothing.
	 *
	 * @return how many levels the character gained
	 */
	public int gainXp(Skill skill, long osrsXp, int percent)
	{
		if (isAway())
		{
			return 0;
		}
		// Work in hundredths of an XP so 10% of a small drop isn't rounded away.
		long hundredths = osrsXp * characterClass.getXpMultiplier(skill) * percent + xpRemainder;
		xpRemainder = hundredths % 100;
		return addXp(hundredths / 100);
	}

	/**
	 * @return how many levels the character gained
	 */
	public int addXp(long amount)
	{
		int levelBefore = getLevel();
		xp = Math.min(xp + amount, maxXp());
		return getLevel() - levelBefore;
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

	/** The gear worn in that slot, or null if it's empty. */
	public GearItem getEquipped(GearType.Slot slot)
	{
		return equipment.get(slot);
	}

	public boolean canEquip(GearItem item)
	{
		return item.getType().canBeUsedBy(characterClass);
	}

	/**
	 * Wear a piece of gear in its slot.
	 *
	 * @return the gear that was in that slot before (to go back to storage), or null
	 * @throws IllegalArgumentException if this character's class can't use it
	 */
	public GearItem equip(GearItem item)
	{
		if (!canEquip(item))
		{
			throw new IllegalArgumentException(characterClass + " can't use " + item.getName());
		}
		return equipment.put(item.getType().getSlot(), item);
	}

	/**
	 * Take off the gear in a slot.
	 *
	 * @return the gear that was there, or null if the slot was empty
	 */
	public GearItem unequip(GearType.Slot slot)
	{
		return equipment.remove(slot);
	}

	/** ATK Power from the equipped weapon. */
	public int getAtk()
	{
		return powerIn(GearType.Slot.WEAPON);
	}

	/** DEF Power from the equipped armour. */
	public int getDef()
	{
		return powerIn(GearType.Slot.ARMOUR);
	}

	/** MAG Power from the equipped trinket. */
	public int getMag()
	{
		return powerIn(GearType.Slot.TRINKET);
	}

	private int powerIn(GearType.Slot slot)
	{
		GearItem item = equipment.get(slot);
		return item == null ? 0 : item.getPower();
	}

	private long maxXp()
	{
		return LevelCurve.xpForLevel(getLevelCap());
	}
}
