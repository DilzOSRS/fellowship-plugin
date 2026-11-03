package com.dilzosrs.fellowship;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import net.runelite.api.Skill;
import net.runelite.api.gameval.ItemID;

/**
 * The classes a Fellowship character can be. Every character starts as an Adventurer.
 * <p>
 * The enum names (ADVENTURER, KNIGHT, ...) are saved per account, so never rename them
 * without a migration. The display names are safe to change.
 */
public enum CharacterClass
{
	ADVENTURER("Adventurer", ItemID.FEDORA),
	KNIGHT("Knight", ItemID.RUNE_FULL_HELM, Skill.ATTACK, Skill.STRENGTH),
	ARCHER("Archer", ItemID.COIF, Skill.RANGED),
	WIZARD("Wizard", ItemID.BLUEWIZHAT, Skill.MAGIC),
	// The Skiller starts like the Adventurer (no bonus, advances at level 10) but advances into skilling paths.
	// The paths' bonus skills are a first guess, to tune after playtesting.
	SKILLER("Skiller", ItemID.KNIFE),
	HARVESTER("Harvester", ItemID.RUNE_AXE,
		Skill.WOODCUTTING, Skill.MINING, Skill.FISHING, Skill.FARMING, Skill.HUNTER),
	ARTISAN("Artisan", ItemID.HAMMER,
		Skill.SMITHING, Skill.CRAFTING, Skill.FLETCHING, Skill.COOKING, Skill.HERBLORE, Skill.CONSTRUCTION, Skill.FIREMAKING),
	EXPLORER("Explorer", ItemID.LEATHER_BOOTS,
		Skill.AGILITY, Skill.THIEVING, Skill.RUNECRAFT, Skill.PRAYER, Skill.SLAYER),
	// A rare character that joins from Thieving and Agility (see RareUnlock), not hired. Its tree
	// has two class advancements: Thief -> Bandit -> Warlord, or Thief -> Assassin -> Night Blade.
	// Every step is at level 10 for now; later the Thief should need a higher level (about 20) and
	// the second step higher still, so it's a real grind. Perks and gear borrowing come later.
	THIEF("Thief", ItemID.ROGUESDEN_HELM, Skill.THIEVING, Skill.AGILITY),
	BANDIT("Bandit", ItemID.HIGHWAYMAN_MASK, Skill.THIEVING, Skill.AGILITY, Skill.ATTACK, Skill.STRENGTH),
	ASSASSIN("Assassin", ItemID.BARROWS_KARIL_HEAD, Skill.THIEVING, Skill.AGILITY, Skill.RANGED),
	WARLORD("Warlord", ItemID.ELITE_BLACK_FULL_HELM,
		Skill.THIEVING, Skill.AGILITY, Skill.ATTACK, Skill.STRENGTH),
	NIGHT_BLADE("Night Blade", ItemID.SHAYZIEN_HELM_5,
		Skill.THIEVING, Skill.AGILITY, Skill.RANGED),
	// A rare character that joins from any XP gained in Morytania (see RareUnlock). 1x XP in every
	// skill for now. At level 10 it becomes a Blood Mage, Death Knight or Shadow Stalker (also 1x XP
	// for now). Their Barrows perks come later.
	VYRE("Vyre", ItemID.VYRELORD_TORSO),
	BLOOD_MAGE("Blood Mage", ItemID.ANCIENT_CEREMONIAL_MASK),
	DEATH_KNIGHT("Death Knight", ItemID.TORVA_HELM_SANGUINE),
	// Shown as "Shadow Stalker" since 2026-11-02; the saved name stays SHADOW_LORD.
	SHADOW_LORD("Shadow Stalker", ItemID.INQUISITORS_HELM),
	// A rare character that joins from Prayer. Its class advancement is choosing a god to worship,
	// which blesses every expedition the team goes on, so it mostly stays at base (see Blessing).
	// The gods' bonus skills are placeholders.
	ACOLYTE("Acolyte", ItemID.ICTHLARINS_HOOD, Skill.PRAYER),
	GUTHIXIAN("Guthixian Zealot", ItemID.CASTLEWARS_GUTHIX_HALO, Skill.PRAYER),
	SARADOMINIST("Saradominist Zealot", ItemID.CASTLEWARS_SARADOMIN_HALO, Skill.PRAYER),
	ZAMORAKIAN("Zamorakian Zealot", ItemID.CASTLEWARS_ZAMORAK_HALO, Skill.PRAYER),
	ZAROSIAN("Zarosian Zealot", ItemID.ZAROS_HALO, Skill.PRAYER),
	// A secret rare character that joins when the player opens a shop or the Grand Exchange.
	// 1x XP in every skill for now (may be slowed later so it's a real grind), and no
	// class advancement yet. It will run the Grand Exchange trade expedition.
	MERCHANT("Merchant", ItemID.ROYAL_CROWN),
	// A rare character that joins from Herblore XP or from casting Low or High Level Alchemy.
	// 2x Herblore and Magic XP, no class advancement yet. It runs Transmuting metals,
	// which turns a stored item into coins (see Expedition).
	ALCHEMIST("Alchemist", ItemID.MM_ALCHEMIST_HAT, Skill.HERBLORE, Skill.MAGIC),
	// A rare character that joins from Sailing XP. 2x Sailing XP. At level 10 it becomes
	// one of three combat classes: the Swashbuckler (melee), the Harpooner (ranged) or the
	// Navigator (magic). Every class in the tree gets 2x Sailing XP and 1x everything else. They all wear leather, plus swords, bows or
	// staffs (see GearType). Sailing weapons, sea expeditions and a dungeon that needs a Sailor to
	// sail the party there come later.
	SAILOR("Sailor", ItemID.PIRATE_BANDANA_BLUE, Skill.SAILING),
	SWASHBUCKLER("Swashbuckler", ItemID.TRAIL_PIRATE_HAT, Skill.SAILING),
	HARPOONER("Harpooner", ItemID.DRAGON_HARPOON, Skill.SAILING),
	NAVIGATOR("Navigator", ItemID.SAILORS_AMULET, Skill.SAILING);

