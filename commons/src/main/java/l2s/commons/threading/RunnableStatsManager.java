package l2s.commons.threading;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public final class RunnableStatsManager
{
	private static final RunnableStatsManager _instance;
	private final Map<Class<?>, ClassStat> classStats;
	private final Lock lock;

	public RunnableStatsManager()
	{
		classStats = new HashMap<>();
		lock = new ReentrantLock();
	}

	public static final RunnableStatsManager getInstance()
	{
		return _instance;
	}

	public void handleStats(Class<?> cl, long runTime)
	{
		try
		{
			lock.lock();
			ClassStat stat = classStats.get(cl);
			if(stat == null)
				stat = new ClassStat(cl);
			stat.runCount++;
			ClassStat classStat = stat;
			classStat.runTime += runTime;
			if(stat.minTime > runTime)
				stat.minTime = runTime;
			if(stat.maxTime < runTime)
				stat.maxTime = runTime;
		}
		finally
		{
			lock.unlock();
		}
	}

	private List<ClassStat> getSortedClassStats()
	{
		List<ClassStat> result = Collections.emptyList();
		try
		{
			lock.lock();
			result = Arrays.asList(classStats.values().toArray(new ClassStat[0]));
		}
		finally
		{
			lock.unlock();
		}
		result.sort((c1, c2) ->
				Long.compare(c2.maxTime, c1.maxTime));
		return result;
	}

	public CharSequence getStats()
	{
		StringBuilder list = new StringBuilder();
		List<ClassStat> stats = getSortedClassStats();
		for(ClassStat stat : stats)
		{
			list.append(stat.clazz.getName()).append(":\n");
			list.append("\tRun: ............ ").append(stat.runCount).append("\n");
			list.append("\tTime: ........... ").append(stat.runTime).append("\n");
			list.append("\tMin: ............ ").append(stat.minTime).append("\n");
			list.append("\tMax: ............ ").append(stat.maxTime).append("\n");
			list.append("\tAverage: ........ ").append(stat.runTime / stat.runCount).append("\n");
		}
		return list;
	}

	static
	{
		_instance = new RunnableStatsManager();
	}

	private class ClassStat
	{
		private final Class<?> clazz;
		private long runCount;
		private long runTime;
		private long minTime;
		private long maxTime;

		private ClassStat(Class<?> cl)
		{
			runCount = 0L;
			runTime = 0L;
			minTime = Long.MAX_VALUE;
			maxTime = Long.MIN_VALUE;
			clazz = cl;
			classStats.put(cl, this);
		}
	}
}
