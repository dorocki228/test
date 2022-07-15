package l2s.commons.threading;

//import java.lang.Thread.UncaughtExceptionHandler;

import com.google.common.flogger.FluentLogger;

/**
 * @author UnAfraid
 */
public final class RunnableWrapper implements Runnable
{
	private static final FluentLogger _log = FluentLogger.forEnclosingClass();

	

	private final Runnable _runnable;
	
	public RunnableWrapper(Runnable runnable)
	{
		_runnable = runnable;
	}
	
	@Override
	public void run()
	{
		try
		{
			_runnable.run();
		}
		catch(Exception e)
		{
			_log.atSevere().withStackTrace(com.google.common.flogger.StackSize.FULL).withCause(e).log();
		}
		/*catch(final Throwable e)
		{
			final Thread t = Thread.currentThread();
			final UncaughtExceptionHandler h = t.getUncaughtExceptionHandler();
			if (h != null)
			{
				h.uncaughtException(t, e);
			}
		}*/
	}
}
