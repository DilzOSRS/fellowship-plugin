package com.dilzosrs.fellowship;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import com.google.gson.Gson;
import java.time.Instant;
import org.junit.Test;

public class SavedCharacterTest
{
	private static final Instant START = Instant.parse("2026-10-25T20:00:00Z");

	// The plugin uses RuneLite's Gson; a plain one is fine for checking the JSON round trip.
	private final Gson gson = new Gson();

	@Test
	public void characterAtHomeSurvivesSavingAndLoading()
	{
		FellowshipCharacter original = new FellowshipCharacter(CharacterClass.WIZARD, 12_345);

		FellowshipCharacter loaded = roundTrip(original);

		assertEquals(CharacterClass.WIZARD, loaded.getCharacterClass());
		assertEquals(12_345, loaded.getXp());
		assertFalse(loaded.isAway());
	}

	@Test
	public void characterOnAnExpeditionSurvivesSavingAndLoading()
	{
		FellowshipCharacter original = new FellowshipCharacter(CharacterClass.ADVENTURER, 500);
		original.startExpedition(Expedition.LUMBRIDGE_SWAMP_CAVES, START);

		FellowshipCharacter loaded = roundTrip(original);

		assertTrue(loaded.isAway());
		assertEquals(Expedition.LUMBRIDGE_SWAMP_CAVES, loaded.getExpedition());
		assertEquals(START, loaded.getExpeditionStart());
	}

	@Test
	public void wholeTeamSurvivesSavingAndLoading()
	{
		SavedCharacter[] team = {
			SavedCharacter.from(new FellowshipCharacter(CharacterClass.KNIGHT, 100)),
			SavedCharacter.from(new FellowshipCharacter(CharacterClass.ADVENTURER, 200)),
		};

		SavedCharacter[] loaded = gson.fromJson(gson.toJson(team), SavedCharacter[].class);

		assertEquals(2, loaded.length);
		assertEquals(CharacterClass.KNIGHT, loaded[0].toCharacter().getCharacterClass());
		assertEquals(200, loaded[1].toCharacter().getXp());
	}

	@Test
	public void unknownClassFallsBackToAdventurer()
	{
		SavedCharacter saved = gson.fromJson("{\"characterClass\":\"DRAGON_RIDER\",\"xp\":50}", SavedCharacter.class);
		FellowshipCharacter loaded = saved.toCharacter();
		assertEquals(CharacterClass.ADVENTURER, loaded.getCharacterClass());
		assertEquals(50, loaded.getXp());
	}

	@Test
	public void unknownExpeditionIsIgnored()
	{
		SavedCharacter saved = gson.fromJson(
			"{\"characterClass\":\"ARCHER\",\"xp\":50,\"expedition\":\"MOON\",\"expeditionStart\":1}", SavedCharacter.class);
		FellowshipCharacter loaded = saved.toCharacter();
		assertFalse(loaded.isAway());
		assertNull(loaded.getExpedition());
	}

	private FellowshipCharacter roundTrip(FellowshipCharacter character)
	{
		String json = gson.toJson(SavedCharacter.from(character));
		return gson.fromJson(json, SavedCharacter.class).toCharacter();
	}
}
