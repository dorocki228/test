package l2s.gameserver.taskmanager;

import l2s.commons.threading.SteppingRunnableQueueManager;
import l2s.gameserver.ThreadPoolManager;

public class RegenTaskManager extends SteppingRunnableQueueManager
{
	private static final RegenTaskManager _instance;

	public static final RegenTaskManager getInstance()
	{
		return _instance;
	}

	private RegenTaskManager()
	{
		super(333L);
		ThreadPoolManager.getInstance().scheduleAtFixedRate(this, 333L, 333L);
		ThreadPoolManager.getInstance().scheduleAtFixedRate(() -> purge(), 10000L, 10000L);
	}

	static
	{
		_instance = new RegenTaskManager();
	}
}
