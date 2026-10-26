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
	public static final int MAX_SIZE = 3;
	public static final int RECRUIT_COST = 100;
	static final int ACTIVE_XP_PERCENT = 100;
	static final int INACTIVE_XP_PERCENT = 10;

	private final List<FellowshipCharacter> characters;

	/**
	 * @param characters the saved characters; an empty list starts the team with one fresh Adventurer
	 */
	public Team(List<FellowshipCharacter> characters)
	{
		this.characters = new ArrayList<>(characters);
		if (this.characters.isEmpty())
		{
			this.characters.add(new FellowshipCharacter(CharacterClass.ADVENTURER, 0));
		}
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

	public boolean isFull()
	{
		return characters.size() >= MAX_SIZE;
	}

	public boolean canRecruit(long coins)
	{
		return !isFull() && coins >= RECRUIT_COST;
	}

	/**
	 * Add a fresh Adventurer to the team. The caller takes the coins.
	 *
	 * @return false if the team is already full
	 */
	public boolean recruit()
	{
		if (isFull())
		{
			return false;
		}
		characters.add(new FellowshipCharacter(CharacterClass.ADVENTURER, 0));
		return true;
	}

	/** Which character gets full XP. For now always the first one; step 3b.3 lets the player choose. */
	public int getActiveIndex()
	{
		return 0;
	}

	/**
	 * Share OSRS XP gained in a skill out to the team: full XP for the active character, 10% for the rest.
	 */
	public void gainXp(Skill skill, long osrsXp)
	{
		for (int i = 0; i < characters.size(); i++)
		{
			int percent = i == getActiveIndex() ? ACTIVE_XP_PERCENT : INACTIVE_XP_PERCENT;
			characters.get(i).gainXp(skill, osrsXp, percent);
		}
	}

	/** e.g. "Knight" for the first character, "Adventurer 2" and "Adventurer 3" for recruits. */
	public String getDisplayName(int index)
	{
		String className = characters.get(index).getCharacterClass().getDisplayName();
		return index == 0 ? className : className + " " + (index + 1);
	}
}
