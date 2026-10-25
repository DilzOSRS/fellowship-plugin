package com.dilzosrs.fellowship;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import net.runelite.api.Skill;

/**
 * The classes a Fellowship character can be. Every character starts as an Adventurer.
 * <p>
 * The enum names (ADVENTURER, KNIGHT, ...) are saved per account, so never rename them
 * without a migration. The display names are safe to change.
 */
public enum CharacterClass
{
	ADVENTURER("Adventurer", 10),
	KNIGHT("Knight", LevelCurve.MAX_LEVEL, Skill.ATTACK, Skill.STRENGTH),
	ARCHER("Archer", LevelCurve.MAX_LEVEL, Skill.RANGED),
	WIZARD("Wizard", LevelCurve.MAX_LEVEL, Skill.MAGIC);

	/** XP from a class's bonus skills is multiplied by this. */
	static final int BONUS_XP_MULTIPLIER = 2;

	private final String displayName;
	private final int levelCap;
	private final Set<Skill> bonusSkills;

	CharacterClass(String displayName, int levelCap, Skill... bonusSkills)
	{
		this.displayName = displayName;
		this.levelCap = levelCap;
		this.bonusSkills = bonusSkills.length == 0
			? EnumSet.noneOf(Skill.class)
			: EnumSet.of(bonusSkills[0], bonusSkills);
	}

	public String getDisplayName()
	{
		return displayName;
	}

	/** A character of this class can't go past this level. XP earned at the cap is wasted. */
	public int getLevelCap()
	{
		return levelCap;
	}

	/** How much Fellowship XP this class gets per OSRS XP in the given skill. */
	public int getXpMultiplier(Skill skill)
	{
		return bonusSkills.contains(skill) ? BONUS_XP_MULTIPLIER : 1;
	}

	/** The classes this class can evolve into once it reaches its level cap. */
	public List<CharacterClass> getEvolutions()
	{
		if (this == ADVENTURER)
		{
			return List.of(KNIGHT, ARCHER, WIZARD);
		}
		return Collections.emptyList();
	}
}