	/** XP from a class's bonus skills is multiplied by this. */
	static final int BONUS_XP_MULTIPLIER = 2;

	/**
	 * The level a class advancement can be taken from. There are no level caps (since 2026-11-02):
	 * a follower can keep levelling and choose when to advance.
	 */
	static final int ADVANCEMENT_LEVEL = 10;

	// Flat stats every follower of the class has, on top of gear (docs/design/roadmap-items-bundles-stats.md,
	// "Draft: class base stats"). Negative stats are a drawback that pays for more elsewhere.
	static
	{
		KNIGHT.setBaseStats(1, 3, 0);
		ARCHER.setBaseStats(3, 1, 0);
		WIZARD.setBaseStats(0, 0, 4);
		BANDIT.setBaseStats(3, 0, 0);
		ASSASSIN.setBaseStats(3, 0, 0);
		// The t3s aren't designed yet, so they keep the stats of the class before them.
		WARLORD.setBaseStats(3, 0, 0);
		NIGHT_BLADE.setBaseStats(3, 0, 0);
		SAILOR.setBaseStats(1, 1, 0);
		SWASHBUCKLER.setBaseStats(4, 1, 0);
		HARPOONER.setBaseStats(5, 0, 0);
		NAVIGATOR.setBaseStats(1, 1, 3);
		VYRE.setBaseStats(1, -1, 0);
		DEATH_KNIGHT.setBaseStats(6, -2, 2);
		SHADOW_LORD.setBaseStats(8, -4, 2);
		BLOOD_MAGE.setBaseStats(4, -4, 6);
	}

	private final String displayName;
	private final int iconItemId;
	private final Set<Skill> bonusSkills;
	private int baseAtk;
	private int baseDef;
	private int baseMag;

