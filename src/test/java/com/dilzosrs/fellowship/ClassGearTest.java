package com.dilzosrs.fellowship;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class ClassGearTest
{
	@Test
	public void thiefAndVyreTreesWearTaggedGear()
	{
		assertTrue(GearType.SWORD.canBeUsedBy(CharacterClass.THIEF));
		assertTrue(GearType.BOW.canBeUsedBy(CharacterClass.THIEF));
		assertTrue(GearType.LEATHER.canBeUsedBy(CharacterClass.THIEF));
		assertFalse(GearType.STAFF.canBeUsedBy(CharacterClass.THIEF));
		assertFalse(GearType.PLATE.canBeUsedBy(CharacterClass.THIEF));

		assertTrue(GearType.SWORD.canBeUsedBy(CharacterClass.BANDIT));
		assertFalse(GearType.BOW.canBeUsedBy(CharacterClass.BANDIT));
		assertTrue(GearType.BOW.canBeUsedBy(CharacterClass.ASSASSIN));
		assertFalse(GearType.SWORD.canBeUsedBy(CharacterClass.ASSASSIN));

		assertTrue(GearType.SWORD.canBeUsedBy(CharacterClass.VYRE));
		assertTrue(GearType.STAFF.canBeUsedBy(CharacterClass.VYRE));
		assertTrue(GearType.ROBE.canBeUsedBy(CharacterClass.VYRE));
		assertFalse(GearType.BOW.canBeUsedBy(CharacterClass.VYRE));
		assertTrue(GearType.BOW.canBeUsedBy(CharacterClass.SHADOW_LORD));
		assertFalse(GearType.PLATE.canBeUsedBy(CharacterClass.DEATH_KNIGHT));
	}

	@Test
	public void classesWithoutTagsWearGenericOnly()
	{
		for (CharacterClass c : new CharacterClass[]{CharacterClass.ADVENTURER, CharacterClass.SKILLER,
			CharacterClass.HARVESTER, CharacterClass.ACOLYTE, CharacterClass.ZAMORAKIAN, CharacterClass.MERCHANT,
			CharacterClass.ALCHEMIST})
		{
			assertTrue(GearType.TRINKET.canBeUsedBy(c));
			assertFalse(GearType.SWORD.canBeUsedBy(c));
			assertFalse(GearType.LEATHER.canBeUsedBy(c));
		}
	}

	@Test
	public void reasons()
	{
		assertNull(ClassGear.whyCantUse(CharacterClass.KNIGHT, GearType.SWORD));
		assertNull(ClassGear.whyCantUse(CharacterClass.ADVENTURER, GearType.TRINKET));
		assertEquals("Adventurers can only use Generic gear",
			ClassGear.whyCantUse(CharacterClass.ADVENTURER, GearType.SWORD));
		assertEquals("Knights can't use robe gear", ClassGear.whyCantUse(CharacterClass.KNIGHT, GearType.ROBE));
		assertEquals("Thieves can't use staff gear", ClassGear.whyCantUse(CharacterClass.THIEF, GearType.STAFF));
	}

	@Test
	public void darkTagIsOnTheRightTrees()
	{
		// No dark items exist yet, so the Bandit's "dark melee only" limit gets its test with them (step 3).
		assertTrue(ClassGear.of(CharacterClass.BANDIT).getTags().contains(Tag.DARK));
		assertTrue(ClassGear.of(CharacterClass.VYRE).getTags().contains(Tag.DARK));
		assertFalse(ClassGear.of(CharacterClass.KNIGHT).getTags().contains(Tag.DARK));
	}

	@Test
	public void comboItemsNeedTheStyleToo()
	{
		assertTrue(GearType.CUTLASS.canBeUsedBy(CharacterClass.SWASHBUCKLER));
		assertFalse(GearType.CUTLASS.canBeUsedBy(CharacterClass.HARPOONER));
		assertFalse(GearType.CUTLASS.canBeUsedBy(CharacterClass.SAILOR));
		assertTrue(GearType.HARPOON.canBeUsedBy(CharacterClass.HARPOONER));
		assertTrue(GearType.TRIDENT.canBeUsedBy(CharacterClass.NAVIGATOR));
		// The Anchor is plain nautical, so the whole Sailor tree can use it.
		assertTrue(GearType.ANCHOR.canBeUsedBy(CharacterClass.SAILOR));
		assertTrue(GearType.ANCHOR.canBeUsedBy(CharacterClass.HARPOONER));
		assertFalse(GearType.ANCHOR.canBeUsedBy(CharacterClass.KNIGHT));
		assertEquals("Harpooners can't use nautical-melee gear",
			ClassGear.whyCantUse(CharacterClass.HARPOONER, GearType.CUTLASS));
	}

	@Test
	public void whoCanUse()
	{
		assertEquals("Anyone", ClassGear.whoCanUse(GearType.TRINKET));
		assertEquals("Knight", ClassGear.whoCanUse(GearType.PLATE));
		assertEquals("Wizard, Vyre, Blood Mage, Navigator", ClassGear.whoCanUse(GearType.STAFF));
	}
}
