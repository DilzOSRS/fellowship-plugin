package com.dilzosrs.fellowship;

import java.util.Objects;

/**
 * One piece of gear, e.g. a Mithril sword (the Uncommon sword). Its type decides the slot and tags;
 * its rarity picks the named item of that type, with its own stats and level to wear.
 */
public final class GearItem
{
	private final GearType type;
	private final Rarity rarity;
	private final GearTier tier;

	/**
	 * @throws IllegalArgumentException if the type has no item at that rarity (see GearType.nearestTier)
	 */
	public GearItem(GearType type, Rarity rarity)
	{
		this.tier = type.getTier(rarity);
		if (tier == null)
		{
			throw new IllegalArgumentException(type + " has no " + rarity + " item");
		}
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

	/** e.g. "Mithril sword". */
	public String getName()
	{
		return tier.getName();
	}

	/** The OSRS item whose icon stands for this item, until there's custom art. */
	public int getIconItemId()
	{
		return tier.getIconItemId();
	}

	public int getAtk()
	{
		return tier.getAtk();
	}

	public int getDef()
	{
		return tier.getDef();
	}

	public int getMag()
	{
		return tier.getMag();
	}

	/** The level a follower needs to wear it. */
	public int getLevelRequirement()
	{
		return rarity.getLevelRequirement();
	}

	/**
	 * The hover text: name, stats, tags, level and who can use it, then why the follower can't
	 * (whyNot, or null) and what a click does (or null).
	 */
	public String getTooltip(String whyNot, String clickText)
	{
		StringBuilder text = new StringBuilder("<html><body style='width: 170px'><b>").append(getName())
			.append("</b> (").append(rarity.getDisplayName()).append(")<br>").append(getStatText())
			.append("<br>Tags: ").append(type.getTagText())
			.append("<br>Level ").append(getLevelRequirement()).append(" to wear")
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

	/** e.g. "5 ATK, 2 DEF". Short enough for chat, where the list of classes would be too long. */
	public String getStatText()
	{
		StringBuilder text = new StringBuilder();
		appendStat(text, getAtk(), "ATK");
		appendStat(text, getDef(), "DEF");
		appendStat(text, getMag(), "MAG");
		return text.toString();
	}

	private static void appendStat(StringBuilder text, int value, String name)
	{
		if (value != 0)
		{
			text.append(text.length() > 0 ? ", " : "").append(value).append(' ').append(name);
		}
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
