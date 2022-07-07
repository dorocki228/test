package l2s.gameserver.model.base;

import l2s.gameserver.data.xml.holder.ClassDataHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.templates.player.ClassData;

import java.util.Optional;

public enum ClassId
{
	HUMAN_FIGHTER(ClassType.FIGHTER, Race.HUMAN, null, ClassLevel.NONE, null, new int[0]),
	WARRIOR(ClassType.FIGHTER, Race.HUMAN, HUMAN_FIGHTER, ClassLevel.FIRST, null, new int[] { 1145 }),
	GLADIATOR(ClassType.FIGHTER, Race.HUMAN, WARRIOR, ClassLevel.SECOND, ClassType2.WARRIOR, new int[] { 2627, 2734, 2762 }),
	WARLORD(ClassType.FIGHTER, Race.HUMAN, WARRIOR, ClassLevel.SECOND, ClassType2.WARRIOR, new int[] { 2627, 2734, 3276 }),
	KNIGHT(ClassType.FIGHTER, Race.HUMAN, HUMAN_FIGHTER, ClassLevel.FIRST, null, new int[] { 1161 }),
	PALADIN(ClassType.FIGHTER, Race.HUMAN, KNIGHT, ClassLevel.SECOND, ClassType2.KNIGHT, new int[] { 2633, 2734, 2820 }),
	DARK_AVENGER(ClassType.FIGHTER, Race.HUMAN, KNIGHT, ClassLevel.SECOND, ClassType2.KNIGHT, new int[] { 2633, 2734, 3307 }),
	ROGUE(ClassType.FIGHTER, Race.HUMAN, HUMAN_FIGHTER, ClassLevel.FIRST, null, new int[] { 1190 }),
	TREASURE_HUNTER(ClassType.FIGHTER, Race.HUMAN, ROGUE, ClassLevel.SECOND, ClassType2.ROGUE, new int[] { 2673, 2734, 2809 }),
	HAWKEYE(ClassType.FIGHTER, Race.HUMAN, ROGUE, ClassLevel.SECOND, ClassType2.ARCHER, new int[] { 2673, 2734, 3293 }),
	HUMAN_MAGE(ClassType.MYSTIC, Race.HUMAN, null, ClassLevel.NONE, null, new int[0]),
	WIZARD(ClassType.MYSTIC, Race.HUMAN, HUMAN_MAGE, ClassLevel.FIRST, null, new int[] { 1292 }),
	SORCERER(ClassType.MYSTIC, Race.HUMAN, WIZARD, ClassLevel.SECOND, ClassType2.WIZARD, new int[] { 2674, 2734, 2840 }),
	NECROMANCER(ClassType.MYSTIC, Race.HUMAN, WIZARD, ClassLevel.SECOND, ClassType2.WIZARD, new int[] { 2674, 2734, 3307 }),
	WARLOCK(ClassType.MYSTIC, Race.HUMAN, WIZARD, ClassLevel.SECOND, ClassType2.SUMMONER, new int[] { 2674, 2734, 3336 }),
	CLERIC(ClassType.MYSTIC, Race.HUMAN, HUMAN_MAGE, ClassLevel.FIRST, null, new int[] { 1201 }),
	BISHOP(ClassType.MYSTIC, Race.HUMAN, CLERIC, ClassLevel.SECOND, ClassType2.HEALER, new int[] { 2721, 2734, 2820 }),
	PROPHET(ClassType.MYSTIC, Race.HUMAN, CLERIC, ClassLevel.SECOND, ClassType2.ENCHANTER, new int[] { 2721, 2734, 2821 }),
	ELVEN_FIGHTER(ClassType.FIGHTER, Race.ELF, null, ClassLevel.NONE, null, new int[0]),
	ELVEN_KNIGHT(ClassType.FIGHTER, Race.ELF, ELVEN_FIGHTER, ClassLevel.FIRST, null, new int[] { 1204 }),
	TEMPLE_KNIGHT(ClassType.FIGHTER, Race.ELF, ELVEN_KNIGHT, ClassLevel.SECOND, ClassType2.KNIGHT, new int[] { 2633, 3140, 2820 }),
	SWORDSINGER(ClassType.FIGHTER, Race.ELF, ELVEN_KNIGHT, ClassLevel.SECOND, ClassType2.ENCHANTER, new int[] { 2627, 3140, 2762 }),
	ELVEN_SCOUT(ClassType.FIGHTER, Race.ELF, ELVEN_FIGHTER, ClassLevel.FIRST, null, new int[] { 1217 }),
	PLAIN_WALKER(ClassType.FIGHTER, Race.ELF, ELVEN_SCOUT, ClassLevel.SECOND, ClassType2.ROGUE, new int[] { 2673, 3140, 2809 }),
	SILVER_RANGER(ClassType.FIGHTER, Race.ELF, ELVEN_SCOUT, ClassLevel.SECOND, ClassType2.ARCHER, new int[] { 2673, 3140, 3293 }),
	ELVEN_MAGE(ClassType.MYSTIC, Race.ELF, null, ClassLevel.NONE, null, new int[0]),
	ELVEN_WIZARD(ClassType.MYSTIC, Race.ELF, ELVEN_MAGE, ClassLevel.FIRST, null, new int[] { 1230 }),
	SPELLSINGER(ClassType.MYSTIC, Race.ELF, ELVEN_WIZARD, ClassLevel.SECOND, ClassType2.WIZARD, new int[] { 2674, 3140, 2840 }),
	ELEMENTAL_SUMMONER(ClassType.MYSTIC, Race.ELF, ELVEN_WIZARD, ClassLevel.SECOND, ClassType2.SUMMONER, new int[] { 2674, 3140, 3336 }),
	ORACLE(ClassType.MYSTIC, Race.ELF, ELVEN_MAGE, ClassLevel.FIRST, null, new int[] { 1235 }),
	ELDER(ClassType.MYSTIC, Race.ELF, ORACLE, ClassLevel.SECOND, ClassType2.HEALER, new int[] { 2721, 3140, 2820 }),
	DARK_FIGHTER(ClassType.FIGHTER, Race.DARKELF, null, ClassLevel.NONE, null, new int[0]),
	PALUS_KNIGHT(ClassType.FIGHTER, Race.DARKELF, DARK_FIGHTER, ClassLevel.FIRST, null, new int[] { 1244 }),
	SHILLEN_KNIGHT(ClassType.FIGHTER, Race.DARKELF, PALUS_KNIGHT, ClassLevel.SECOND, ClassType2.KNIGHT, new int[] { 2633, 3172, 3307 }),
	BLADEDANCER(ClassType.FIGHTER, Race.DARKELF, PALUS_KNIGHT, ClassLevel.SECOND, ClassType2.ENCHANTER, new int[] { 2627, 3172, 2762 }),
	ASSASIN(ClassType.FIGHTER, Race.DARKELF, DARK_FIGHTER, ClassLevel.FIRST, null, new int[] { 1252 }),
	ABYSS_WALKER(ClassType.FIGHTER, Race.DARKELF, ASSASIN, ClassLevel.SECOND, ClassType2.ROGUE, new int[] { 2673, 3172, 2809 }),
	PHANTOM_RANGER(ClassType.FIGHTER, Race.DARKELF, ASSASIN, ClassLevel.SECOND, ClassType2.ARCHER, new int[] { 2673, 3172, 3293 }),
	DARK_MAGE(ClassType.MYSTIC, Race.DARKELF, null, ClassLevel.NONE, null, new int[0]),
	DARK_WIZARD(ClassType.MYSTIC, Race.DARKELF, DARK_MAGE, ClassLevel.FIRST, null, new int[] { 1261 }),
	SPELLHOWLER(ClassType.MYSTIC, Race.DARKELF, DARK_WIZARD, ClassLevel.SECOND, ClassType2.WIZARD, new int[] { 2674, 3172, 2840 }),
	PHANTOM_SUMMONER(ClassType.MYSTIC, Race.DARKELF, DARK_WIZARD, ClassLevel.SECOND, ClassType2.SUMMONER, new int[] { 2674, 3172, 3336 }),
	SHILLEN_ORACLE(ClassType.MYSTIC, Race.DARKELF, DARK_MAGE, ClassLevel.FIRST, null, new int[] { 1270 }),
	SHILLEN_ELDER(ClassType.MYSTIC, Race.DARKELF, SHILLEN_ORACLE, ClassLevel.SECOND, ClassType2.HEALER, new int[] { 2721, 3172, 2821 }),
	ORC_FIGHTER(ClassType.FIGHTER, Race.ORC, null, ClassLevel.NONE, null, new int[0]),
	ORC_RAIDER(ClassType.FIGHTER, Race.ORC, ORC_FIGHTER, ClassLevel.FIRST, null, new int[] { 1592 }),
	DESTROYER(ClassType.FIGHTER, Race.ORC, ORC_RAIDER, ClassLevel.SECOND, ClassType2.WARRIOR, new int[] { 2627, 3203, 3276 }),
	ORC_MONK(ClassType.FIGHTER, Race.ORC, ORC_FIGHTER, ClassLevel.FIRST, null, new int[] { 1615 }),
	TYRANT(ClassType.FIGHTER, Race.ORC, ORC_MONK, ClassLevel.SECOND, ClassType2.WARRIOR, new int[] { 2627, 3203, 2762 }),
	ORC_MAGE(ClassType.MYSTIC, Race.ORC, null, ClassLevel.NONE, null, new int[0]),
	ORC_SHAMAN(ClassType.MYSTIC, Race.ORC, ORC_MAGE, ClassLevel.FIRST, null, new int[] { 1631 }),
	OVERLORD(ClassType.MYSTIC, Race.ORC, ORC_SHAMAN, ClassLevel.SECOND, ClassType2.ENCHANTER, new int[] { 2721, 3203, 3390 }),
	WARCRYER(ClassType.MYSTIC, Race.ORC, ORC_SHAMAN, ClassLevel.SECOND, ClassType2.ENCHANTER, new int[] { 2721, 3203, 2879 }),
	DWARVEN_FIGHTER(ClassType.FIGHTER, Race.DWARF, null, ClassLevel.NONE, null, new int[0]),
	SCAVENGER(ClassType.FIGHTER, Race.DWARF, DWARVEN_FIGHTER, ClassLevel.FIRST, null, new int[] { 1642 }),
	BOUNTY_HUNTER(ClassType.FIGHTER, Race.DWARF, SCAVENGER, ClassLevel.SECOND, ClassType2.ROGUE, new int[] { 3119, 3238, 2809 }),
	ARTISAN(ClassType.FIGHTER, Race.DWARF, DWARVEN_FIGHTER, ClassLevel.FIRST, null, new int[] { 1635 }),
	WARSMITH(ClassType.FIGHTER, Race.DWARF, ARTISAN, ClassLevel.SECOND, ClassType2.WARRIOR, new int[] { 3119, 3238, 2867 }),
	DUMMY_ENTRY_58,
	DUMMY_ENTRY_59,
	DUMMY_ENTRY_60,
	DUMMY_ENTRY_61,
	DUMMY_ENTRY_62,
	DUMMY_ENTRY_63,
	DUMMY_ENTRY_64,
	DUMMY_ENTRY_65,
	DUMMY_ENTRY_66,
	DUMMY_ENTRY_67,
	DUMMY_ENTRY_68,
	DUMMY_ENTRY_69,
	DUMMY_ENTRY_70,
	DUMMY_ENTRY_71,
	DUMMY_ENTRY_72,
	DUMMY_ENTRY_73,
	DUMMY_ENTRY_74,
	DUMMY_ENTRY_75,
	DUMMY_ENTRY_76,
	DUMMY_ENTRY_77,
	DUMMY_ENTRY_78,
	DUMMY_ENTRY_79,
	DUMMY_ENTRY_80,
	DUMMY_ENTRY_81,
	DUMMY_ENTRY_82,
	DUMMY_ENTRY_83,
	DUMMY_ENTRY_84,
	DUMMY_ENTRY_85,
	DUMMY_ENTRY_86,
	DUMMY_ENTRY_87,
	DUELIST(ClassType.FIGHTER, Race.HUMAN, GLADIATOR, ClassLevel.THIRD, ClassType2.WARRIOR),
	DREADNOUGHT(ClassType.FIGHTER, Race.HUMAN, WARLORD, ClassLevel.THIRD, ClassType2.WARRIOR),
	PHOENIX_KNIGHT(ClassType.FIGHTER, Race.HUMAN, PALADIN, ClassLevel.THIRD, ClassType2.KNIGHT),
	HELL_KNIGHT(ClassType.FIGHTER, Race.HUMAN, DARK_AVENGER, ClassLevel.THIRD, ClassType2.KNIGHT),
	SAGITTARIUS(ClassType.FIGHTER, Race.HUMAN, HAWKEYE, ClassLevel.THIRD, ClassType2.ARCHER),
	ADVENTURER(ClassType.FIGHTER, Race.HUMAN, TREASURE_HUNTER, ClassLevel.THIRD, ClassType2.ROGUE),
	ARCHMAGE(ClassType.MYSTIC, Race.HUMAN, SORCERER, ClassLevel.THIRD, ClassType2.WIZARD),
	SOULTAKER(ClassType.MYSTIC, Race.HUMAN, NECROMANCER, ClassLevel.THIRD, ClassType2.WIZARD),
	ARCANA_LORD(ClassType.MYSTIC, Race.HUMAN, WARLOCK, ClassLevel.THIRD, ClassType2.SUMMONER),
	CARDINAL(ClassType.MYSTIC, Race.HUMAN, BISHOP, ClassLevel.THIRD, ClassType2.HEALER),
	HIEROPHANT(ClassType.MYSTIC, Race.HUMAN, PROPHET, ClassLevel.THIRD, ClassType2.ENCHANTER),
	EVAS_TEMPLAR(ClassType.FIGHTER, Race.ELF, TEMPLE_KNIGHT, ClassLevel.THIRD, ClassType2.KNIGHT),
	SWORD_MUSE(ClassType.FIGHTER, Race.ELF, SWORDSINGER, ClassLevel.THIRD, ClassType2.ENCHANTER), // 100
	WIND_RIDER(ClassType.FIGHTER, Race.ELF, PLAIN_WALKER, ClassLevel.THIRD, ClassType2.ROGUE),
	MOONLIGHT_SENTINEL(ClassType.FIGHTER, Race.ELF, SILVER_RANGER, ClassLevel.THIRD, ClassType2.ARCHER),
	MYSTIC_MUSE(ClassType.MYSTIC, Race.ELF, SPELLSINGER, ClassLevel.THIRD, ClassType2.WIZARD),
	ELEMENTAL_MASTER(ClassType.MYSTIC, Race.ELF, ELEMENTAL_SUMMONER, ClassLevel.THIRD, ClassType2.SUMMONER),
	EVAS_SAINT(ClassType.MYSTIC, Race.ELF, ELDER, ClassLevel.THIRD, ClassType2.HEALER),
	SHILLIEN_TEMPLAR(ClassType.FIGHTER, Race.DARKELF, SHILLEN_KNIGHT, ClassLevel.THIRD, ClassType2.KNIGHT),
	SPECTRAL_DANCER(ClassType.FIGHTER, Race.DARKELF, BLADEDANCER, ClassLevel.THIRD, ClassType2.ENCHANTER),
	GHOST_HUNTER(ClassType.FIGHTER, Race.DARKELF, ABYSS_WALKER, ClassLevel.THIRD, ClassType2.ROGUE),
	GHOST_SENTINEL(ClassType.FIGHTER, Race.DARKELF, PHANTOM_RANGER, ClassLevel.THIRD, ClassType2.ARCHER),
	STORM_SCREAMER(ClassType.MYSTIC, Race.DARKELF, SPELLHOWLER, ClassLevel.THIRD, ClassType2.WIZARD),
	SPECTRAL_MASTER(ClassType.MYSTIC, Race.DARKELF, PHANTOM_SUMMONER, ClassLevel.THIRD, ClassType2.SUMMONER),
	SHILLIEN_SAINT(ClassType.MYSTIC, Race.DARKELF, SHILLEN_ELDER, ClassLevel.THIRD, ClassType2.HEALER),
	TITAN(ClassType.FIGHTER, Race.ORC, DESTROYER, ClassLevel.THIRD, ClassType2.WARRIOR),
	GRAND_KHAVATARI(ClassType.FIGHTER, Race.ORC, TYRANT, ClassLevel.THIRD, ClassType2.WARRIOR),
	DOMINATOR(ClassType.MYSTIC, Race.ORC, OVERLORD, ClassLevel.THIRD, ClassType2.ENCHANTER), // 115
	DOOMCRYER(ClassType.MYSTIC, Race.ORC, WARCRYER, ClassLevel.THIRD, ClassType2.ENCHANTER),
	FORTUNE_SEEKER(ClassType.FIGHTER, Race.DWARF, BOUNTY_HUNTER, ClassLevel.THIRD, ClassType2.ROGUE),
	MAESTRO(ClassType.FIGHTER, Race.DWARF, WARSMITH, ClassLevel.THIRD, ClassType2.WARRIOR); // 118

