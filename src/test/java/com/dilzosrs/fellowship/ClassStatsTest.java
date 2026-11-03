package com.dilzosrs.fellowship;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class ClassStatsTest
{
	@Test
	public void classStatsAddToGear()
	{
		FellowshipCharacter knight = new FellowshipCharacter(CharacterClass.KNIGHT, 0);
		assertEquals(1, knight.getAtk());
		assertEquals(3, knight.getDef());
		knight.equip(new GearItem(GearType.SWORD, Rarity.UNCOMMON));
		assertEquals(4, knight.getAtk());
	}

	@Test
	public void adventurersHaveNoBaseStats()
	{
		FellowshipCharacter adventurer = new FellowshipCharacter(CharacterClass.ADVENTURER, 0);
		assertEquals(0, adventurer.getAtk());
		assertEquals(0, adventurer.getDef());
		assertEquals(0, adventurer.getMag());
		assertEquals("", CharacterClass.ADVENTURER.describeBaseStats());
	}

	@Test
	public void negativeDefAndMagSoftenIt()
	{
		// Blood Mage: 4 ATK, -4 DEF, 6 MAG works out to 7 ATK, -1 DEF on expeditions.
		FellowshipCharacter bloodMage = new FellowshipCharacter(CharacterClass.BLOOD_MAGE, 0);
		assertEquals(-4, bloodMage.getDef());
		assertEquals(7.0, bloodMage.getEffectiveAtk(), 0.001);
		assertEquals(-1.0, bloodMage.getEffectiveDef(), 0.001);
		assertEquals("+4 ATK, -4 DEF, +6 MAG", CharacterClass.BLOOD_MAGE.describeBaseStats());
	}

	@Test
	public void eachT2NetsItsBudget()
	{
		// The design's "net total": Adventurer t2s 4, Sailor t2s 5, Vyre t2s 6.
		for (CharacterClass c : CharacterClass.ADVENTURER.getEvolutions())
		{
			assertEquals(4, net(c));
		}
		for (CharacterClass c : CharacterClass.SAILOR.getEvolutions())
		{
			assertEquals(5, net(c));
		}
		for (CharacterClass c : CharacterClass.VYRE.getEvolutions())
		{
			assertEquals(6, net(c));
		}
	}

	@Test
	public void shadowLordIsShownAsShadowStalker()
	{
		assertEquals("Shadow Stalker", CharacterClass.SHADOW_LORD.getDisplayName());
	}

	@Test
	public void swampCavesIsThreatThree()
	{
		assertEquals(3, Expedition.LUMBRIDGE_SWAMP_CAVES.getThreatLevel());
	}

	private static int net(CharacterClass c)
	{
		return c.getBaseAtk() + c.getBaseDef() + c.getBaseMag();
	}
}
