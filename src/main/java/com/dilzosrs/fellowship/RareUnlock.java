package com.dilzosrs.fellowship;

import java.util.EnumSet;
import java.util.Random;
import java.util.Set;
import net.runelite.api.Skill;

/**
 * The rare chance of a character joining the Fellowship from OSRS XP, e.g. a Thief from Thieving
 * and Agility, an Acolyte from Prayer, or a Vyre from any XP gained in Morytania.
 * <p>
 * There's a roll for every 1,000 XP (fair to fast and slow training methods), and skill-based
 * unlocks also roll on every level-up (fair to low-level players, who earn XP slowly but level
 * often). Each one of the class you already have makes the chances 10 times smaller.
 */
public class RareUnlock
{
	/** OSRS XP in the unlock's skills (together) per XP roll. */
	static final long XP_STEP = 1_000;
	/** Chances are out of this many, so tiny chances (0.01%) are still whole numbers. */
	static final int ONE_IN = 1_000_000;
	/** 1% per XP step for the first one. */
	static final int XP_STEP_CHANCE = 10_000;
	/** 3% per level-up for the first one. */
	static final int LEVEL_UP_CHANCE = 30_000;
	/** 1% per interaction (opening a shop or the Grand Exchange) for the first one. */
	static final int INTERACTION_CHANCE = 10_000;
	/** Each one already on the team divides the chances by this. */
	static final int HARDER_PER_OWNED = 10;

	private final CharacterClass characterClass;
	private final Set<Skill> skills;
	private final boolean levelUpRolls;
	private final boolean morytaniaOnly;
	// Kept in memory only: a restart loses under 1,000 XP towards the next roll.
	private long xpProgress;

	private RareUnlock(CharacterClass characterClass, Set<Skill> skills, boolean levelUpRolls, boolean morytaniaOnly)
	{
		this.characterClass = characterClass;
		this.skills = skills;
		this.levelUpRolls = levelUpRolls;
		this.morytaniaOnly = morytaniaOnly;
	}

	/** Rolls on XP and level-ups in these skills, anywhere. */
	public static RareUnlock fromSkills(CharacterClass characterClass, Skill first, Skill... rest)
	{
		return new RareUnlock(characterClass, EnumSet.of(first, rest), true, false);
	}

	/** Rolls on XP in any skill, but only while the player is in Morytania. No level-up rolls. */
	public static RareUnlock fromMorytania(CharacterClass characterClass)
	{
		return new RareUnlock(characterClass, EnumSet.allOf(Skill.class), false, true);
	}

	/** The class this unlock adds to the team. */
	public CharacterClass getCharacterClass()
	{
		return characterClass;
	}

	/** If true, the caller should only roll while the player is in Morytania. */
	public boolean isMorytaniaOnly()
	{
		return morytaniaOnly;
	}

	public boolean counts(Skill skill)
	{
		return skills.contains(skill);
	}

	/**
	 * Roll for OSRS XP gained in a skill, including any level-ups it caused.
	 *
	 * @param levelsGained how many OSRS levels the skill went up with this XP
	 * @param owned how many of this class are already on the team
	 * @return true if a character joins (at most one per call)
	 */
	public boolean roll(Skill skill, long xpGained, int levelsGained, int owned, Random random)
	{
		if (!counts(skill))
		{
			return false;
		}
		boolean found = false;
		xpProgress += xpGained;
		while (xpProgress >= XP_STEP)
		{
			xpProgress -= XP_STEP;
			found |= hits(XP_STEP_CHANCE, owned, random);
		}
		for (int i = 0; levelUpRolls && i < levelsGained; i++)
		{
			found |= hits(LEVEL_UP_CHANCE, owned, random);
		}
		return found;
	}

	/**
	 * One roll at the chance per interaction (e.g. opening a shop), for unlocks that come from
	 * something the player does rather than XP.
	 *
	 * @param owned how many of this class are already on the team
	 */
	public static boolean rollInteraction(int owned, Random random)
	{
		return hits(INTERACTION_CHANCE, owned, random);
	}

	/** A chance (out of {@link #ONE_IN}), made 10 times smaller for each one already owned. */
	static int chance(int baseChance, int owned)
	{
		int chance = baseChance;
		for (int i = 0; i < owned; i++)
		{
			chance /= HARDER_PER_OWNED;
		}
		return chance;
	}

	private static boolean hits(int baseChance, int owned, Random random)
	{
		return random.nextInt(ONE_IN) < chance(baseChance, owned);
	}

	long getXpProgress()
	{
		return xpProgress;
	}
}
