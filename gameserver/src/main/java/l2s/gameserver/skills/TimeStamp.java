package l2s.gameserver.skills;

import l2s.gameserver.model.Skill;

public class TimeStamp
{
	private final int _id;
	private final int _level;
	private final long _reuse;
	private final long _endTime;
	private final boolean _addedOnEquip;
	private boolean globalReuse;

	public TimeStamp(int id, long endTime, long reuse)
	{
		_id = id;
		_level = 0;
		_reuse = reuse;
		_endTime = endTime;
		_addedOnEquip = false;
	}

	public TimeStamp(Skill skill, long reuse, boolean addedOnEquip)
	{
		this(skill, System.currentTimeMillis() + reuse, reuse, addedOnEquip);
	}

	public TimeStamp(Skill skill, long reuse)
	{
		this(skill, System.currentTimeMillis() + reuse, reuse, false);
	}

	public TimeStamp(Skill skill, long endTime, long reuse)
	{
		this(skill, endTime, reuse, false);
	}

	public TimeStamp(Skill skill, long endTime, long reuse, boolean addedOnEquip)
	{
		_id = skill.getId();
		_level = skill.getLevel();
		_reuse = reuse;
		_endTime = endTime;
		_addedOnEquip = addedOnEquip;
	}

	public long getReuseBasic()
	{
		if(_reuse == 0L)
			return getReuseCurrent();
		return _reuse;
	}

	public long getReuseCurrent()
	{
		return Math.max(_endTime - System.currentTimeMillis(), 0L);
	}

	public long getEndTime()
	{
		return _endTime;
	}

	public boolean hasNotPassed()
	{
		return System.currentTimeMillis() < _endTime;
	}

	public int getId()
	{
		return _id;
	}

	public int getLevel()
	{
		return _level;
	}

	public boolean isAddedOnEquip()
	{
		return _addedOnEquip;
	}

	public boolean isGlobalReuse(){
		return globalReuse;
	}

	public void setGlobalReuse(boolean globalReuse) {
		this.globalReuse = globalReuse;
	}
}
