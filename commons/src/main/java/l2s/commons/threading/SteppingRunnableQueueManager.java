package l2s.commons.threading;

import com.google.common.flogger.FluentLogger;
import org.apache.commons.lang3.mutable.MutableLong;

import java.util.Collection;
import java.util.Map;
import java.util.Queue;
import java.util.TreeMap;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * Менеджер очереди задач с кратным запланированным временем выполнения.
 *
 * @author G1ta0
 */
public abstract class SteppingRunnableQueueManager implements Runnable
{
	private static final FluentLogger _log = FluentLogger.forEnclosingClass();

	

	protected final long tickPerStepInMillis;
	private final Queue<SteppingScheduledFuture<?>> queue = new ConcurrentLinkedQueue<>();
	private final AtomicBoolean isRunning = new AtomicBoolean();

	public SteppingRunnableQueueManager(long tickPerStepInMillis)
	{
		this.tickPerStepInMillis = tickPerStepInMillis;
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
			this.step = initial;
			this.stepping = stepping;
			this.isPeriodic = isPeriodic;
		}

		@Override
		public void run()
		{
			if(--step == 0)
				try
				{
					r.run();
				}
				catch(Exception e)
				{
					_log.atSevere().withStackTrace(com.google.common.flogger.StackSize.FULL).withCause(e).log( "SteppingScheduledFuture.run():%s", e );
				}
				finally
				{
					if (isPeriodic)
						step = stepping;
				}
		}

		@Override
		public boolean isDone()
		{
			return isCancelled || !isPeriodic && step == 0;
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
		public V get() throws InterruptedException, ExecutionException
		{
			return null;
		}

		@Override
		public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException
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

	/**
	 * Запланировать выполнение задачи через промежуток времени
	 * @param r задача для выполнения
	 * @param delay задержка в миллисекундах
	 * @return SteppingScheduledFuture управляющий объект, отвечающий за выполенение задачи
	 */
	public SteppingScheduledFuture<?> schedule(Runnable r, long delay)
	{
		return schedule(r, delay, delay, false);
	}

	/**
	 * Запланировать выполнение задачи через равные промежутки времени, с начальной задержкой
	 * @param r задача для выполнения
	 * @param initial начальная задержка в миллисекундах
	 * @param delay период выполенения в силлисекундах
	 * @return SteppingScheduledFuture управляющий объект, отвечающий за выполенение задачи
	 */
	public SteppingScheduledFuture<?> scheduleAtFixedRate(Runnable r, long initial, long delay)
	{
		return schedule(r, initial, delay, true);
	}

	private SteppingScheduledFuture<?> schedule(Runnable r, long initial, long delay, boolean isPeriodic)
	{
		SteppingScheduledFuture<?> sr;

		long initialStepping = getStepping(initial);
		long stepping = getStepping(delay);

		queue.add(sr = new SteppingScheduledFuture<Boolean>(r, initialStepping, stepping, isPeriodic));

		return sr;
	}

	/**
	 * Выбираем "степпинг" для работы задачи:
	 * если delay меньше шага выполнения, результат будет равен 1
	 * если delay больше шага выполнения, результат будет результатом округления от деления delay / step
	 */
	private long getStepping(long delay)
	{
		delay = Math.max(0, delay);
		return delay % tickPerStepInMillis > tickPerStepInMillis / 2 ? delay / tickPerStepInMillis + 1 : delay < tickPerStepInMillis ? 1 : delay / tickPerStepInMillis;
	}

	@Override
	public void run()
	{
		if (!isRunning.compareAndSet(false, true))
		{
			_log.atWarning().log( "Slow running queue, managed by %s, queue size : %s!", this, queue.size() );
			return;
		}

		try
		{
			if (queue.isEmpty())
				return;

			for(SteppingScheduledFuture<?> sr: queue)
				if(!sr.isDone())
					sr.run();
		}
		finally
		{
			isRunning.set(false);
		}
	}

	/**
	 * Очистить очередь от выполенных и отмененных задач.
	 */
	public void purge()
	{
		if (queue.isEmpty())
			return;

		Collection<SteppingScheduledFuture<?>> purge =
				queue.stream()
						.filter(SteppingScheduledFuture::isDone)
						.collect(Collectors.toUnmodifiableList());
		queue.removeAll(purge);
	}

	public CharSequence getStats()
	{
		StringBuilder list = new StringBuilder();

		Map<String, MutableLong> stats = new TreeMap<String, MutableLong>();
		int total = 0;
		int done = 0;

		for(SteppingScheduledFuture<?> sr: queue)
		{
			if (sr.isDone())
			{
				done++;
				continue;
			}
			total++;
			MutableLong count = stats.get(sr.r.getClass().getName());
			if (count == null)
				stats.put(sr.r.getClass().getName(), count = new MutableLong(1L));
			else
				count.increment();
		}

		for(Map.Entry<String, MutableLong> e : stats.entrySet())
			list.append("\t").append(e.getKey()).append(" : ").append(e.getValue().longValue()).append("\n");

		list.append("Scheduled: ....... ").append(total).append("\n");
		list.append("Done/Cancelled: .. ").append(done).append("\n");

		return list;
	}
}
