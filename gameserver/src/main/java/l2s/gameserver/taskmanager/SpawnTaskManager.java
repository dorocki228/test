package l2s.gameserver.taskmanager;

import l2s.commons.lang.reference.HardReference;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.model.Spawner;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.utils.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class SpawnTaskManager
{
	private static final Logger _log = LoggerFactory.getLogger(SpawnTaskManager.class);

	private SpawnTask[] _spawnTasks;
	private int _spawnTasksSize;
	private final Object spawnTasks_lock;
	private static SpawnTaskManager _instance;

	public SpawnTaskManager()
	{
		_spawnTasks = new SpawnTask[500];
		_spawnTasksSize = 0;
		spawnTasks_lock = new Object();
		ThreadPoolManager.getInstance().scheduleAtFixedRate(new SpawnScheduler(), 2000L, 2000L);
	}

	public static SpawnTaskManager getInstance()
	{
		if(_instance == null)
			_instance = new SpawnTaskManager();
		return _instance;
	}

	public void addSpawnTask(NpcInstance actor, long interval)
	{
		removeObject(actor);
		addObject(new SpawnTask(actor, System.currentTimeMillis() + interval));
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder("============= SpawnTask Manager Report ============\n\r");
		sb.append("Tasks count: ").append(_spawnTasksSize).append("\n\r");
		sb.append("Tasks dump:\n\r");
		long current = System.currentTimeMillis();
		for(SpawnTask container : _spawnTasks)
		{
			sb.append("Class/Name: ").append(container.getClass().getSimpleName()).append('/').append(container.getActor());
			sb.append(" spawn timer: ").append(Util.formatTime((int) (container.endtime - current))).append("\n\r");
		}
		return sb.toString();
	}

	private void addObject(SpawnTask decay)
	{
		synchronized (spawnTasks_lock)
		{
			if(_spawnTasksSize >= _spawnTasks.length)
			{
				SpawnTask[] temp = new SpawnTask[_spawnTasks.length * 2];
				System.arraycopy(_spawnTasks, 0, temp, 0, _spawnTasksSize);
				_spawnTasks = temp;
			}
			_spawnTasks[_spawnTasksSize] = decay;
			++_spawnTasksSize;
		}
	}

	public void removeObject(NpcInstance actor)
	{
		synchronized (spawnTasks_lock)
		{
			if(_spawnTasksSize > 1)
			{
				int k = -1;
				for(int i = 0; i < _spawnTasksSize; ++i)
					if(_spawnTasks[i].getActor() == actor)
						k = i;
				if(k > -1)
				{
					_spawnTasks[k] = _spawnTasks[_spawnTasksSize - 1];
					_spawnTasks[_spawnTasksSize - 1] = null;
					--_spawnTasksSize;
				}
			}
			else if(_spawnTasksSize == 1 && _spawnTasks[0].getActor() == actor)
			{
				_spawnTasks[0] = null;
				_spawnTasksSize = 0;
			}
		}
	}

	public class SpawnScheduler implements Runnable
	{
		@Override
		public void run()
		{
			if(_spawnTasksSize > 0)
				try
				{
					List<NpcInstance> works = new ArrayList<>();
					synchronized (spawnTasks_lock)
					{
						long current = System.currentTimeMillis();
						int size = _spawnTasksSize;
						for(int i = size - 1; i >= 0; --i)
							try
							{
								SpawnTask container = _spawnTasks[i];
								if(container != null && container.endtime > 0L && current > container.endtime)
								{
									NpcInstance actor = container.getActor();
									if(actor != null && actor.getSpawn() != null)
										works.add(actor);
									container.endtime = -1L;
								}
								if(container == null || container.getActor() == null || container.endtime < 0L)
								{
									if(i == _spawnTasksSize - 1)
										_spawnTasks[i] = null;
									else
									{
										_spawnTasks[i] = _spawnTasks[_spawnTasksSize - 1];
										_spawnTasks[_spawnTasksSize - 1] = null;
									}
									if(_spawnTasksSize > 0)
										_spawnTasksSize--;
								}
							}
							catch(Exception e)
							{
								_log.error("", e);
							}
					}
					for(NpcInstance work : works)
					{
						Spawner spawn = work.getSpawn();
						if(spawn == null)
							continue;
						spawn.decreaseScheduledCount();
						if(!spawn.isDoRespawn())
							continue;
						spawn.respawnNpc(work);
					}
				}
				catch(Exception e2)
				{
					_log.error("", e2);
				}
		}
	}

	private class SpawnTask
	{
		private final HardReference<NpcInstance> _npcRef;
		public long endtime;

		SpawnTask(NpcInstance cha, long delay)
		{
			_npcRef = cha.getRef();
			endtime = delay;
		}

		public NpcInstance getActor()
		{
			return _npcRef.get();
		}
	}
}
