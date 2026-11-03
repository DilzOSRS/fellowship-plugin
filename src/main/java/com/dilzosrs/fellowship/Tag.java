package com.dilzosrs.fellowship;

/**
 * The visible tags on gear and followers (see docs/design/roadmap-items-bundles-stats.md). A follower
 * can use an item only if it has every tag the item has. Gear with no tags is Generic: anyone can use it.
 * <p>
 * Tags aren't saved (they come from the gear type and class), so renaming them is safe.
 */
public enum Tag
{
	SWORD("sword"),
	BOW("bow"),
	STAFF("staff"),
	PLATE("plate"),
	LEATHER("leather"),
	ROBE("robe"),
	NAUTICAL("nautical"),
	DARK("dark");

	private final String displayName;

	Tag(String displayName)
	{
		this.displayName = displayName;
	}

	/** e.g. "sword". Lower case, like the design docs. */
	public String getDisplayName()
	{
		return displayName;
	}
}
