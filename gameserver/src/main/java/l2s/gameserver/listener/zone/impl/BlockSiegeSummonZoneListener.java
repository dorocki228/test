package l2s.gameserver.listener.zone.impl;

import l2s.gameserver.listener.zone.OnZoneEnterLeaveListener;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Zone;
import l2s.gameserver.model.entity.events.impl.SiegeEvent;

public class BlockSiegeSummonZoneListener implements OnZoneEnterLeaveListener
{
	public static final OnZoneEnterLeaveListener STATIC = new BlockSiegeSummonZoneListener();

	@Override
	public void onZoneEnter(Zone zone, Creature creature)
	{
		Player player = creature.getPlayer();

		if(player != null)
		{
			player.getEvents(SiegeEvent.class)
					.forEach(siegeEvent -> siegeEvent.despawnSiegeSummons(player));
		}
	}

	@Override
	public void onZoneLeave(Zone zone, Creature creature)
	{
	}
}
