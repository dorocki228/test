package l2s.gameserver.templates.item;

import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import l2s.gameserver.handler.items.IItemHandler;
import l2s.gameserver.handler.items.ItemHandler;
import l2s.gameserver.stats.Stats;
import l2s.gameserver.stats.funcs.FuncTemplate;
import l2s.gameserver.templates.StatsSet;

public final class WeaponTemplate extends ItemTemplate
{
	private final int _soulShotCount;
	private final int _spiritShotCount;
	private final int _kamaelConvert;
	private final int _rndDam;
	private final int _atkReuse;
	private final int _mpConsume;
	private final int _atkRange;
	private final boolean _isMagicWeapon;
	private int _critical;
	private final int[] _reducedSoulshot;
	private final int[] _reducedSpiritshot;
	private final int[] _reducedMPConsume;
	private TIntSet _availableEnsouls = null;

	public WeaponTemplate(StatsSet set)
	{
		super(set);
		_type = set.getEnum("type", WeaponType.class);
		_soulShotCount = set.getInteger("soulshots", 0);
		_spiritShotCount = set.getInteger("spiritshots", 0);
		_kamaelConvert = set.getInteger("kamael_convert", 0);
		_rndDam = set.getInteger("rnd_dam", 0);
		_atkReuse = set.getInteger("atk_reuse", _type == WeaponType.BOW ? 1500 : 0);
		_atkRange = set.getInteger("atk_range", getDefaultAttackRange((WeaponType) _type));
		_mpConsume = set.getInteger("mp_consume", 0);
		_isMagicWeapon = set.getBool("is_magic_weapon", false);
		_reducedSoulshot = set.getIntegerArray("reduced_soulshot", new int[] { 0, _soulShotCount });
		_reducedSpiritshot = set.getIntegerArray("reduced_spiritshot", new int[] { 0, _spiritShotCount });
		_reducedMPConsume = set.getIntegerArray("reduced_mp_consume", new int[] { 0, _mpConsume });

		int[] availableEnsouls = set.getIntegerArray("available_ensouls", new int[0]);
		if(availableEnsouls.length > 0)
			_availableEnsouls = new TIntHashSet(availableEnsouls);

		if(_type == WeaponType.NONE)
		{
			_type1 = 1;
			_type2 = 1;
		}
		else
		{
			_type1 = 0;
			_type2 = 0;
		}
		if(_type == WeaponType.SWORD && !_isMagicWeapon)
			_exType = ExItemType.SWORD;
		else if(_type == WeaponType.SWORD && _isMagicWeapon)
			_exType = ExItemType.MAGIC_SWORD;
		else if(_type == WeaponType.DAGGER)
			_exType = ExItemType.DAGGER;
		else if(_type == WeaponType.BIGSWORD)
			_exType = ExItemType.BIG_SWORD;
		else if(_type == WeaponType.DUAL)
			_exType = ExItemType.DUAL_SWORD;
		else if(_type == WeaponType.DUALDAGGER)
			_exType = ExItemType.DUAL_DAGGER;
		else if(_type == WeaponType.BLUNT && !_isMagicWeapon)
			_exType = ExItemType.BLUNT_WEAPON;
		else if(_type == WeaponType.BLUNT && _isMagicWeapon)
			_exType = ExItemType.MAGIC_BLUNT_WEAPON;
		else if(_type == WeaponType.BIGBLUNT && !_isMagicWeapon)
			_exType = ExItemType.BIG_BLUNT_WEAPON;
		else if(_type == WeaponType.BIGBLUNT && _isMagicWeapon)
			_exType = ExItemType.BIG_MAGIC_BLUNT_WEAPON;
		else if(_type == WeaponType.DUALBLUNT)
			_exType = ExItemType.DUAL_BLUNT_WEAPON;
		else if(_type == WeaponType.BOW)
			_exType = ExItemType.BOW;
		else if(_type == WeaponType.DUALFIST)
			_exType = ExItemType.HAND_TO_HAND;
		else if(_type == WeaponType.POLE)
			_exType = ExItemType.POLE;
		else if(_type == WeaponType.ETC || _type == WeaponType.ROD)
			_exType = ExItemType.OTHER_WEAPON;
		else if(_bodyPart == 256 && _type == WeaponType.NONE)
			_exType = ExItemType.SHIELD;
		initEnchantFuncs();
	}

