package com.dilzosrs.fellowship;

/**
 * Whether a map tile is in Morytania, for the Vyre unlock. Rough boxes around the region, so the
 * edges may be a few tiles off. Reading the player's location is read-only and allowed.
 */
public final class Morytania
{
	/** Underground areas sit this far north of the surface above them on the OSRS map. */
	static final int UNDERGROUND_OFFSET = 6400;

	private Morytania()
	{
	}

	/**
	 * @param x the tile's world x (for instances like the Theatre of Blood, use the template's x)
	 * @param y the tile's world y; underground tiles are mapped back to the surface first
	 */
	public static boolean contains(int x, int y)
	{
		int surfaceY = y >= UNDERGROUND_OFFSET ? y - UNDERGROUND_OFFSET : y;
		return mainland(x, surfaceY) || mosLeHarmless(x, surfaceY);
	}

	/** East of the River Salve: Canifis, Port Phasmatys, Barrows, Burgh de Rott, Darkmeyer, Slepe and more. */
	private static boolean mainland(int x, int y)
	{
		return x >= 3405 && x <= 3905 && y >= 3140 && y <= 3600;
	}

	/** The island south-east of the mainland. */
	private static boolean mosLeHarmless(int x, int y)
	{
		return x >= 3640 && x <= 3860 && y >= 2930 && y <= 3080;
	}
}
