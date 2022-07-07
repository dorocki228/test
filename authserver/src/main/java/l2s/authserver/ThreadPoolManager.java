package l2s.authserver;

import l2s.commons.threading.PriorityThreadFactory;

import java.util.concurrent.*;

public class ThreadPoolManager
{
	private static final long MAX_DELAY = TimeUnit.NANOSECONDS.toMillis(Long.MAX_VALUE - System.nanoTime()) / 2L;

	private static final ThreadPoolManager INSTANCE = new ThreadPoolManager();

	private final ScheduledThreadPoolExecutor scheduledThreadPoolExecutor;
	private final ThreadPoolExecutor threadPoolExecutor;

	public static final ThreadPoolManager getInstance()
	{
		return INSTANCE;
	}

	private ThreadPoolManager()
	{
		scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(1,
				new PriorityThreadFactory("ScheduledThreadPool", 3));
		threadPoolExecutor = new ThreadPoolExecutor(1, 1, 5L, TimeUnit.SECONDS,
				new LinkedBlockingQueue<>(), new PriorityThreadFactory("ThreadPoolExecutor", 3));

		scheduleAtFixedRate(() ->
		{
			threadPoolExecutor.purge();
			scheduledThreadPoolExecutor.purge();
		}, 600000L, 600000L);
	}

	private final long validate(long delay)
	{
		return Math.max(0L, Math.min(MAX_DELAY, delay));
	}

	public void execute(Runnable r)
	{
		threadPoolExecutor.execute(r);
	}

	public ScheduledFuture<?> schedule(Runnable r, long delay)
	{
		return scheduledThreadPoolExecutor.schedule(r, validate(delay), TimeUnit.MILLISECONDS);
	}

	public ScheduledFuture<?> scheduleAtFixedRate(Runnable r, long initial, long delay)
	{
		return scheduledThreadPoolExecutor.scheduleAtFixedRate(r, validate(initial), validate(delay), TimeUnit.MILLISECONDS);
	}
}
