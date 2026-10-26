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
	WIZARD("Wizard", LevelCurve.MAX_LEVEL, Skill.MAGIC),
	// The Skiller starts like the Adventurer (no bonus, cap 10) but advances into skilling paths.
	// The paths' bonus skills are a first guess, to tune after playtesting.
	SKILLER("Skiller", 10),
	HARVESTER("Harvester", LevelCurve.MAX_LEVEL,
		Skill.WOODCUTTING, Skill.MINING, Skill.FISHING, Skill.FARMING, Skill.HUNTER),
	ARTISAN("Artisan", LevelCurve.MAX_LEVEL,
		Skill.SMITHING, Skill.CRAFTING, Skill.FLETCHING, Skill.COOKING, Skill.HERBLORE, Skill.CONSTRUCTION, Skill.FIREMAKING),
	EXPLORER("Explorer", LevelCurve.MAX_LEVEL,
		Skill.AGILITY, Skill.THIEVING, Skill.RUNECRAFT, Skill.PRAYER, Skill.SLAYER);

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
		if (this == SKILLER)
		{
			return List.of(HARVESTER, ARTISAN, EXPLORER);
		}
		return Collections.emptyList();
	}
}
