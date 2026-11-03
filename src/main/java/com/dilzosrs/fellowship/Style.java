package com.dilzosrs.fellowship;

/**
 * The hidden fighting style of a weapon or follower. Players never see it on its own; it's what
 * combo items (e.g. a nautical-melee Cutlass) and limits (e.g. "dark melee weapons only") check.
 */
public enum Style
{
	MELEE("melee"),
	RANGED("ranged"),
	MAGIC("magic");

	private final String displayName;

	Style(String displayName)
	{
		this.displayName = displayName;
	}

	public String getDisplayName()
	{
		return displayName;
	}
}
