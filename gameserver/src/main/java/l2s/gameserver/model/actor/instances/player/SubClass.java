package l2s.gameserver.model.actor.instances.player;

import l2s.gameserver.Config;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.base.ClassId;
import l2s.gameserver.model.base.Experience;
import l2s.gameserver.model.base.SubClassType;

public class SubClass
{
	private final Player _owner;
	private int _classId;
	private int _index;
	private boolean _active;
	private SubClassType _type;
	private int _level;
	private long _exp;
	private long _sp;
	private int _maxLvl;
	private long _minExp;
	private long _maxExp;
	private double _hp;
	private double _mp;
	private double _cp;

	public SubClass(Player owner)
	{
		_classId = 0;
		_index = 1;
		_active = false;
		_type = SubClassType.BASE_CLASS;
		_level = 1;
		_exp = 0L;
		_sp = 0L;
		_maxLvl = Experience.getMaxLevel();
		_minExp = 0L;
		_maxExp = Experience.LEVEL[_maxLvl + 1] - 1L;
		_hp = 1.0;
		_mp = 1.0;
		_cp = 1.0;
		_owner = owner;
	}

	public int getClassId()
	{
		return _classId;
	}

	public long getExp()
	{
		return _exp;
	}

	public long getMaxExp()
	{
		return _maxExp;
	}

	public void addExp(long val, boolean delevel)
	{
		setExp(_exp + val, delevel);
	}

	public long getSp()
	{
		return _sp;
	}

	public void addSp(long val)
	{
		setSp(_sp + val);
	}

	public int getLevel()
	{
		return _level;
	}

	public void setClassId(int id)
	{
		if(_classId == id)
			return;
		_classId = id;
	}

	public void setExp(long val, boolean delevel)
	{
		if(delevel)
			_exp = Math.min(Math.max(_minExp, val), _maxExp);
		else
			_exp = Math.min(Math.max(Experience.LEVEL[_level], val), _maxExp);
		_level = Experience.getLevel(_exp);
	}

	public void setSp(long spValue)
	{
		_sp = Math.min(Math.max(0L, spValue), Config.SP_LIMIT);
	}

	public void setHp(double hpValue)
	{
		_hp = Math.max(0.0, hpValue);
	}

	public double getHp()
	{
		return _hp;
	}

	public void setMp(double mpValue)
	{
		_mp = Math.max(0.0, mpValue);
	}

	public double getMp()
	{
		return _mp;
	}

	public void setCp(double cpValue)
	{
		_cp = Math.max(0.0, cpValue);
	}

	public double getCp()
	{
		return _cp;
	}

	public void setActive(boolean active)
	{
		_active = active;
	}

	public boolean isActive()
	{
		return _active;
	}

	public void setType(SubClassType type)
	{
		if(_type == type)
			return;
		_type = type;
		if(_type == SubClassType.SUBCLASS)
		{
			_maxLvl = Experience.getMaxSubLevel();
			_minExp = Experience.LEVEL[Config.SUB_START_LEVEL];
			_maxExp = Experience.LEVEL[_maxLvl + 1] - 1L;
			_level = Math.min(Math.max(Config.SUB_START_LEVEL, _level), _maxLvl);
		}
		else
		{
			_maxLvl = Experience.getMaxLevel();
			_minExp = 0L;
			_maxExp = Experience.LEVEL[_maxLvl + 1] - 1L;
			_level = Math.min(Math.max(1, _level), _maxLvl);
		}
		_exp = Math.min(Math.max(Experience.LEVEL[_level], _exp), _maxExp);
	}

	public SubClassType getType()
	{
		return _type;
	}

	public boolean isBase()
	{
		return _type == SubClassType.BASE_CLASS;
	}

	@Override
	public String toString()
	{
		return ClassId.VALUES[_classId] + " " + _level;
	}

	public int getMaxLevel()
	{
		return _maxLvl;
	}

	public void setIndex(int i)
	{
		_index = i;
	}

	public int getIndex()
	{
		return _index;
	}
}
