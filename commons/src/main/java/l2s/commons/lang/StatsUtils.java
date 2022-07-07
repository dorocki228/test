package l2s.commons.lang;

import java.lang.management.*;

public final class StatsUtils
{
	private static final MemoryMXBean memMXbean;
	private static final ThreadMXBean threadMXbean;

	public static long getMemUsed()
	{
		return memMXbean.getHeapMemoryUsage().getUsed();
	}

	public static String getMemUsedMb()
	{
		return getMemUsed() / 1048576L + " Mb";
	}

	public static long getMemMax()
	{
		return memMXbean.getHeapMemoryUsage().getMax();
	}

	public static String getMemMaxMb()
	{
		return getMemMax() / 1048576L + " Mb";
	}

	public static long getMemFree()
	{
		MemoryUsage heapMemoryUsage = memMXbean.getHeapMemoryUsage();
		return heapMemoryUsage.getMax() - heapMemoryUsage.getUsed();
	}

	public static String getMemFreeMb()
	{
		return getMemFree() / 1048576L + " Mb";
	}

	public static CharSequence getMemUsage()
	{
		double maxMem = memMXbean.getHeapMemoryUsage().getMax() / 1024.0;
		double allocatedMem = memMXbean.getHeapMemoryUsage().getCommitted() / 1024.0;
		double usedMem = memMXbean.getHeapMemoryUsage().getUsed() / 1024.0;
        StringBuilder list = new StringBuilder();
		list.append("AllowedMemory: ........... ").append((int) maxMem).append(" KB").append("\n");
		list.append("     Allocated: .......... ").append((int) allocatedMem).append(" KB (").append(Math.round(allocatedMem / maxMem * 1000000.0) / 10000.0).append("%)").append("\n");
        double nonAllocatedMem = maxMem - allocatedMem;
        list.append("     Non-Allocated: ...... ").append((int) nonAllocatedMem).append(" KB (").append(Math.round(nonAllocatedMem / maxMem * 1000000.0) / 10000.0).append("%)").append("\n");
		list.append("AllocatedMemory: ......... ").append((int) allocatedMem).append(" KB").append("\n");
		list.append("     Used: ............... ").append((int) usedMem).append(" KB (").append(Math.round(usedMem / maxMem * 1000000.0) / 10000.0).append("%)").append("\n");
        double cachedMem = allocatedMem - usedMem;
        list.append("     Unused (cached): .... ").append((int) cachedMem).append(" KB (").append(Math.round(cachedMem / maxMem * 1000000.0) / 10000.0).append("%)").append("\n");
        double useableMem = maxMem - usedMem;
        list.append("UseableMemory: ........... ").append((int) useableMem).append(" KB (").append(Math.round(useableMem / maxMem * 1000000.0) / 10000.0).append("%)").append("\n");
		return list;
	}

	public static CharSequence getThreadStats()
	{
		StringBuilder list = new StringBuilder();
		int threadCount = threadMXbean.getThreadCount();
		int daemonCount = threadMXbean.getThreadCount();
        int peakCount = threadMXbean.getPeakThreadCount();
		long totalCount = threadMXbean.getTotalStartedThreadCount();
		list.append("Live: .................... ").append(threadCount).append(" threads").append("\n");
        int nonDaemonCount = threadCount - daemonCount;
        list.append("     Non-Daemon: ......... ").append(nonDaemonCount).append(" threads").append("\n");
		list.append("     Daemon: ............. ").append(daemonCount).append(" threads").append("\n");
		list.append("Peak: .................... ").append(peakCount).append(" threads").append("\n");
		list.append("Total started: ........... ").append(totalCount).append(" threads").append("\n");
		list.append("=================================================").append("\n");
		return list;
	}

	public static CharSequence getThreadStats(boolean lockedMonitors, boolean lockedSynchronizers, boolean stackTrace)
	{
		StringBuilder list = new StringBuilder();
		for(ThreadInfo info : threadMXbean.dumpAllThreads(lockedMonitors, lockedSynchronizers))
		{
			list.append("Thread #").append(info.getThreadId()).append(" (").append(info.getThreadName()).append(")").append("\n");
			list.append("=================================================\n");
			list.append("\tgetThreadState: ...... ").append(info.getThreadState()).append("\n");
			for(MonitorInfo monitorInfo : info.getLockedMonitors())
			{
				list.append("\tLocked monitor: ....... ").append(monitorInfo).append("\n");
				list.append("\t\t[").append(monitorInfo.getLockedStackDepth()).append(".]: at ").append(monitorInfo.getLockedStackFrame()).append("\n");
			}
			for(LockInfo lockInfo : info.getLockedSynchronizers())
				list.append("\tLocked synchronizer: ...").append(lockInfo).append("\n");
			if(stackTrace)
			{
				list.append("\tgetStackTace: ..........\n");
				for(StackTraceElement trace : info.getStackTrace())
					list.append("\t\tat ").append(trace).append("\n");
			}
			list.append("=================================================\n");
		}
		return list;
	}

	public static CharSequence getGCStats()
	{
		StringBuilder list = new StringBuilder();
		for(GarbageCollectorMXBean gcBean : ManagementFactory.getGarbageCollectorMXBeans())
		{
			list.append("GarbageCollector (").append(gcBean.getName()).append(")\n");
			list.append("=================================================\n");
			list.append("getCollectionCount: ..... ").append(gcBean.getCollectionCount()).append("\n");
			list.append("getCollectionTime: ...... ").append(gcBean.getCollectionTime()).append(" ms").append("\n");
			list.append("=================================================\n");
		}
		return list;
	}

	static
	{
		memMXbean = ManagementFactory.getMemoryMXBean();
		threadMXbean = ManagementFactory.getThreadMXBean();
	}
}
