package l2s.gameserver.listener.zone.impl;

import gnu.trove.map.hash.TIntObjectHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import l2s.gameserver.listener.actor.npc.OnDecayListener;
import l2s.gameserver.listener.zone.OnZoneEnterLeaveListener;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Zone;
import l2s.gameserver.model.instances.NpcInstance;

public class AlligatorSwampsListener implements OnZoneEnterLeaveListener
{

	private final int NOS = 20793;
	private final int MAX_NOS_SPAW = 20;
	private final int MIN_SPAWN = 2;
	private final int MAX_SPAWN = 5;

	private final TIntObjectHashMap<ScheduledFuture<?>> _spawnTask = new TIntObjectHashMap<>();

	private long _lastSpawn;

	private final long SPAWN_DELAY = 6000;
	private final long SPAWN_DELAY2 = 30000;

	private final List<NpcInstance> _spawned = new ArrayList<>();
	private final onDecayImpl DECAY_LISTENER = new onDecayImpl();

	@Override
	public void onZoneEnter(Zone zone, Creature creature)
	{
//		if(creature.isPlayer())
//		{
//			if(_spawnTask.get(creature.getObjectId()) != null)
//				return;
//
//			_spawnTask.put(creature.getObjectId(), ThreadPoolManager.getInstance().scheduleAtFixedDelay(new SwampTick(creature), SPAWN_DELAY, 1000));
//		}
	}

	@Override
	public void onZoneLeave(Zone zone, Creature creature)
	{
//		if(creature.isPlayer())
//		{
//			ScheduledFuture<?> task = _spawnTask.remove(creature.getObjectId());
//			if(task != null)
//			{
//				task.cancel(false);
//				task = null;
//			}
//		}
	}

	private class SwampTick implements Runnable
	{
		private final Creature _cha;

		public SwampTick(Creature cha)
		{
			_cha = cha;
		}

		@Override
		public void run()
		{
//			if(_spawned.size() < MAX_NOS_SPAW && System.currentTimeMillis() > _lastSpawn + SPAWN_DELAY + SPAWN_DELAY2)
//			{
//				int count = Math.min(Rnd.get(MIN_SPAWN, MAX_SPAWN), MAX_NOS_SPAW - _spawned.size());
//
//				for(int i = 0; i < count; i++)
//				{
//					NpcInstance npc = NpcUtils.spawnSingle(NOS, Location.findAroundPosition(_cha, 150).setH(Rnd.get(Short.MAX_VALUE)));
//					npc.addListener(DECAY_LISTENER);
//					_spawned.add(npc);
//				}
//
//				_lastSpawn = System.currentTimeMillis();
//			}
		}
	}

	private class onDecayImpl implements OnDecayListener
	{
		@Override
		public void onDecay(NpcInstance npc)
		{
			_spawned.remove(npc);
		}

	}
}
