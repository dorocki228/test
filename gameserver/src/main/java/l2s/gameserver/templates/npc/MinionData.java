package l2s.gameserver.templates.npc;

public class MinionData
{
	private final int _minionId;
	private final int _minionAmount;

	public MinionData(int minionId, int minionAmount)
	{
		_minionId = minionId;
		_minionAmount = minionAmount;
	}

	public int getMinionId()
	{
		return _minionId;
	}

	public int getAmount()
	{
		return _minionAmount;
	}

	@Override
	public boolean equals(Object o)
	{
		return o == this || o != null && o.getClass() == getClass() && ((MinionData) o).getMinionId() == getMinionId();
	}

	@Override
	public int hashCode()
	{
		return 7 * getMinionId() + 17840;
	}
}
