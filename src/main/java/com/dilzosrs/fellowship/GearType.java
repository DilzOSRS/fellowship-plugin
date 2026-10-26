package com.dilzosrs.fellowship;

/**
 * The kinds of gear. Each kind goes in one slot, gives one stat, and is either for one
 * class or Generic (anyone can use it, including Adventurers).
 * <p>
 * The enum names are saved per account, so never rename them without a migration.
 */
public enum GearType
{
	SWORD("Sword", Slot.WEAPON, CharacterClass.KNIGHT),
	PLATE("Plate", Slot.ARMOUR, CharacterClass.KNIGHT),
	BOW("Bow", Slot.WEAPON, CharacterClass.ARCHER),
	LEATHER("Leather", Slot.ARMOUR, CharacterClass.ARCHER),
	STAFF("Staff", Slot.WEAPON, CharacterClass.WIZARD),
	ROBE("Robe", Slot.ARMOUR, CharacterClass.WIZARD),
	TRINKET("Trinket", Slot.TRINKET, null);

	/** Where a piece of gear is worn. Weapons give ATK, armour DEF and trinkets MAG. */
	public enum Slot
	{
		WEAPON("ATK"),
		ARMOUR("DEF"),
		TRINKET("MAG");

		private final String statName;

		Slot(String statName)
		{
			this.statName = statName;
		}

		public String getStatName()
		{
			return statName;
		}
	}

	private final String displayName;
	private final Slot slot;
	/** The only class that can use it, or null for Generic gear. */
	private final CharacterClass requiredClass;

	GearType(String displayName, Slot slot, CharacterClass requiredClass)
	{
		this.displayName = displayName;
		this.slot = slot;
		this.requiredClass = requiredClass;
	}

	public String getDisplayName()
	{
		return displayName;
	}

	public Slot getSlot()
	{
		return slot;
	}

	/** The only class that can use it, or null for Generic gear. */
	public CharacterClass getRequiredClass()
	{
		return requiredClass;
	}

	public boolean canBeUsedBy(CharacterClass characterClass)
	{
		return requiredClass == null || requiredClass == characterClass;
	}
}
