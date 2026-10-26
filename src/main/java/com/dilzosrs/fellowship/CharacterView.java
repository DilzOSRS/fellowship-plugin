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
	}
}
