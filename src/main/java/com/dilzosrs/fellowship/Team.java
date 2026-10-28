package com.dilzosrs.fellowship;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
		hire(CharacterClass.ADVENTURER);
	}

	/** Add a fresh level 1 character of a starting class (Adventurer, Skiller, Thief, Acolyte or Vyre). */
	public void hire(CharacterClass startingClass)
	{
		characters.add(new FellowshipCharacter(startingClass, 0));
	}

	/** How many characters of a class are on the team. */
	public int count(CharacterClass characterClass)
	{
		int count = 0;
		for (FellowshipCharacter character : characters)
		{
			if (character.getCharacterClass() == characterClass)
			{
				count++;
			}
		}
		return count;
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

	/**
	 * Send several characters on an expedition together. They share a start time, which is how
	 * they're known to be one party later (even after a restart).
	 *
	 * @return false (and nobody is sent) if the party is the wrong size, has repeats, or anyone is
	 *     away or resting from an injury
	 */
	public boolean sendParty(List<Integer> indexes, Expedition expedition, Instant start)
	{
		if (indexes.size() != expedition.getPartySize() || new HashSet<>(indexes).size() != indexes.size())
		{
			return false;
		}
		for (int index : indexes)
		{
			if (index < 0 || index >= characters.size() || !characters.get(index).isAvailable(start))
			{
				return false;
			}
		}
		for (int index : indexes)
		{
			characters.get(index).startExpedition(expedition, start);
		}
		return true;
	}

	/**
	 * Everyone on the same trip as this character (same expedition and start time), including it.
	 *
	 * @return their positions in the team, or an empty list if the character is home
	 */
	public List<Integer> partyOf(int index)
	{
		FellowshipCharacter character = characters.get(index);
		if (!character.isAway())
		{
			return Collections.emptyList();
		}
		List<Integer> party = new ArrayList<>();
		for (int i = 0; i < characters.size(); i++)
		{
			FellowshipCharacter other = characters.get(i);
			if (other.getExpedition() == character.getExpedition()
				&& character.getExpeditionStart().equals(other.getExpeditionStart()))
			{
				party.add(i);
			}
		}
		return party;
	}

	/**
	 * Bring a finished trip home. On a success every party member gets the XP reward; on a
	 * failure they come back with nothing. The caller handles the coins and bonus bundle,
	 * which are rolled once per party.
	 *
	 * @return the positions of the members who came home, or an empty list if it isn't finished
	 */
	public List<Integer> collectParty(int index, Instant now, boolean success)
	{
		if (index < 0 || index >= characters.size() || !characters.get(index).isExpeditionDone(now))
		{
			return Collections.emptyList();
		}
		List<Integer> party = partyOf(index);
		for (int member : party)
		{
			characters.get(member).collectExpedition(now, success);
		}
		return party;
	}

	/**
	 * The party's ATK against a threat: the average of its members' effective ATK, plus the
	 * blessings of the gods the team worships.
	 */
	public double averageAtk(List<Integer> party)
	{
		double bonus = blessings().stream().mapToDouble(Blessing::getEffectiveAtk).sum();
		return party.stream().mapToDouble(i -> characters.get(i).getEffectiveAtk() + bonus).average().orElse(0);
	}

	/** The party's DEF against a threat: the average of {@link #memberDefs}. */
	public double averageDef(List<Integer> party)
	{
		return Arrays.stream(memberDefs(party)).average().orElse(0);
	}

	/** Each member's effective DEF, plus the blessings of the gods the team worships. */
	public double[] memberDefs(List<Integer> party)
	{
		double bonus = blessings().stream().mapToDouble(Blessing::getEffectiveDef).sum();
		return party.stream().mapToDouble(i -> characters.get(i).getEffectiveDef() + bonus).toArray();
	}

	/**
	 * The gods worshipped by anyone on the team, wherever they are. They bless every expedition.
	 * Each god counts once, however many worshippers it has.
	 */
	public Set<Blessing> blessings()
	{
		Set<Blessing> blessings = EnumSet.noneOf(Blessing.class);
		for (FellowshipCharacter character : characters)
		{
			Blessing blessing = character.getCharacterClass().getBlessing();
			if (blessing != null)
			{
				blessings.add(blessing);
			}
		}
		return blessings;
	}

	/**
	 * Characters are numbered within their class, in team order: the first Wizard is just
	 * "Wizard", the second is "Wizard 2". Names can change when someone takes a class advancement.
	 */
	public String getDisplayName(int index)
	{
		CharacterClass characterClass = characters.get(index).getCharacterClass();
		int number = 1;
		for (int i = 0; i < index; i++)
		{
			if (characters.get(i).getCharacterClass() == characterClass)
			{
				number++;
			}
		}
		return number == 1 ? characterClass.getDisplayName() : characterClass.getDisplayName() + " " + number;
	}
}
