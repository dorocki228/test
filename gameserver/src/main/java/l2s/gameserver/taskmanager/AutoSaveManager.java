package l2s.gameserver.taskmanager;

import l2s.commons.threading.SteppingRunnableQueueManager;
import l2s.commons.util.Rnd;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.model.Player;

import java.util.concurrent.Future;

public class AutoSaveManager extends SteppingRunnableQueueManager
{
	private static final AutoSaveManager _instance;

	public static final AutoSaveManager getInstance()
	{
		return _instance;
	}

	private AutoSaveManager()
	{
		super(10000L);
		ThreadPoolManager.getInstance().scheduleAtFixedRate(this, 10000L, 10000L);
		ThreadPoolManager.getInstance().scheduleAtFixedRate(() -> purge(), 60000L, 60000L);
	}

	public Future<?> addAutoSaveTask(Player player)
	{
		long delay = Rnd.get(180, 360) * 1000L;
		return scheduleAtFixedRate(() -> {
			if(player.isOnline())
				player.store(true);
		}, delay, delay);
	}

	static
	{
		_instance = new AutoSaveManager();
	}
}
