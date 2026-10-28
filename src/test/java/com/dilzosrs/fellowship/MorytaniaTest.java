package com.dilzosrs.fellowship;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class MorytaniaTest
{
	@Test
	public void morytaniaPlaces()
	{
		assertTrue(Morytania.contains(3493, 3488)); // Canifis
		assertTrue(Morytania.contains(3680, 3470)); // Port Phasmatys
		assertTrue(Morytania.contains(3565, 3290)); // Barrows
		assertTrue(Morytania.contains(3500, 3210)); // Burgh de Rott
		assertTrue(Morytania.contains(3680, 3000)); // Mos Le'Harmless
	}

	@Test
	public void undergroundCountsLikeTheSurfaceAbove()
	{
		assertTrue(Morytania.contains(3552, 9694)); // Barrows crypts
		assertFalse(Morytania.contains(3222, 9618)); // Lumbridge cellar
	}

	@Test
	public void placesOutsideMorytania()
	{
		assertFalse(Morytania.contains(3222, 3218)); // Lumbridge
		assertFalse(Morytania.contains(3212, 3428)); // Varrock
		assertFalse(Morytania.contains(3300, 3180)); // Al Kharid
		assertFalse(Morytania.contains(3350, 3600)); // Wilderness, west of the river
		assertFalse(Morytania.contains(3450, 2900)); // Kharidian Desert
	}
}
