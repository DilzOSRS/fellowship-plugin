package com.dilzosrs.fellowship;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.junit.Test;

public class SailingVoyageTest
{
	private static final Instant START = Instant.parse("2026-11-03T12:00:00Z");
	private static final Expedition VOYAGE = Expedition.SAILING_VOYAGE;

	private static Team sailorAndTwoOthers()
	{
		return new Team(List.of(new FellowshipCharacter(CharacterClass.SAILOR, 0),
			new FellowshipCharacter(CharacterClass.KNIGHT, 0),
			new FellowshipCharacter(CharacterClass.ARCHER, 0)));
	}

	@Test
	public void waitsForTheShipASailorAndThreeFollowers()
	{
		Team team = new Team(List.of(new FellowshipCharacter(CharacterClass.SAILOR, 0),
			new FellowshipCharacter(CharacterClass.KNIGHT, 0)));
		team.setVoyagePrepared(true);
		// Only 2 followers.
		assertFalse(VOYAGE.isUnlockedFor(team, 1));

		team = sailorAndTwoOthers();
		assertFalse(VOYAGE.isUnlockedFor(team, 1));
		team.setVoyagePrepared(true);
		assertTrue(VOYAGE.isUnlockedFor(team, 1));

		// No Sailor tree, no voyage, even with a ship.
		Team noSailor = new Team(List.of(new FellowshipCharacter(CharacterClass.KNIGHT, 0),
			new FellowshipCharacter(CharacterClass.ARCHER, 0), new FellowshipCharacter(CharacterClass.WIZARD, 0)));
		noSailor.setVoyagePrepared(true);
		assertFalse(VOYAGE.isUnlockedFor(noSailor, 1));
	}

	@Test
	public void exactlyThreeWithOneFromTheSailorTree()
	{
		assertEquals(15, VOYAGE.getThreatLevel());
		assertEquals(Duration.ofMinutes(90), VOYAGE.getDuration());
		assertEquals(5_000, VOYAGE.getXpReward(3));
		assertTrue(VOYAGE.canLead(CharacterClass.NAVIGATOR));
		assertFalse(VOYAGE.canLead(CharacterClass.KNIGHT));

		Team team = new Team(List.of(new FellowshipCharacter(CharacterClass.KNIGHT, 0),
			new FellowshipCharacter(CharacterClass.ARCHER, 0),
			new FellowshipCharacter(CharacterClass.WIZARD, 0),
			new FellowshipCharacter(CharacterClass.SAILOR, 0)));
		assertFalse(team.sendParty(List.of(0, 1, 2), VOYAGE, START));
		assertFalse(team.sendParty(List.of(3, 0), VOYAGE, START));
		assertTrue(team.sendParty(List.of(3, 0, 1), VOYAGE, START));
	}

	@Test
	public void threeStagesOfEqualLength()
	{
		Duration trip = Duration.ofMinutes(90);
		assertEquals("Stage 1/3: Sailing to the dungeon", VOYAGE.getStageName(Duration.ofMinutes(90), trip));
		assertEquals("Stage 1/3: Sailing to the dungeon", VOYAGE.getStageName(Duration.ofMinutes(61), trip));
		assertEquals("Stage 2/3: In the dungeon", VOYAGE.getStageName(Duration.ofMinutes(60), trip));
		assertEquals("Stage 3/3: Sailing home", VOYAGE.getStageName(Duration.ofMinutes(29), trip));
		assertEquals("Stage 3/3: Sailing home", VOYAGE.getStageName(Duration.ZERO, trip));
		// A Skiller shortens the whole trip, so each stage is shorter too: 67.5 min, 22.5 per stage.
		assertEquals("Stage 2/3: In the dungeon",
			VOYAGE.getStageName(Duration.ofMinutes(44), VOYAGE.getDuration(3, 1)));
		// Other expeditions have no stages.
		assertNull(Expedition.DORGESHUUN_CAVES.getStageName(Duration.ofMinutes(10), Duration.ofMinutes(30)));
	}

	@Test
	public void everySuccessBringsASailingBundle()
	{
		assertEquals(BundleType.SAILING, VOYAGE.rollBonusBundle(new java.util.Random()));
	}

	@Test
	public void theSailingBundleHoldsNauticalWeaponsAtUncommonAndRare()
	{
		List<GearItem> pool = BundleType.SAILING.getPoolItems();
		assertTrue(pool.contains(new GearItem(GearType.ANCHOR, Rarity.UNCOMMON)));
		assertTrue(pool.contains(new GearItem(GearType.TRIDENT, Rarity.RARE)));
		assertTrue(pool.contains(new GearItem(GearType.PLATE, Rarity.RARE)));
		assertTrue(pool.contains(new GearItem(GearType.DAGGER, Rarity.COMMON)));
		// Only armour from the Adventurer gear, and no trinkets.
		assertFalse(pool.contains(new GearItem(GearType.SWORD, Rarity.COMMON)));
		assertFalse(pool.stream().anyMatch(item -> item.getType() == GearType.TRINKET));
	}
}
