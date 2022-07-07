package l2s.gameserver;

import l2s.commons.threading.PriorityThreadFactory;
import l2s.commons.threading.RunnableStatsWrapper;

import java.util.concurrent.*;

public class ThreadPoolManager
{
	private static final long MAX_DELAY = TimeUnit.NANOSECONDS.toMillis(Long.MAX_VALUE - System.nanoTime()) / 2L;

	private static final ThreadPoolManager INSTANCE = new ThreadPoolManager();

	private final ScheduledThreadPoolExecutor PhantomOtherScheduledExecutor;
	private final ScheduledThreadPoolExecutor PhantomSpawnscheduledExecutor;
	private final ScheduledThreadPoolExecutor PhantomAiScheduledExecutor;
	private final ScheduledThreadPoolExecutor PhantomScheduledExecutor;
	
	private final ScheduledThreadPoolExecutor scheduledThreadPoolExecutor;
	private final ThreadPoolExecutor threadPoolExecutor;

	private boolean shutdown;

	public static ThreadPoolManager getInstance()
	{
		return INSTANCE;
	}

	private ThreadPoolManager()
	{
		PhantomAiScheduledExecutor = new ScheduledThreadPoolExecutor(Config.SCHEDULED_THREAD_POOL_SIZE, new PriorityThreadFactory("PhantomAiScheduled", Thread.NORM_PRIORITY), new ThreadPoolExecutor.CallerRunsPolicy());
		PhantomOtherScheduledExecutor = new ScheduledThreadPoolExecutor(Config.SCHEDULED_THREAD_POOL_SIZE, new PriorityThreadFactory("PhantomOtherScheduled", Thread.NORM_PRIORITY), new ThreadPoolExecutor.CallerRunsPolicy());
		PhantomSpawnscheduledExecutor = new ScheduledThreadPoolExecutor(Config.SCHEDULED_THREAD_POOL_SIZE, new PriorityThreadFactory("PhantomSpawnScheduled", Thread.NORM_PRIORITY), new ThreadPoolExecutor.CallerRunsPolicy());
		PhantomScheduledExecutor = new ScheduledThreadPoolExecutor(Config.SCHEDULED_THREAD_POOL_SIZE, new PriorityThreadFactory("PhantomScheduled", Thread.NORM_PRIORITY), new ThreadPoolExecutor.CallerRunsPolicy());
		
		scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(Config.SCHEDULED_THREAD_POOL_SIZE,new PriorityThreadFactory("ScheduledThreadPool", 5),new ThreadPoolExecutor.CallerRunsPolicy());
		threadPoolExecutor = new ThreadPoolExecutor(Config.EXECUTOR_THREAD_POOL_SIZE, Integer.MAX_VALUE, 5L, TimeUnit.SECONDS, new LinkedBlockingQueue<>(),new PriorityThreadFactory("ThreadPoolExecutor", 5),new ThreadPoolExecutor.CallerRunsPolicy());

		scheduleAtFixedRate(() ->
        {
        	
  				PhantomAiScheduledExecutor.purge();
  				PhantomOtherScheduledExecutor.purge();
  				PhantomSpawnscheduledExecutor.purge();
  				PhantomScheduledExecutor.purge();
  				
			scheduledThreadPoolExecutor.purge();
			threadPoolExecutor.purge();
		}, 300000L, 300000L);
	}

	public ScheduledThreadPoolExecutor getScheduledThreadPoolExecutor()
	{
		return scheduledThreadPoolExecutor;
	}

	public ThreadPoolExecutor getThreadPoolExecutor()
	{
		return threadPoolExecutor;
	}

	private long validate(long delay)
	{
		long validatedDelay = Math.max(0L, Math.min(MAX_DELAY, delay));
		if(delay > validatedDelay)
			return -1L;
		long secondsToRestart = Shutdown.getInstance().getSeconds() * 1000L;
		if(secondsToRestart > 0L && validatedDelay > secondsToRestart)
			return -1L;
		return validatedDelay;
	}

	public boolean isShutdown()
	{
		return shutdown;
	}

	public Runnable wrap(Runnable r)
	{
		return RunnableStatsWrapper.wrap(r);
	}

	public ScheduledFuture<?> schedule(Runnable r, long delay)
	{
		return schedule(r, delay, TimeUnit.MILLISECONDS);
	}

	public ScheduledFuture<?> schedule(Runnable r, long delay, TimeUnit unit)
	{
		delay = validate(delay);
		if(delay == -1L)
			return null;
		return scheduledThreadPoolExecutor.schedule(wrap(r), delay, unit);
	}

	public ScheduledFuture<?> scheduleAtFixedRate(Runnable r, long initial, long delay)
	{
		return scheduleAtFixedRate(r, initial, delay, TimeUnit.MILLISECONDS);
	}

