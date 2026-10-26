package com.dilzosrs.fellowship;

import java.time.Instant;
import lombok.extern.slf4j.Slf4j;

/**
 * How one character is stored in the account's save. The team is saved as a JSON list of these.
 * <p>
 * The field names end up in players' saves, so never rename them without a migration.
 * Classes and expeditions are stored by their enum names.
 */
@Slf4j
public class SavedCharacter
{
	String characterClass;
	long xp;
	/** Null when the character is home. */
	String expedition;
	/** Epoch milliseconds. Null when the character is home. */
	Long expeditionStart;
	/** Epoch milliseconds when an injured character is healed. Null if it isn't injured. */
	Long restingUntil;
	/** What the character wears. Null for an empty slot. */
	SavedVault.SavedGear weapon;
	SavedVault.SavedGear armour;
	SavedVault.SavedGear trinket;

	public static SavedCharacter from(FellowshipCharacter character)
	{
		SavedCharacter saved = new SavedCharacter();
		saved.characterClass = character.getCharacterClass().name();
		saved.xp = character.getXp();
		if (character.isAway())
		{
			saved.expedition = character.getExpedition().name();
			saved.expeditionStart = character.getExpeditionStart().toEpochMilli();
		}
		if (character.getRestingUntil() != null)
		{
			saved.restingUntil = character.getRestingUntil().toEpochMilli();
		}
		saved.weapon = SavedVault.SavedGear.from(character.getEquipped(GearType.Slot.WEAPON));
		saved.armour = SavedVault.SavedGear.from(character.getEquipped(GearType.Slot.ARMOUR));
		saved.trinket = SavedVault.SavedGear.from(character.getEquipped(GearType.Slot.TRINKET));
		return saved;
	}

	public FellowshipCharacter toCharacter()
	{
		FellowshipCharacter character = new FellowshipCharacter(parseClass(characterClass), xp);
		// Gear first: it can't be put on once the character is away.
		for (SavedVault.SavedGear gear : new SavedVault.SavedGear[]{weapon, armour, trinket})
		{
			GearItem item = gear == null ? null : gear.toItem();
			if (item != null && character.canEquip(item))
			{
				character.equip(item);
			}
		}
		Expedition savedExpedition = parseExpedition(expedition);
		if (savedExpedition != null && expeditionStart != null)
		{
			// Timers run in real time, so an expedition can finish while logged out.
			character.startExpedition(savedExpedition, Instant.ofEpochMilli(expeditionStart));
		}
		if (restingUntil != null)
		{
			character.injure(Instant.ofEpochMilli(restingUntil));
		}
		return character;
	}

	static CharacterClass parseClass(String savedClass)
	{
		if (savedClass == null)
		{
			return CharacterClass.ADVENTURER;
		}
		try
		{
			return CharacterClass.valueOf(savedClass);
		}
		catch (IllegalArgumentException e)
		{
			log.warn("Unknown saved class {}, using Adventurer", savedClass);
			return CharacterClass.ADVENTURER;
		}
	}

	static Expedition parseExpedition(String savedExpedition)
	{
		if (savedExpedition == null)
		{
			return null;
		}
		try
		{
			return Expedition.valueOf(savedExpedition);
		}
		catch (IllegalArgumentException e)
		{
			log.warn("Unknown saved expedition {}, ignoring it", savedExpedition);
			return null;
		}
	}
}
