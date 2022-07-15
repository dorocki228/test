package l2s.gameserver.listener.zone.impl;

import l2s.gameserver.listener.zone.OnZoneEnterLeaveListener;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Zone;
import l2s.gameserver.model.entity.residence.Residence;

/**
 * @author VISTALL
 * @date 16:04/03.07.2011
 */
public class ResidenceEnterLeaveListenerImpl implements OnZoneEnterLeaveListener
{
	public static final OnZoneEnterLeaveListener STATIC = new ResidenceEnterLeaveListenerImpl();

	@Override
	public void onZoneEnter(Zone zone, Creature actor)
	{
		if(!actor.isPlayer())
			return;

		Player player = (Player) actor;
		Residence residence = (Residence) zone.getParams().get("residence");
		if(residence == null)
			return;

		if(!residence.isOwner(player.getClanId()))
			return;

		actor.getStat().recalculateStats(false);
	}

	@Override
	public void onZoneLeave(Zone zone, Creature actor)
	{
		if(!actor.isPlayer())
			return;

		Residence residence = (Residence) zone.getParams().get("residence");
		if(residence == null)
			return;

		actor.getStat().recalculateStats(false);
	}
}
