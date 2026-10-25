package com.dilzosrs.fellowship;

/**
 * A Fellowship character. Its level comes from its total Fellowship XP, and its class
 * sets its level cap.
 */
public class FellowshipCharacter
{
	private CharacterClass characterClass;
	private long xp;

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
		return isAtLevelCap() && !characterClass.getEvolutions().isEmpty();
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

	private long maxXp()
	{
		return LevelCurve.xpForLevel(getLevelCap());
	}
}
