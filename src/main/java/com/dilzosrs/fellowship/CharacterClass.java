package com.dilzosrs.fellowship;

import java.util.Collections;
import java.util.List;

/**
 * The classes a Fellowship character can be. Every character starts as an Adventurer.
 * <p>
 * The enum names (ADVENTURER, KNIGHT, ...) are saved per account, so never rename them
 * without a migration. The display names are safe to change.
 */
public enum CharacterClass
{
	ADVENTURER("Adventurer", 10),
	KNIGHT("Knight", LevelCurve.MAX_LEVEL),
	ARCHER("Archer", LevelCurve.MAX_LEVEL),
	WIZARD("Wizard", LevelCurve.MAX_LEVEL);

	private final String displayName;
	private final int levelCap;

	CharacterClass(String displayName, int levelCap)
	{
		this.displayName = displayName;
		this.levelCap = levelCap;
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
