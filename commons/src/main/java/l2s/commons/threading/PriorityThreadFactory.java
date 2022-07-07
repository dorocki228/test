package l2s.commons.threading;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class PriorityThreadFactory implements ThreadFactory
{
	private static final Logger LOGGER = LoggerFactory.getLogger(PriorityThreadFactory.class);

	private final int priority;
	private final String name;
	private final AtomicInteger threadNumber;
	private final ThreadGroup group;

	public PriorityThreadFactory(String name, int priority)
	{
		this.priority = priority;
		this.name = name;
		threadNumber = new AtomicInteger(1);
		group = new ThreadGroup(this.name);
	}

	@Override
	public Thread newThread(Runnable r)
	{
		Thread t = new Thread(group, r)
		{
			@Override
			public void run()
			{
				try
				{
					super.run();
				}
				catch(Exception e)
				{
					LOGGER.error("Exception:", e);
				}
			}
		};
		t.setName(name + "-" + threadNumber.getAndIncrement());
		t.setPriority(priority);
		return t;
	}

	public ThreadGroup getGroup()
	{
		return group;
	}
}
