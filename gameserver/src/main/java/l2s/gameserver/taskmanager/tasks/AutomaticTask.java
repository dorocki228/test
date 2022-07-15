package l2s.gameserver.taskmanager.tasks;

import com.google.common.flogger.FluentLogger;
import l2s.gameserver.ThreadPoolManager;

/**
 * @author VISTALL
 * @date 20:00/24.06.2011
 */
public abstract class AutomaticTask implements Runnable
{
	private static final FluentLogger _log = FluentLogger.forEnclosingClass();

	

	public AutomaticTask()
	{
		init(true);
	}

	public abstract void doTask() throws Exception;

	public abstract long reCalcTime(boolean start);

	public void init(boolean start)
	{
		ThreadPoolManager.getInstance().schedule(this, reCalcTime(start) - System.currentTimeMillis());
	}

	@Override
	public void run()
	{
		try
		{
			doTask();
		}
		catch(Exception e)
		{
			_log.atSevere().withStackTrace(com.google.common.flogger.StackSize.FULL).withCause(e).log( "Exception: %s", e );
		}
		finally
		{
			init(false);
		}
	}
}
