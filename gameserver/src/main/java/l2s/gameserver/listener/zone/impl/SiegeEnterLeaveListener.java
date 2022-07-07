package l2s.gameserver.listener.zone.impl;

import l2s.gameserver.listener.zone.OnZoneEnterLeaveListener;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Zone;

public class SiegeEnterLeaveListener implements OnZoneEnterLeaveListener
{
	public static final OnZoneEnterLeaveListener STATIC = new SiegeEnterLeaveListener();

	@Override
	public void onZoneEnter(Zone zone, Creature creature)
	{
		if(creature.isPlayable() && creature.getClan() != null)
		{
			if(creature.isPlayer())
				creature.getPlayer().broadcastUserInfo(true);
			else
				creature.getPlayable().broadcastRelation();
		}

	}

	@Override
	public void onZoneLeave(Zone zone, Creature creature)
	{
		if(creature.isPlayable() && creature.getClan() != null)
		{
			if(creature.isPlayer())
				creature.getPlayer().broadcastUserInfo(true);
			else
				creature.getPlayable().broadcastRelation();
		}
	}

}
