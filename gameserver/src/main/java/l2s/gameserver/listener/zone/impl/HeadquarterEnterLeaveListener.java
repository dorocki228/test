package l2s.gameserver.listener.zone.impl;

import l2s.gameserver.listener.zone.OnZoneEnterLeaveListener;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Zone;
import l2s.gameserver.stats.Stats;
import l2s.gameserver.stats.funcs.FuncMul;

public class HeadquarterEnterLeaveListener implements OnZoneEnterLeaveListener
{

	private final int _ownerId;

	public HeadquarterEnterLeaveListener(int ownerId)
	{
		_ownerId = ownerId;
	}

	@Override
	public void onZoneEnter(Zone zone, Creature creature)
	{
		if(!creature.isPlayer() || !creature.getPlayer().isInSameClan(_ownerId))
			return;

		creature.addStatFunc(new FuncMul(Stats.REGENERATE_HP_RATE, 48, this, 3.0));
		creature.addStatFunc(new FuncMul(Stats.REGENERATE_MP_RATE, 48, this, 0.4));
	}

	@Override
	public void onZoneLeave(Zone zone, Creature creature)
	{
		if(!creature.isPlayer())
			return;

		creature.removeStatsOwner(this);
	}

}
