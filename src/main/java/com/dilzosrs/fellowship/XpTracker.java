package com.dilzosrs.fellowship;

import java.util.EnumMap;
import java.util.Map;
import net.runelite.api.Skill;

/**
 * Works out how much XP was gained each time a skill's total XP changes.
 * <p>
 * The first value seen for a skill is only remembered, not counted. On login the
 * game reports every skill's total XP, and we don't want to count all of that.
 */
public class XpTracker
{
	private final Map<Skill, Integer> lastXp = new EnumMap<>(Skill.class);

	/**
	 * @return the XP gained since this skill was last seen, or 0 the first time it is seen
	 */
	public int update(Skill skill, int xp)
	{
		Integer previous = lastXp.put(skill, xp);
		if (previous == null)
		{
			return 0;
		}
		return Math.max(0, xp - previous);
	}

	/**
	 * Forget everything, so the next value for each skill is treated as a fresh snapshot.
	 */
	public void reset()
	{
		lastXp.clear();
	}
}
