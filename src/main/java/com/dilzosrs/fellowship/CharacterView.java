package com.dilzosrs.fellowship;

import java.time.Instant;

/**
 * A snapshot of one character for the panel. Characters change on the client thread and the
 * panel draws on the Swing thread, so the panel gets copies instead of the live character.
 */
public class CharacterView
{
	final String name;
	final CharacterClass characterClass;
	final long xp;
	final boolean active;
	final boolean canEvolve;
	/** Null when the character is home. */
	final Expedition expedition;
	/** Null when the character is home. */
	final Instant expeditionEnd;
	/** When an injured character is healed; null if it isn't resting. */
	final Instant restingUntil;
	/** Equipped gear; null for an empty slot. */
	final GearItem weapon;
	final GearItem armour;
	final GearItem trinket;
	final int atk;
	final int def;
	final int mag;

	CharacterView(Team team, int index)
	{
		FellowshipCharacter character = team.get(index);
		this.name = team.getDisplayName(index);
		this.characterClass = character.getCharacterClass();
		this.xp = character.getXp();
		this.active = index == team.getActiveIndex();
		this.canEvolve = character.canEvolve();
		this.expedition = character.getExpedition();
		this.expeditionEnd = character.getExpeditionEnd();
		this.restingUntil = character.isResting(Instant.now()) ? character.getRestingUntil() : null;
		this.weapon = character.getEquipped(GearType.Slot.WEAPON);
		this.armour = character.getEquipped(GearType.Slot.ARMOUR);
		this.trinket = character.getEquipped(GearType.Slot.TRINKET);
		this.atk = character.getAtk();
		this.def = character.getDef();
		this.mag = character.getMag();
	}

	/** The gear in a slot, or null if it's empty. */
	GearItem getEquipped(GearType.Slot slot)
	{
		switch (slot)
		{
			case WEAPON:
				return weapon;
			case ARMOUR:
				return armour;
			default:
				return trinket;
		}
	}
}
