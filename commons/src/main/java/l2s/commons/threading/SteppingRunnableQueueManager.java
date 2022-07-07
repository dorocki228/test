package l2s.commons.threading;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Delayed;
import java.util.concurrent.RunnableScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;

public abstract class SteppingRunnableQueueManager implements Runnable
{
	private static final Logger LOGGER = LoggerFactory.getLogger(SteppingRunnableQueueManager.class);

	private final long tickPerStepInMillis;
	private final List<SteppingScheduledFuture<?>> queue;
	private final AtomicBoolean isRunning;

	public SteppingRunnableQueueManager(long tickPerStepInMillis)
	{
		queue = new CopyOnWriteArrayList<>();
		isRunning = new AtomicBoolean();
		this.tickPerStepInMillis = tickPerStepInMillis;
	}

	public SteppingScheduledFuture<?> schedule(Runnable r, long delay)
	{
		return schedule(r, delay, delay, false);
	}

	public SteppingScheduledFuture<?> scheduleAtFixedRate(Runnable r, long initial, long delay)
	{
		return schedule(r, initial, delay, true);
	}

	private SteppingScheduledFuture<?> schedule(Runnable r, long initial, long delay, boolean isPeriodic)
	{
		long initialStepping = getStepping(initial);
		long stepping = getStepping(delay);
		SteppingScheduledFuture<?> sr;
		queue.add(sr = new SteppingScheduledFuture<>(r, initialStepping, stepping, isPeriodic));
		return sr;
	}

	private long getStepping(long delay)
	{
		delay = Math.max(0L, delay);
		return delay % tickPerStepInMillis > tickPerStepInMillis / 2L ? delay / tickPerStepInMillis + 1L : delay < tickPerStepInMillis ? 1L : delay / tickPerStepInMillis;
	}

	@Override
	public void run()
	{
		if(!isRunning.compareAndSet(false, true))
		{
            LOGGER.warn("Slow running queue, managed by {}, queue size : {}!", this, queue.size());
			return;
		}

		try
		{
			if(queue.isEmpty())
				return;

			queue.stream()
					.filter(sr -> !sr.isDone())
					.forEach(SteppingScheduledFuture::run);
		}
		finally
		{
			isRunning.set(false);
		}
	}

	public void purge()
	{
		Collection<SteppingScheduledFuture<?>> purge =
				queue.stream()
						.filter(SteppingScheduledFuture::isDone)
						.collect(Collectors.toList());
		queue.removeAll(purge);
	}

	public CharSequence getStats()
	{
		StringBuilder list = new StringBuilder();
		Map<String, LongAdder> stats = new TreeMap<>();
		int total = 0;
		int done = 0;
		for(SteppingScheduledFuture<?> sr : queue)
			if(sr.isDone())
				++done;
			else
			{
				++total;

				String name = sr.r.getClass().getName();
				LongAdder count = stats.computeIfAbsent(name, key -> new LongAdder());
				count.increment();
			}
		stats.forEach((key, value) -> list.append("\t").append(key).append(" : ").append(value.longValue()).append("%n"));
		list.append("Scheduled: ....... ").append(total).append("%n");
		list.append("Done/Cancelled: .. ").append(done).append("%n");
		return list;
	}

	public class SteppingScheduledFuture<V> implements RunnableScheduledFuture<V>
	{
		private final Runnable r;
		private final long stepping;
		private final boolean isPeriodic;
		private long step;
		private boolean isCancelled;

		public SteppingScheduledFuture(Runnable r, long initial, long stepping, boolean isPeriodic)
		{
			this.r = r;
			step = initial;
			this.stepping = stepping;
			this.isPeriodic = isPeriodic;
			isCancelled = false;
		}

		@Override
		public void run()
		{
			long step = this.step - 1L;
			this.step = step;
			if(step == 0L)
				try
				{
					r.run();
				}
				catch(RuntimeException e)
				{
                    LOGGER.error("SteppingScheduledFuture.run():{}", e, e);
				}
				finally
				{
					if(isPeriodic)
						this.step = stepping;
				}
		}

		@Override
		public boolean isDone()
		{
			return isCancelled || !isPeriodic && step == 0L;
		}

		@Override
		public boolean isCancelled()
		{
			return isCancelled;
		}

		@Override
		public boolean cancel(boolean mayInterruptIfRunning)
		{
			return isCancelled = true;
		}

		@Override
		public V get()
        {
			return null;
		}

		@Override
		public V get(long timeout, TimeUnit unit)
        {
			return null;
		}

		@Override
		public long getDelay(TimeUnit unit)
		{
			return unit.convert(step * tickPerStepInMillis, TimeUnit.MILLISECONDS);
		}

		@Override
		public int compareTo(Delayed o)
		{
			return 0;
		}

		@Override
		public boolean isPeriodic()
		{
			return isPeriodic;
		}
	}
}
