package l2s.commons.threading;

import com.google.common.flogger.FluentLogger;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author NB4L1
 */
public final class RejectedExecutionHandlerImpl implements RejectedExecutionHandler
{
	private static final FluentLogger _log = FluentLogger.forEnclosingClass();

	

	@Override
	public void rejectedExecution(Runnable r, ThreadPoolExecutor executor)
	{
		if(executor.isShutdown())
			return;
		
		_log.atWarning().withStackTrace(com.google.common.flogger.StackSize.FULL).withCause(new RejectedExecutionException()).log( "%s from %s", r, executor );
		
		if(Thread.currentThread().getPriority() > Thread.NORM_PRIORITY)
		{
			new Thread(r).start();
		}
		else
			r.run();
	}
}