	CharacterClass(String displayName, int iconItemId, Skill... bonusSkills)
	{
		this.displayName = displayName;
		this.iconItemId = iconItemId;
		this.bonusSkills = bonusSkills.length == 0
			? EnumSet.noneOf(Skill.class)
			: EnumSet.of(bonusSkills[0], bonusSkills);
	}

	public String getDisplayName()
	{
		return displayName;
	}

	/** The OSRS item whose icon stands for this class on its card, until there's custom art. */
	public int getIconItemId()
	{
		return iconItemId;
	}

	private void setBaseStats(int atk, int def, int mag)
	{
		baseAtk = atk;
		baseDef = def;
		baseMag = mag;
	}

	/** ATK every follower of this class has before gear. Can be negative. */
	public int getBaseAtk()
	{
		return baseAtk;
	}

	/** DEF every follower of this class has before gear. Can be negative. */
	public int getBaseDef()
	{
		return baseDef;
	}

	/** MAG every follower of this class has before gear. */
	public int getBaseMag()
	{
		return baseMag;
	}

	/** e.g. "+1 ATK, +3 DEF" or "+8 ATK, -4 DEF, +2 MAG", for the card. Empty if the class has none. */
	public String describeBaseStats()
	{
		List<String> parts = new ArrayList<>();
		addStat(parts, baseAtk, "ATK");
		addStat(parts, baseDef, "DEF");
		addStat(parts, baseMag, "MAG");
		return String.join(", ", parts);
	}

	private static void addStat(List<String> parts, int value, String name)
	{
		if (value != 0)
		{
			parts.add((value > 0 ? "+" : "") + value + " " + name);
		}
	}

	/** How much Fellowship XP this class gets per OSRS XP in the given skill. */
	public int getXpMultiplier(Skill skill)
	{
		return bonusSkills.contains(skill) ? BONUS_XP_MULTIPLIER : 1;
	}

	/** The classes this class can evolve into from {@link #ADVANCEMENT_LEVEL}. */
	public List<CharacterClass> getEvolutions()
	{
		if (this == ADVENTURER)
		{
			return List.of(KNIGHT, ARCHER, WIZARD);
		}
		if (this == SKILLER)
		{
			return List.of(HARVESTER, ARTISAN, EXPLORER);
		}
		if (this == THIEF)
		{
			return List.of(BANDIT, ASSASSIN);
		}
		if (this == BANDIT)
		{
			return List.of(WARLORD);
		}
		if (this == ASSASSIN)
		{
			return List.of(NIGHT_BLADE);
		}
		if (this == VYRE)
		{
			return List.of(BLOOD_MAGE, DEATH_KNIGHT, SHADOW_LORD);
		}
		if (this == ACOLYTE)
		{
			return List.of(GUTHIXIAN, SARADOMINIST, ZAMORAKIAN, ZAROSIAN);
		}
		if (this == SAILOR)
		{
			return List.of(SWASHBUCKLER, HARPOONER, NAVIGATOR);
		}
		return Collections.emptyList();
	}

	/**
	 * Whether this class is reached by a class advancement (e.g. Knight or Bandit), rather than
	 * being a class characters start as (e.g. Adventurer or Thief).
	 */
	public boolean isAdvanced()
	{
		for (CharacterClass other : values())
		{
			if (other.getEvolutions().contains(this))
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * The class this one's tree starts from, e.g. Thief for a Bandit or a Warlord, and Adventurer
	 * for a Knight. A class characters start as is its own base.
	 */
	public CharacterClass getBaseClass()
	{
		for (CharacterClass other : values())
		{
			if (other.getEvolutions().contains(this))
			{
				return other.getBaseClass();
			}
		}
		return this;
	}

	/** The god this class worships, whose blessing helps its parties, or null if none. */
	public Blessing getBlessing()
	{
		switch (this)
		{
			case GUTHIXIAN:
				return Blessing.GUTHIX;
			case SARADOMINIST:
				return Blessing.SARADOMIN;
			case ZAMORAKIAN:
				return Blessing.ZAMORAK;
			case ZAROSIAN:
				return Blessing.ZAROS;
			default:
				return null;
		}
	}
}
