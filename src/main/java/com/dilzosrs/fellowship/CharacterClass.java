package com.dilzosrs.fellowship;

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
	ADVENTURER("Adventurer", 10, ItemID.FEDORA),
	KNIGHT("Knight", LevelCurve.MAX_LEVEL, ItemID.RUNE_FULL_HELM, Skill.ATTACK, Skill.STRENGTH),
	ARCHER("Archer", LevelCurve.MAX_LEVEL, ItemID.COIF, Skill.RANGED),
	WIZARD("Wizard", LevelCurve.MAX_LEVEL, ItemID.BLUEWIZHAT, Skill.MAGIC),
	// The Skiller starts like the Adventurer (no bonus, cap 10) but advances into skilling paths.
	// The paths' bonus skills are a first guess, to tune after playtesting.
	SKILLER("Skiller", 10, ItemID.KNIFE),
	HARVESTER("Harvester", LevelCurve.MAX_LEVEL, ItemID.RUNE_AXE,
		Skill.WOODCUTTING, Skill.MINING, Skill.FISHING, Skill.FARMING, Skill.HUNTER),
	ARTISAN("Artisan", LevelCurve.MAX_LEVEL, ItemID.HAMMER,
		Skill.SMITHING, Skill.CRAFTING, Skill.FLETCHING, Skill.COOKING, Skill.HERBLORE, Skill.CONSTRUCTION, Skill.FIREMAKING),
	EXPLORER("Explorer", LevelCurve.MAX_LEVEL, ItemID.LEATHER_BOOTS,
		Skill.AGILITY, Skill.THIEVING, Skill.RUNECRAFT, Skill.PRAYER, Skill.SLAYER),
	// A rare character that joins from Thieving and Agility (see RareUnlock), not hired. Its tree
	// has two class advancements: Thief -> Bandit -> Warlord, or Thief -> Assassin -> Night Blade.
	// Every step is at level 10 for now; later the Thief should cap higher (about 20) and the
	// second step higher still, so it's a real grind. Perks and gear borrowing come later.
	THIEF("Thief", 10, ItemID.ROGUESDEN_HELM, Skill.THIEVING, Skill.AGILITY),
	BANDIT("Bandit", 10, ItemID.HIGHWAYMAN_MASK, Skill.THIEVING, Skill.AGILITY, Skill.ATTACK, Skill.STRENGTH),
	ASSASSIN("Assassin", 10, ItemID.BARROWS_KARIL_HEAD, Skill.THIEVING, Skill.AGILITY, Skill.RANGED),
	WARLORD("Warlord", LevelCurve.MAX_LEVEL, ItemID.ELITE_BLACK_FULL_HELM,
		Skill.THIEVING, Skill.AGILITY, Skill.ATTACK, Skill.STRENGTH),
	NIGHT_BLADE("Night Blade", LevelCurve.MAX_LEVEL, ItemID.SHAYZIEN_HELM_5,
		Skill.THIEVING, Skill.AGILITY, Skill.RANGED),
	// A rare character that joins from any XP gained in Morytania (see RareUnlock). 1x XP in every
	// skill for now. Its class advancements (Blood Mage, Death Knight, Shadow Lord) come later.
	VYRE("Vyre", 10, ItemID.VYRELORD_TORSO),
	// A rare character that joins from Prayer. Its class advancement is choosing a god to worship,
	// which blesses every expedition the team goes on, so it mostly stays at base (see Blessing).
	// The gods' bonus skills are placeholders.
	ACOLYTE("Acolyte", 10, ItemID.ICTHLARINS_HOOD, Skill.PRAYER),
	GUTHIXIAN("Guthixian Zealot", LevelCurve.MAX_LEVEL, ItemID.CASTLEWARS_GUTHIX_HALO, Skill.PRAYER),
	SARADOMINIST("Saradominist Zealot", LevelCurve.MAX_LEVEL, ItemID.CASTLEWARS_SARADOMIN_HALO, Skill.PRAYER),
	ZAMORAKIAN("Zamorakian Zealot", LevelCurve.MAX_LEVEL, ItemID.CASTLEWARS_ZAMORAK_HALO, Skill.PRAYER),
	ZAROSIAN("Zarosian Zealot", LevelCurve.MAX_LEVEL, ItemID.ZAROS_HALO, Skill.PRAYER);

	/** XP from a class's bonus skills is multiplied by this. */
	static final int BONUS_XP_MULTIPLIER = 2;

	private final String displayName;
	private final int levelCap;
	private final int iconItemId;
	private final Set<Skill> bonusSkills;

	CharacterClass(String displayName, int levelCap, int iconItemId, Skill... bonusSkills)
	{
		this.displayName = displayName;
		this.levelCap = levelCap;
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

	/** A character of this class can't go past this level. XP earned at the cap is wasted. */
	public int getLevelCap()
	{
		return levelCap;
	}

	/** How much Fellowship XP this class gets per OSRS XP in the given skill. */
	public int getXpMultiplier(Skill skill)
	{
		return bonusSkills.contains(skill) ? BONUS_XP_MULTIPLIER : 1;
	}

	/** The classes this class can evolve into once it reaches its level cap. */
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
		if (this == ACOLYTE)
		{
			return List.of(GUTHIXIAN, SARADOMINIST, ZAMORAKIAN, ZAROSIAN);
		}
		return Collections.emptyList();
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
