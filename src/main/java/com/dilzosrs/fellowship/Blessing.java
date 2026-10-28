package com.dilzosrs.fellowship;

/**
 * A god's flat buff to every character sent on an expedition, while one of its worshippers is on the team.
 * Each god's blessing counts once, however many worshippers it has. The worshipper can stay at base.
 * <p>
 * Placeholder buffs (2026-10-28) until the user decides what worshipping each god does.
 */
public enum Blessing
{
	GUTHIX("Guthix", 1, 1, 1),
	SARADOMIN("Saradomin", 0, 3, 0),
	ZAMORAK("Zamorak", 3, 0, 0),
	ZAROS("Zaros", 0, 0, 3);

	private final String godName;
	private final int atk;
	private final int def;
	private final int mag;

	Blessing(String godName, int atk, int def, int mag)
	{
		this.godName = godName;
		this.atk = atk;
		this.def = def;
		this.mag = mag;
	}

	public String getGodName()
	{
		return godName;
	}

	/** Like a character's effective ATK: ATK plus half of MAG. */
	public double getEffectiveAtk()
	{
		return atk + mag / 2.0;
	}

	/** Like a character's effective DEF: DEF plus half of MAG. */
	public double getEffectiveDef()
	{
		return def + mag / 2.0;
	}

	/** e.g. "+3 ATK" or "+1 ATK, DEF and MAG", for the card. */
	public String describe()
	{
		if (atk == def && def == mag)
		{
			return "+" + atk + " ATK, DEF and MAG";
		}
		return atk > 0 ? "+" + atk + " ATK" : def > 0 ? "+" + def + " DEF" : "+" + mag + " MAG";
	}
}