	public ScheduledFuture<?> scheduleAtFixedRate(Runnable r, long initial, long delay, TimeUnit unit)
	{
		initial = validate(initial);
		if(initial == -1L)
			return null;
		delay = validate(delay);
		if(delay == -1L)
			return scheduledThreadPoolExecutor.schedule(wrap(r), initial, unit);
		return scheduledThreadPoolExecutor.scheduleAtFixedRate(wrap(r), initial, delay, unit);
	}

	public ScheduledFuture<?> scheduleAtFixedDelay(Runnable r, long initial, long delay)
	{
		return scheduleAtFixedDelay(r, initial, delay, TimeUnit.MILLISECONDS);
	}

	public ScheduledFuture<?> scheduleAtFixedDelay(Runnable r, long initial, long delay, TimeUnit unit)
	{
		initial = validate(initial);
		if(initial == -1L)
			return null;
		delay = validate(delay);
		if(delay == -1L)
			return scheduledThreadPoolExecutor.schedule(wrap(r), initial, unit);
		return scheduledThreadPoolExecutor.scheduleWithFixedDelay(wrap(r), initial, delay, unit);
	}

	public void execute(Runnable r)
	{
		threadPoolExecutor.execute(wrap(r));
	}

	public void shutdown() throws InterruptedException
	{
		shutdown = true;
		try
		{
			PhantomAiScheduledExecutor.shutdown();
			PhantomOtherScheduledExecutor.shutdown();
			PhantomSpawnscheduledExecutor.shutdown();
			PhantomScheduledExecutor.shutdown();

			
			PhantomAiScheduledExecutor.awaitTermination(10, TimeUnit.SECONDS);
			PhantomOtherScheduledExecutor.awaitTermination(10, TimeUnit.SECONDS);
			PhantomSpawnscheduledExecutor.awaitTermination(10, TimeUnit.SECONDS);
			PhantomScheduledExecutor.awaitTermination(10, TimeUnit.SECONDS);
			
			scheduledThreadPoolExecutor.shutdown();
			scheduledThreadPoolExecutor.awaitTermination(10L, TimeUnit.SECONDS);
		}
		finally
		{
			threadPoolExecutor.shutdown();
			threadPoolExecutor.awaitTermination(1L, TimeUnit.MINUTES);
		}
	}

