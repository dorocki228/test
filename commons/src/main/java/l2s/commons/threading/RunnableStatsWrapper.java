package l2s.commons.threading;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RunnableStatsWrapper implements Runnable
{
	private static final Logger LOGGER = LogManager.getLogger(RunnableStatsWrapper.class);

	private final Runnable _runnable;

	RunnableStatsWrapper(Runnable runnable)
	{
		_runnable = runnable;
	}

	public static Runnable wrap(Runnable runnable)
	{
		return new RunnableStatsWrapper(runnable);
	}

	@Override
	public void run()
	{
		execute(_runnable);
	}

	public static void execute(Runnable runnable)
	{
		long begin = System.nanoTime();
		try
		{
			runnable.run();
			RunnableStatsManager.getInstance().handleStats(runnable.getClass(), System.nanoTime() - begin);
		}
		catch(Exception e)
		{
			LOGGER.error("Exception in a Runnable execution:", e);
		}
	}
}
