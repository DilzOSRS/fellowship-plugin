package com.dilzosrs.fellowship;

import java.util.Objects;

/**
 * One piece of gear, e.g. a "Rare Bow". Its type decides the slot, stat and tags;
 * its rarity decides how much of that stat it gives.
 */
public final class GearItem
{
	private final GearType type;
	private final Rarity rarity;

	public GearItem(GearType type, Rarity rarity)
	{
		this.type = type;
		this.rarity = rarity;
	}

	public GearType getType()
	{
		return type;
	}

	public Rarity getRarity()
	{
		return rarity;
	}

	/** e.g. "Rare Bow". */
	public String getName()
	{
		return rarity.getDisplayName() + " " + type.getDisplayName();
	}

	/** The OSRS item whose icon stands for this item, until there's custom art. */
	public int getIconItemId()
	{
		return type.getIconItemId(rarity);
	}

	public int getPower()
	{
		return rarity.getPower();
	}


	/**
	 * The hover text: name, stat, tags and who can use it, then why the follower can't (whyNot, or
	 * null) and what a click does (or null).
	 */
	public String getTooltip(String whyNot, String clickText)
	{
		StringBuilder text = new StringBuilder("<html><body style='width: 170px'><b>").append(getName())
			.append("</b>, ").append(getStatText())
			.append("<br>Tags: ").append(type.getTagText())
			.append("<br>Used by: ").append(ClassGear.whoCanUse(type));
		if (whyNot != null)
		{
			text.append("<br><font color='#ff6060'>").append(whyNot).append("</font>");
		}
		if (clickText != null)
		{
			text.append("<br>").append(clickText);
		}
		return text.append("</body></html>").toString();
	}

	/** e.g. "ATK 6". Short enough for chat, where the list of classes would be too long. */
	public String getStatText()
	{
		return type.getSlot().getStatName() + " " + getPower();
	}

	@Override
	public boolean equals(Object o)
	{
		if (!(o instanceof GearItem))
		{
			return false;
		}
		GearItem other = (GearItem) o;
		return type == other.type && rarity == other.rarity;
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(type, rarity);
	}
}
