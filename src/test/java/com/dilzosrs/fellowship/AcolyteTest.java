package com.dilzosrs.fellowship;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.util.List;
import org.junit.Test;

public class AcolyteTest
{
	private static FellowshipCharacter fresh(CharacterClass characterClass)
	{
		return new FellowshipCharacter(characterClass, 0);
	}

	@Test
	public void anAcolyteChoosesAGod()
	{
		FellowshipCharacter acolyte = new FellowshipCharacter(CharacterClass.ACOLYTE,
			LevelCurve.xpForLevel(CharacterClass.ACOLYTE.getLevelCap()));
		assertTrue(acolyte.canEvolve());
		assertFalse(acolyte.evolve(CharacterClass.KNIGHT));
		assertTrue(acolyte.evolve(CharacterClass.ZAMORAKIAN));
		assertEquals(1, acolyte.getLevel());
		assertEquals(Blessing.ZAMORAK, acolyte.getCharacterClass().getBlessing());
	}

	/** A Knight (no gear, so no stats of its own) with one worshipper staying at base. */
	private static Team knightWith(CharacterClass worshipper)
	{
		return new Team(List.of(fresh(CharacterClass.KNIGHT), fresh(worshipper)));
	}

	@Test
	public void blessingsHelpExpeditionsWhileTheWorshipperStaysAtBase()
	{
		// Only the Knight (position 0) goes, so everything comes from the blessings.
		List<Integer> knightAlone = List.of(0);
		// Zamorak: +3 ATK.
		assertEquals(3, knightWith(CharacterClass.ZAMORAKIAN).averageAtk(knightAlone), 0.001);
		assertEquals(0, knightWith(CharacterClass.ZAMORAKIAN).averageDef(knightAlone), 0.001);
		// Saradomin: +3 DEF.
		assertEquals(3, knightWith(CharacterClass.SARADOMINIST).averageDef(knightAlone), 0.001);
		// Zaros: +3 MAG, which counts half to ATK and half to DEF.
		assertEquals(1.5, knightWith(CharacterClass.ZAROSIAN).averageAtk(knightAlone), 0.001);
		assertEquals(1.5, knightWith(CharacterClass.ZAROSIAN).averageDef(knightAlone), 0.001);
		// Guthix: +1 to every stat, so 1.5 ATK and 1.5 DEF with MAG's halves.
		assertEquals(1.5, knightWith(CharacterClass.GUTHIXIAN).averageAtk(knightAlone), 0.001);
		assertEquals(1.5, knightWith(CharacterClass.GUTHIXIAN).averageDef(knightAlone), 0.001);
	}

	@Test
	public void differentGodsStack()
	{
		Team team = new Team(List.of(fresh(CharacterClass.KNIGHT), fresh(CharacterClass.ZAMORAKIAN),
			fresh(CharacterClass.ZAROSIAN)));
		assertEquals(4.5, team.averageAtk(List.of(0)), 0.001);
	}

	@Test
	public void aGodBlessesOnlyOnce()
	{
		Team team = new Team(List.of(fresh(CharacterClass.KNIGHT), fresh(CharacterClass.ZAMORAKIAN),
			fresh(CharacterClass.ZAMORAKIAN)));
		assertEquals(3, team.averageAtk(List.of(0)), 0.001);
	}

	@Test
	public void noBlessingWithoutAWorshipper()
	{
		Team team = knightWith(CharacterClass.ACOLYTE);
		assertEquals(0, team.averageAtk(List.of(0)), 0.001);
		assertTrue(team.blessings().isEmpty());
	}
}