	public CharSequence getStats()
	{
		StringBuilder list = new StringBuilder();
		
		list.append("PhantomAiScheduledThreadPool\n");
		list.append("=================================================\n");
		list.append("\tgetActiveCount: ...... ").append(PhantomAiScheduledExecutor.getActiveCount()).append('\n');
		list.append("\tgetCorePoolSize: ..... ").append(PhantomAiScheduledExecutor.getCorePoolSize()).append('\n');
		list.append("\tgetPoolSize: ......... ").append(PhantomAiScheduledExecutor.getPoolSize()).append('\n');
		list.append("\tgetLargestPoolSize: .. ").append(PhantomAiScheduledExecutor.getLargestPoolSize()).append('\n');
		list.append("\tgetMaximumPoolSize: .. ").append(PhantomAiScheduledExecutor.getMaximumPoolSize()).append('\n');
		list.append("\tgetCompletedTaskCount: ").append(PhantomAiScheduledExecutor.getCompletedTaskCount()).append('\n');
		list.append("\tgetQueuedTaskCount: .. ").append(PhantomAiScheduledExecutor.getQueue().size()).append('\n');
		list.append("\tgetTaskCount: ........ ").append(PhantomAiScheduledExecutor.getTaskCount()).append('\n');
		
		list.append("PhantomSpawnScheduledThreadPool\n");
		list.append("=================================================\n");
		list.append("\tgetActiveCount: ...... ").append(PhantomSpawnscheduledExecutor.getActiveCount()).append('\n');
		list.append("\tgetCorePoolSize: ..... ").append(PhantomSpawnscheduledExecutor.getCorePoolSize()).append('\n');
		list.append("\tgetPoolSize: ......... ").append(PhantomSpawnscheduledExecutor.getPoolSize()).append('\n');
		list.append("\tgetLargestPoolSize: .. ").append(PhantomSpawnscheduledExecutor.getLargestPoolSize()).append('\n');
		list.append("\tgetMaximumPoolSize: .. ").append(PhantomSpawnscheduledExecutor.getMaximumPoolSize()).append('\n');
		list.append("\tgetCompletedTaskCount: ").append(PhantomSpawnscheduledExecutor.getCompletedTaskCount()).append('\n');
		list.append("\tgetQueuedTaskCount: .. ").append(PhantomSpawnscheduledExecutor.getQueue().size()).append('\n');
		list.append("\tgetTaskCount: ........ ").append(PhantomSpawnscheduledExecutor.getTaskCount()).append('\n');
		
		list.append("ScheduledThreadPool\n");
		list.append("=================================================\n");
		list.append("\tgetActiveCount: ...... ").append(scheduledThreadPoolExecutor.getActiveCount()).append("\n");
		list.append("\tgetCorePoolSize: ..... ").append(scheduledThreadPoolExecutor.getCorePoolSize()).append("\n");
		list.append("\tgetPoolSize: ......... ").append(scheduledThreadPoolExecutor.getPoolSize()).append("\n");
		list.append("\tgetLargestPoolSize: .. ").append(scheduledThreadPoolExecutor.getLargestPoolSize()).append("\n");
		list.append("\tgetMaximumPoolSize: .. ").append(scheduledThreadPoolExecutor.getMaximumPoolSize()).append("\n");
		list.append("\tgetCompletedTaskCount: ").append(scheduledThreadPoolExecutor.getCompletedTaskCount()).append("\n");
		list.append("\tgetQueuedTaskCount: .. ").append(scheduledThreadPoolExecutor.getQueue().size()).append("\n");
		list.append("\tgetTaskCount: ........ ").append(scheduledThreadPoolExecutor.getTaskCount()).append("\n");
		list.append("ThreadPoolExecutor\n");
		list.append("=================================================\n");
		list.append("\tgetActiveCount: ...... ").append(threadPoolExecutor.getActiveCount()).append("\n");
		list.append("\tgetCorePoolSize: ..... ").append(threadPoolExecutor.getCorePoolSize()).append("\n");
		list.append("\tgetPoolSize: ......... ").append(threadPoolExecutor.getPoolSize()).append("\n");
		list.append("\tgetLargestPoolSize: .. ").append(threadPoolExecutor.getLargestPoolSize()).append("\n");
		list.append("\tgetMaximumPoolSize: .. ").append(threadPoolExecutor.getMaximumPoolSize()).append("\n");
		list.append("\tgetCompletedTaskCount: ").append(threadPoolExecutor.getCompletedTaskCount()).append("\n");
		list.append("\tgetQueuedTaskCount: .. ").append(threadPoolExecutor.getQueue().size()).append("\n");
		list.append("\tgetTaskCount: ........ ").append(threadPoolExecutor.getTaskCount()).append("\n");

		return list;
	}

//TODO
	public ScheduledFuture <?> PhantomOtherSchedule(final Runnable r, final long delay, final TimeUnit timeUnit)
	{
		return PhantomOtherScheduledExecutor.schedule(wrap(r), delay, timeUnit);
	}
	
	public ScheduledFuture <?> PhantomOtherSchedeleAtFixedRate(final Runnable r, final long initial, final long delay)
	{
		return PhantomOtherScheduledExecutor.scheduleAtFixedRate(wrap(r), initial, delay, TimeUnit.MILLISECONDS);
	}
	
	public ScheduledFuture <?> PhantomOtherSchedule(final Runnable r, final long delay)
	{
		return PhantomOtherSchedule(r, delay, TimeUnit.MILLISECONDS);
	}

	public ScheduledFuture <?> PhantomSpawnSchedeleAtFixedRate(final Runnable r, final long initial, final long delay)
	{
		return PhantomSpawnscheduledExecutor.scheduleAtFixedRate(wrap(r), initial, delay, TimeUnit.SECONDS);
	}
	
	public ScheduledFuture <?> PhantomSchedule(final Runnable r, final long delay)
	{
		return PhantomSchedule(r, delay, TimeUnit.MILLISECONDS);
	}
	
	public ScheduledFuture <?> PhantomSchedule(final Runnable r, final long delay, final TimeUnit timeUnit)
	{
		return PhantomScheduledExecutor.schedule(wrap(r), delay, timeUnit);
	}

	public ScheduledFuture <?> PhantomScheduleAtFixedRate(final Runnable r, final long initial, final long delay)
	{
		return PhantomScheduledExecutor.scheduleAtFixedRate(wrap(r), initial, delay, TimeUnit.MILLISECONDS);
	}
	
	public ScheduledFuture <?> PhantomAiScheduleAtFixedRate(final Runnable r, final long initial, final long delay)
	{
		return PhantomAiScheduleAtFixedRate(r, initial, delay, TimeUnit.MILLISECONDS);
	}
	
	public ScheduledFuture <?> PhantomAiScheduleAtFixedRate(final Runnable r, final long initial, final long delay, final TimeUnit timeUnit)
	{
		return PhantomAiScheduledExecutor.scheduleAtFixedRate(wrap(r), initial, delay, timeUnit);
	}
	
	public ScheduledFuture <?> PhantomAiSchedule(final Runnable r, final long delay)
	{
		return PhantomAiScheduledExecutor.schedule(wrap(r), delay, TimeUnit.MILLISECONDS);
	}
}
