package l2s.gameserver.taskmanager;

import l2s.commons.threading.SteppingRunnableQueueManager;
import l2s.commons.util.Rnd;
import l2s.gameserver.Config;
import l2s.gameserver.ThreadPoolManager;

public class EffectTaskManager extends SteppingRunnableQueueManager
{
	private static final long TICK = 250L;
	private static int _randomizer;
	private static final EffectTaskManager[] _instances;

	public static final EffectTaskManager getInstance()
	{
		return _instances[_randomizer++ & _instances.length - 1];
	}

	private EffectTaskManager()
	{
		super(250L);
		ThreadPoolManager.getInstance().scheduleAtFixedRate(this, Rnd.get(250L), 250L);
		ThreadPoolManager.getInstance().scheduleAtFixedRate(() -> purge(), 30000L + 1000L * _randomizer++, 30000L);
	}

	public CharSequence getStats(int num)
	{
		return _instances[num].getStats();
	}

	static
	{
		_instances = new EffectTaskManager[Config.EFFECT_TASK_MANAGER_COUNT];
		for(int i = 0; i < _instances.length; ++i)
			_instances[i] = new EffectTaskManager();
	}
}
