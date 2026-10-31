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
 * often). Each one of the class you already have makes the chances 5 times smaller, except for
 * unlocks with their own chance for each one, like the Skiller.
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
	/** 0.1% per Low or High Level Alchemy cast for the first Alchemist (about 1,000 casts). */
	static final int ALCH_CHANCE = 1_000;
	/** Each one already on the team divides the chances by this. */
	static final int HARDER_PER_OWNED = 5;
	/** Combat skills, which don't count towards skilling unlocks like the Skiller. */
	static final Set<Skill> COMBAT_SKILLS = EnumSet.of(Skill.ATTACK, Skill.STRENGTH, Skill.DEFENCE,
		Skill.RANGED, Skill.MAGIC, Skill.HITPOINTS, Skill.PRAYER, Skill.SLAYER);

	private final CharacterClass characterClass;
	private final Set<Skill> skills;
	private final boolean levelUpRolls;
	private final boolean morytaniaOnly;
	/**
	 * Its own chance per XP roll and per level-up for the 1st, 2nd, 3rd... one (out of ONE_IN),
	 * with none after the last, or null for the usual chances made 5 times smaller per one owned.
	 */
	private final int[] chancesByOwned;
	// Kept in memory only: a restart loses under 1,000 XP towards the next roll.
	private long xpProgress;

	private RareUnlock(CharacterClass characterClass, Set<Skill> skills, boolean levelUpRolls, boolean morytaniaOnly,
		int[] chancesByOwned)
	{
		this.chancesByOwned = chancesByOwned;
		this.characterClass = characterClass;
		this.skills = skills;
		this.levelUpRolls = levelUpRolls;
		this.morytaniaOnly = morytaniaOnly;
	}

	/** Rolls on XP and level-ups in these skills, anywhere. */
	public static RareUnlock fromSkills(CharacterClass characterClass, Skill first, Skill... rest)
	{
		return new RareUnlock(characterClass, EnumSet.of(first, rest), true, false, null);
	}

	/**
	 * Rolls on XP and level-ups in every skill except combat, with its own chance for each one
	 * (e.g. the 1st, 2nd and 3rd Skiller). No more join after the last chance.
	 */
	public static RareUnlock fromSkilling(CharacterClass characterClass, int... chancesByOwned)
	{
		return new RareUnlock(characterClass, EnumSet.complementOf(EnumSet.copyOf(COMBAT_SKILLS)), true, false,
			chancesByOwned);
	}

	/**
	 * Rolls on XP and level-ups in combat skills, with its own chance for each one (e.g. the
	 * 2nd Adventurer). No more join after the last chance.
	 */
	public static RareUnlock fromCombat(CharacterClass characterClass, int... chancesByOwned)
	{
		return new RareUnlock(characterClass, EnumSet.copyOf(COMBAT_SKILLS), true, false, chancesByOwned);
	}

	/** Rolls on XP in any skill, but only while the player is in Morytania. No level-up rolls. */
	public static RareUnlock fromMorytania(CharacterClass characterClass)
	{
		return new RareUnlock(characterClass, EnumSet.allOf(Skill.class), false, true, null);
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
		int xpChance = chancesByOwned != null ? ownChance(owned) : chance(XP_STEP_CHANCE, owned);
		int levelUpChance = chancesByOwned != null ? ownChance(owned) : chance(LEVEL_UP_CHANCE, owned);
		boolean found = false;
		xpProgress += xpGained;
		while (xpProgress >= XP_STEP)
		{
			xpProgress -= XP_STEP;
			found |= random.nextInt(ONE_IN) < xpChance;
		}
		for (int i = 0; levelUpRolls && i < levelsGained; i++)
		{
			found |= random.nextInt(ONE_IN) < levelUpChance;
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

	/**
	 * One roll for casting Low or High Level Alchemy, towards an Alchemist.
	 *
	 * @param owned how many Alchemists are already on the team
	 */
	public static boolean rollAlch(int owned, Random random)
	{
		return hits(ALCH_CHANCE, owned, random);
	}

	/** This unlock's own chance with this many already owned, or 0 once they've all joined. */
	int ownChance(int owned)
	{
		return owned < chancesByOwned.length ? chancesByOwned[owned] : 0;
	}

	/** A chance (out of {@link #ONE_IN}), made 5 times smaller for each one already owned. */
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