	public static final ClassId[] VALUES = values();
	private final Race _race;
	private final ClassId _parent;
	private final ClassLevel _level;
	private final ClassType _type;
	private final ClassType2 _type2;
	private final boolean _isDummy;
	private final int[] _changeClassItemIds;

	public static Optional<ClassId> valueOf(int id)
	{
		if(id < 0 || id >= VALUES.length)
			return Optional.empty();
		ClassId result = VALUES[id];
		if(result != null && !result.isDummy())
			return Optional.of(result);
		return Optional.empty();
	}

	ClassId()
	{
		this(null, null, null, null, null, true, new int[0]);
	}

	ClassId(ClassType classType, Race race, ClassId parent, ClassLevel level, ClassType2 type2)
	{
		this(classType, race, parent, level, type2, false, new int[0]);
	}

	ClassId(ClassType classType, Race race, ClassId parent, ClassLevel level, ClassType2 type2, int[] changeClassItemIds)
	{
		this(classType, race, parent, level, type2, false, changeClassItemIds);
	}

	ClassId(ClassType classType, Race race, ClassId parent, ClassLevel level, ClassType2 type2, boolean isDummy, int[] changeClassItemIds)
	{
		_type = classType;
		_race = race;
		_parent = parent;
		_level = level;
		_type2 = type2;
		_isDummy = isDummy;
		_changeClassItemIds = changeClassItemIds;
	}

