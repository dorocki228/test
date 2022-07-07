package l2s.gameserver.ai.residences.fortress;

import l2s.gameserver.model.instances.NpcInstance;

public class GuardFighter extends Guard
{
	public GuardFighter(NpcInstance actor)
	{
		super(actor);
	}

	@Override
	protected boolean createNewTask()
	{
		return defaultFightTask();
	}

	@Override
	public int getRatePHYS()
	{
		return 25;
	}

	@Override
	public int getRateDOT()
	{
		return 50;
	}

	@Override
	public int getRateDEBUFF()
	{
		return 50;
	}

	@Override
	public int getRateDAM()
	{
		return 75;
	}

	@Override
	public int getRateSTUN()
	{
		return 50;
	}

	@Override
	public int getRateBUFF()
	{
		return 10;
	}

	@Override
	public int getRateHEAL()
	{
		return 50;
	}
}