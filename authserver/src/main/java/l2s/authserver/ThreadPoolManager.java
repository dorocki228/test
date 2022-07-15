package l2s.authserver;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import l2s.commons.threading.RejectedExecutionHandlerImpl;
import l2s.commons.threading.RunnableWrapper;

public class ThreadPoolManager
{
	private static final long MAX_DELAY = TimeUnit.NANOSECONDS.toMillis(Long.MAX_VALUE - System.nanoTime()) / 2;

	private static final ThreadPoolManager _instance = new ThreadPoolManager();

	public static final ThreadPoolManager getInstance()
	{
		return _instance;
	}

	private final ScheduledThreadPoolExecutor _scheduledExecutor;
	private final ThreadPoolExecutor _executor;

	private ThreadPoolManager()
	{
		_scheduledExecutor = new ScheduledThreadPoolExecutor(1);
		_scheduledExecutor.setRejectedExecutionHandler(new RejectedExecutionHandlerImpl());
		_scheduledExecutor.prestartAllCoreThreads();

		_executor = new ThreadPoolExecutor(1, 1, 5L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
		_executor.setRejectedExecutionHandler(new RejectedExecutionHandlerImpl());
		_executor.prestartAllCoreThreads();

		scheduleAtFixedRate(() ->
		{
			_scheduledExecutor.purge();
			_executor.purge();
		}, 600000L, 600000L);
	}

	private final long validate(long delay)
	{
		return Math.max(0, Math.min(MAX_DELAY, delay));
	}

	public void execute(Runnable r)
	{
		_executor.execute(new RunnableWrapper(r));
	}

	public ScheduledFuture<?> schedule(Runnable r, long delay)
	{
		return _scheduledExecutor.schedule(new RunnableWrapper(r), validate(delay), TimeUnit.MILLISECONDS);
	}

	public ScheduledFuture<?> scheduleAtFixedRate(Runnable r, long initial, long delay)
	{
		return _scheduledExecutor.scheduleAtFixedRate(new RunnableWrapper(r), validate(initial), validate(delay), TimeUnit.MILLISECONDS);
	}
}