	public final int getId()
	{
		return ordinal();
	}

	public final Race getRace()
	{
		return _race;
	}

	public final boolean isOfRace(Race race)
	{
		return _race == race;
	}

	public final ClassLevel getClassLevel()
	{
		return _level;
	}

	public final boolean isOfLevel(ClassLevel level)
	{
		return _level == level;
	}

	public final ClassType getType()
	{
		return _type;
	}

	public final boolean isOfType(ClassType type)
	{
		return _type == type;
	}

	public ClassType2 getType2()
	{
		return _type2;
	}

	public final boolean isOfType2(ClassType2 type)
	{
		return _type2 == type;
	}

	public final boolean isMage()
	{
		return _type.isMagician();
	}

	public final boolean isDummy()
	{
		return _isDummy;
	}

	public boolean childOf(ClassId cid)
	{
		return _parent != null && (_parent == cid || _parent.childOf(cid));
	}

	public final boolean equalsOrChildOf(ClassId cid)
	{
		return this == cid || childOf(cid);
	}

	public final ClassId getParent()
	{
		return _parent;
	}

	public ClassData getClassData()
	{
		return ClassDataHolder.getInstance().getClassData(getId());
	}

	public double getBaseCp(int level)
	{
		return getClassData().getHpMpCpData(level).getCP();
	}

	public double getBaseHp(int level)
	{
		return getClassData().getHpMpCpData(level).getHP();
	}

	public double getBaseMp(int level)
	{
		return getClassData().getHpMpCpData(level).getMP();
	}

	public final String getName(Player player)
	{
		return new CustomMessage("l2s.gameserver.model.base.name." + getId()).toString(player);
	}

	public int getClassMinLevel(boolean forNextClass)
	{
		ClassLevel classLevel = getClassLevel();
		if(forNextClass)
		{
			if(classLevel == ClassLevel.THIRD)
				return -1;
			classLevel = ClassLevel.VALUES[classLevel.ordinal() + 1];
		}
		switch(classLevel)
		{
			case FIRST:
				return 20;
			case SECOND:
				return 40;
			case THIRD:
				return 76;
			default:
				return 1;

		}
	}

	public boolean isLast()
	{
		return isOfLevel(ClassLevel.THIRD);
	}

	public int[] getChangeClassItemIds()
	{
		return _changeClassItemIds;
	}

	public boolean contains(ClassId other) {
		ClassId temp = this;
		while(temp != null)
		{
			if (temp == other) {
				return true;
			}

			temp = temp.getParent();
		}

		return false;
	}
}
