package l2s.gameserver.listener.zone.impl;

import l2s.gameserver.listener.zone.OnZoneEnterLeaveListener;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Zone;

public class NoPartyZoneListener implements OnZoneEnterLeaveListener
{
	public static final OnZoneEnterLeaveListener STATIC;

	@Override
	public void onZoneEnter(Zone zone, Creature creature)
	{
		Player player = creature.getPlayer();

		if(player != null)
		{
			player.startPartyBlock();
			player.leaveParty();
		}
	}

	@Override
	public void onZoneLeave(Zone zone, Creature creature)
	{

		Player player = creature.getPlayer();

		if(player != null)
			player.stopPartyBlock();
	}

	static
	{
		STATIC = new NoPartyZoneListener();
	}
}
