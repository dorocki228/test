package l2s.gameserver.listener.zone.impl;

import l2s.gameserver.Config;
import l2s.gameserver.listener.zone.OnZoneEnterLeaveListener;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Zone;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.utils.Location;

public class EpicZoneListener implements OnZoneEnterLeaveListener
{
	public static final OnZoneEnterLeaveListener STATIC;

	@Override
	public void onZoneEnter(Zone zone, Creature creature)
	{
		if(creature.isPlayable() && !creature.getPlayer().isGM())
			if(creature.getLevel() > zone.getParams().getInteger("levelLimit", Integer.MAX_VALUE))
			{
				if(creature.isPlayer())
					creature.getPlayer().sendMessage(new CustomMessage("scripts.zones.epic.banishMsg"));
				creature.teleToLocation(Location.parseLoc(zone.getParams().getString("tele")));
			}
			else if(!Config.ALT_USE_TRANSFORM_IN_EPIC_ZONE && creature.isPlayer())
			{
				Player player = creature.getPlayer();
				if(player.isTransformed())
					player.setTransform(null);
			}
	}

	@Override
	public void onZoneLeave(Zone zone, Creature creature)
	{
		if(creature.isNpc() && creature.getNpcId() == zone.getParams().getInteger("noOutForNpc", 0))
		{
			creature.getAI().returnHome(true);
		}

	}

	static
	{
		STATIC = new EpicZoneListener();
	}
}
