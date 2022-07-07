package l2s.gameserver.taskmanager;

import l2s.commons.threading.SteppingRunnableQueueManager;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.model.Creature;

import java.util.concurrent.Future;

public class DecayTaskManager extends SteppingRunnableQueueManager
{
	private static final DecayTaskManager _instance;

	public static final DecayTaskManager getInstance()
	{
		return _instance;
	}

	private DecayTaskManager()
	{
		super(500L);
		ThreadPoolManager.getInstance().scheduleAtFixedRate(this, 500L, 500L);
		ThreadPoolManager.getInstance().scheduleAtFixedRate(() -> purge(), 60000L, 60000L);
	}

	public Future<?> addDecayTask(Creature actor, long delay)
	{
		return schedule(() -> actor.doDecay(), delay);
	}

	static
	{
		_instance = new DecayTaskManager();
	}
}
