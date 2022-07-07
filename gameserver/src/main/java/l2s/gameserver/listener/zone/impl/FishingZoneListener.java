
package l2s.gameserver.listener.zone.impl;

import l2s.commons.lang.reference.HardReference;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.listener.zone.OnZoneEnterLeaveListener;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Zone;
import l2s.gameserver.model.Zone.ZoneType;
import l2s.gameserver.model.actor.instances.player.Fishing;
import l2s.gameserver.network.l2.s2c.ExAutoFishAvailable;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

public class FishingZoneListener implements OnZoneEnterLeaveListener
{
	public static final OnZoneEnterLeaveListener STATIC = new FishingZoneListener();

	private final Map<Integer, ScheduledFuture<?>> _notifyTasks = new HashMap<>();

	@Override
	public void onZoneEnter(Zone zone, Creature creature)
	{
		if(!creature.isPlayer())
			return;

		if(_notifyTasks.containsKey(creature.getObjectId()))
			return;

		_notifyTasks.put(creature.getObjectId(), ThreadPoolManager.getInstance().scheduleAtFixedRate(new NotifyPacketTask(zone, creature.getPlayer()), 0, 5000));
	}

	@Override
	public void onZoneLeave(Zone zone, Creature creature)
	{
		if(!creature.isPlayer())
			return;

		creature.sendPacket(ExAutoFishAvailable.REMOVE);

		stopAndRemoveNotifyTask(creature.getObjectId());
	}

	private void stopAndRemoveNotifyTask(int objectId)
	{
		ScheduledFuture<?> notifyTask = _notifyTasks.remove(objectId);

		if(notifyTask != null)
			notifyTask.cancel(false);
	}

	private class NotifyPacketTask implements Runnable
	{
		private final Zone _zone;
		private final int _objectId;
		private final HardReference<Player> _playerRef;

		public NotifyPacketTask(Zone zone, Player player)
		{
			_zone = zone;
			_objectId = player.getObjectId();
			_playerRef = player.getRef();
		}

		@Override
		public void run()
		{
			Player player = _playerRef.get();
			if(player == null)
			{
				stopAndRemoveNotifyTask(_objectId);
				return;
			}

			if(!player.isInZone(ZoneType.FISHING))
			{
				player.sendPacket(ExAutoFishAvailable.REMOVE);
				stopAndRemoveNotifyTask(player.getObjectId());
				return;
			}

			if(Fishing.findHookLocation(player) != null)
			{
				if(player.isFishing())
					player.sendPacket(ExAutoFishAvailable.FISHING);
				else
					player.sendPacket(ExAutoFishAvailable.SHOW);
			}
		}
	}

}
