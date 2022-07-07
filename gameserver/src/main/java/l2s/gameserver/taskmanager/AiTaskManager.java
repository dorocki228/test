package l2s.gameserver.taskmanager;

import l2s.commons.threading.SteppingRunnableQueueManager;
import l2s.commons.util.Rnd;
import l2s.gameserver.Config;
import l2s.gameserver.ThreadPoolManager;

public class AiTaskManager extends SteppingRunnableQueueManager
{
	private static final long TICK = 250L;
	private static int _randomizer;
	private static final AiTaskManager[] _instances;

	public static final AiTaskManager getInstance()
	{
		return _instances[_randomizer++ & _instances.length - 1];
	}

	private AiTaskManager()
	{
		super(TICK);
		ThreadPoolManager.getInstance().scheduleAtFixedRate(this, Rnd.get(TICK), TICK);
		ThreadPoolManager.getInstance().scheduleAtFixedRate(this::purge, 60000L + 1000L * _randomizer++, 60000L);
	}

	public CharSequence getStats(int num)
	{
		return _instances[num].getStats();
	}

	static
	{
		_instances = new AiTaskManager[Config.AI_TASK_MANAGER_COUNT];
		for(int i = 0; i < _instances.length; ++i)
			_instances[i] = new AiTaskManager();
	}
}