	@Override
	public boolean canBeEnsoul(int ensoulId)
	{
		if(!isWeapon())
			return false;

		if(_availableEnsouls == null ? getGrade().ordinal() < ItemGrade.D.ordinal() : !_availableEnsouls.contains(ensoulId))
			return false;

		return isEnsoulable();
	}

	@Override
	public IItemHandler getHandler()
	{
		return ItemHandler.EQUIPABLE_HANDLER;
	}

	@Override
	public WeaponType getItemType()
	{
		return (WeaponType) super.getItemType();
	}

	@Override
	public long getItemMask()
	{
		return getItemType().mask();
	}

	public int getSoulShotCount()
	{
		return _soulShotCount;
	}

	public int getSpiritShotCount()
	{
		return _spiritShotCount;
	}

	public int getCritical()
	{
		return _critical;
	}

	public int getRandomDamage()
	{
		return _rndDam;
	}

	public int getAttackReuseDelay()
	{
		return _atkReuse;
	}

	public int getMpConsume()
	{
		return _mpConsume;
	}

	public int getAttackRange()
	{
		return _atkRange;
	}

	public static int getDefaultAttackRange(WeaponType type)
	{
		switch(type)
		{
			case BOW:
			{
				return 500;
			}
			case POLE:
			{
				return 80;
			}
			default:
			{
				return 40;
			}
		}
	}

	@Override
	public void attachFunc(FuncTemplate f)
	{
		if(f._stat == Stats.BASE_P_CRITICAL_RATE && f._order == 8)
			_critical = (int) Math.round(f._value / 10.0);
		super.attachFunc(f);
	}

	public int getKamaelConvert()
	{
		return _kamaelConvert;
	}

	@Override
	public boolean isMagicWeapon()
	{
		return _isMagicWeapon;
	}

	public int[] getReducedSoulshot()
	{
		return _reducedSoulshot;
	}

	public int[] getReducedSpiritshot()
	{
		return _reducedSpiritshot;
	}

	public int[] getReducedMPConsume()
	{
		return _reducedMPConsume;
	}

	@Override
	public WeaponFightType getWeaponFightType()
	{
		if(_isMagicWeapon)
			return WeaponFightType.MAGE;
		return WeaponFightType.WARRIOR;
	}

	public enum WeaponType implements ItemType
	{
		NONE("Shield", Stats.FIST_WPN_VULNERABILITY),
		SWORD("Sword", Stats.SWORD_WPN_VULNERABILITY),
		BLUNT("Blunt", Stats.BLUNT_WPN_VULNERABILITY),
		DAGGER("Dagger", Stats.DAGGER_WPN_VULNERABILITY),
		BOW("Bow", Stats.BOW_WPN_VULNERABILITY),
		POLE("Pole", Stats.POLE_WPN_VULNERABILITY),
		ETC("Etc", Stats.FIST_WPN_VULNERABILITY),
		FIST("Fist", Stats.FIST_WPN_VULNERABILITY),
		DUAL("Dual Sword", Stats.DUAL_WPN_VULNERABILITY),
		DUALFIST("Dual Fist", Stats.FIST_WPN_VULNERABILITY),
		BIGSWORD("Big Sword", Stats.SWORD_WPN_VULNERABILITY),
		ROD("Rod", Stats.FIST_WPN_VULNERABILITY),
		BIGBLUNT("Big Blunt", Stats.BLUNT_WPN_VULNERABILITY),
		DUALDAGGER("Dual Dagger", Stats.DAGGER_WPN_VULNERABILITY),
		DUALBLUNT("Dual Blunt", Stats.BLUNT_WPN_VULNERABILITY),
		MAGIC("Magic", null);

		public static final WeaponType[] VALUES;
		private final long _mask;
		private final String _name;
		private final Stats _defence;

		WeaponType(String name, Stats defence)
		{
			_mask = 1L << ordinal() + 1000;
			_name = name;
			_defence = defence;
		}

		@Override
		public long mask()
		{
			return _mask;
		}

		@Override
		public IItemHandler getHandler()
		{
			return ItemHandler.EQUIPABLE_HANDLER;
		}

		public Stats getDefence()
		{
			return _defence;
		}

		@Override
		public ExItemType getExType()
		{
			return null;
		}

		@Override
		public String toString()
		{
			return _name;
		}

		static
		{
			VALUES = values();
		}
	}
}
