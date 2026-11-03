package com.dilzosrs.fellowship;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.util.List;
import java.util.Random;
import net.runelite.api.Skill;
import org.junit.Test;

public class SailorTest
{
	@Test
	public void sailorsGetDoubleSailingAndCapAtTen()
	{
		assertEquals(2, CharacterClass.SAILOR.getXpMultiplier(Skill.SAILING));
		assertEquals(1, CharacterClass.SAILOR.getXpMultiplier(Skill.ATTACK));
		assertEquals(10, CharacterClass.SAILOR.getLevelCap());
	}

	@Test
	public void sailorsAdvanceIntoThreeCombatClasses()
	{
		assertEquals(List.of(CharacterClass.SWASHBUCKLER, CharacterClass.HARPOONER, CharacterClass.NAVIGATOR),
			CharacterClass.SAILOR.getEvolutions());
		for (CharacterClass path : CharacterClass.SAILOR.getEvolutions())
		{
			// 2x Sailing, 1x everything else, including their own combat style.
			assertEquals(2, path.getXpMultiplier(Skill.SAILING));
			assertEquals(1, path.getXpMultiplier(Skill.STRENGTH));
			assertEquals(1, path.getXpMultiplier(Skill.RANGED));
			assertEquals(1, path.getXpMultiplier(Skill.MAGIC));
			assertEquals(LevelCurve.MAX_LEVEL, path.getLevelCap());
			assertEquals(CharacterClass.SAILOR, path.getBaseClass());
		}

		FellowshipCharacter sailor = new FellowshipCharacter(CharacterClass.SAILOR, LevelCurve.xpForLevel(10));
		assertTrue(sailor.evolve(CharacterClass.NAVIGATOR));
		assertEquals(CharacterClass.NAVIGATOR, sailor.getCharacterClass());
		assertEquals(1, sailor.getLevel());
	}

	@Test
	public void sailorPathsWearLeatherAndOneWeaponEach()
	{
		for (CharacterClass path : CharacterClass.SAILOR.getEvolutions())
		{
			assertTrue(GearType.LEATHER.canBeUsedBy(path));
			assertTrue(GearType.TRINKET.canBeUsedBy(path));
			assertFalse(GearType.PLATE.canBeUsedBy(path));
			assertFalse(GearType.ROBE.canBeUsedBy(path));
		}
		assertTrue(GearType.SWORD.canBeUsedBy(CharacterClass.SWASHBUCKLER));
		assertFalse(GearType.BOW.canBeUsedBy(CharacterClass.SWASHBUCKLER));
		assertTrue(GearType.BOW.canBeUsedBy(CharacterClass.HARPOONER));
		assertFalse(GearType.STAFF.canBeUsedBy(CharacterClass.HARPOONER));
		assertTrue(GearType.STAFF.canBeUsedBy(CharacterClass.NAVIGATOR));
		assertFalse(GearType.SWORD.canBeUsedBy(CharacterClass.NAVIGATOR));
		// Since tags (2026-11-02) the base Sailor wears leather too, but no weapon until nautical ones exist.
		assertTrue(GearType.LEATHER.canBeUsedBy(CharacterClass.SAILOR));
		assertFalse(GearType.SWORD.canBeUsedBy(CharacterClass.SAILOR));
	}

	@Test
	public void sailorsJoinFromSailingXpOnly()
	{
		RareUnlock unlock = RareUnlock.fromSkills(CharacterClass.SAILOR, Skill.SAILING);
		assertFalse(unlock.roll(Skill.FISHING, 50_000, 5, 0, always(0)));
		assertTrue(unlock.roll(Skill.SAILING, 1_000, 0, 0, always(0)));
	}

	private static Random always(int value)
	{
		return new Random()
		{
			@Override
			public int nextInt(int bound)
			{
				return Math.min(value, bound - 1);
			}
		};
	}
}
