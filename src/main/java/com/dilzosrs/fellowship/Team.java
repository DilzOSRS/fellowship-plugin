package com.dilzosrs.fellowship;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.runelite.api.Skill;

/**
 * The account's characters. The active character gets full XP from OSRS play, the rest get 10%.
 */
public class Team
{
	static final int ACTIVE_XP_PERCENT = 100;
	static final int INACTIVE_XP_PERCENT = 10;

	private final List<FellowshipCharacter> characters;
	private int activeIndex;

	/**
	 * @param characters the saved characters; an empty list starts the team with one fresh Adventurer
	 */
	public Team(List<FellowshipCharacter> characters)
	{
		this(characters, 0);
	}

	/**
	 * @param characters the saved characters; an empty list starts the team with one fresh Adventurer
	 * @param activeIndex which character is active; anything out of range falls back to the first
	 */
	public Team(List<FellowshipCharacter> characters, int activeIndex)
	{
		this.characters = new ArrayList<>(characters);
		if (this.characters.isEmpty())
		{
			this.characters.add(new FellowshipCharacter(CharacterClass.ADVENTURER, 0));
		}
		this.activeIndex = activeIndex >= 0 && activeIndex < this.characters.size() ? activeIndex : 0;
	}

	public List<FellowshipCharacter> getCharacters()
	{
		return Collections.unmodifiableList(characters);
	}

	public FellowshipCharacter get(int index)
	{
		return characters.get(index);
	}

	public int size()
	{
		return characters.size();
	}

	/**
	 * Add a fresh Adventurer to the team. There's no team size limit; the Guild decides
	 * when a hire is allowed, and the caller takes the hire and the coins.
	 */
	public void hire()
	{
		characters.add(new FellowshipCharacter(CharacterClass.ADVENTURER, 0));
	}

	/** Which character gets full XP. */
	public int getActiveIndex()
	{
		return activeIndex;
	}

	/**
	 * Make a different character active. Switching is free and can be done any time.
	 *
	 * @return false if there's no character there, or it's already active
	 */
	public boolean setActive(int index)
	{
		if (index < 0 || index >= characters.size() || index == activeIndex)
		{
			return false;
		}
		activeIndex = index;
		return true;
	}

	/**
	 * Share OSRS XP gained in a skill out to the team: full XP for the active character, 10% for the rest.
	 * Characters who are away get nothing, so if the active one is away, nobody gets full XP.
	 *
	 * @return how many levels the whole team gained (for Guild XP)
	 */
	public int gainXp(Skill skill, long osrsXp)
	{
		int levelsGained = 0;
		for (int i = 0; i < characters.size(); i++)
		{
			int percent = i == getActiveIndex() ? ACTIVE_XP_PERCENT : INACTIVE_XP_PERCENT;
			levelsGained += characters.get(i).gainXp(skill, osrsXp, percent);
		}
		return levelsGained;
	}

	/** e.g. "Knight" for the first character, "Adventurer 2" and "Adventurer 3" for hires. */
	public String getDisplayName(int index)
	{
		String className = characters.get(index).getCharacterClass().getDisplayName();
		return index == 0 ? className : className + " " + (index + 1);
	}
}
