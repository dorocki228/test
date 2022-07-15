package l2s.commons.logging;

import com.google.common.flogger.FluentLogger;

import static com.google.common.flogger.LazyArgs.lazy;

/**
 * @author VISTALL
 * @date  20:49/30.11.2010
 */
public abstract class LoggerObject
{
	protected static final FluentLogger _log = FluentLogger.forEnclosingClass();

	public final FluentLogger getLogger() {
		return _log;
	}

	public void error(String st, Exception e)
	{
		_log.atSevere().withStackTrace(com.google.common.flogger.StackSize.FULL).withCause(e).log( "%s: %s", getClass().getSimpleName(), st );
	}

	public void error(String st)
	{
		_log.atSevere().log( "%s: %s", getClass().getSimpleName(), st );
	}

	public void warn(String st, Exception e)
	{
		_log.atWarning().withStackTrace(com.google.common.flogger.StackSize.FULL).withCause(e).log( "%s: %s", getClass().getSimpleName(), st );
	}

	public void warn(String st)
	{
		_log.atWarning().log( "%s: %s", getClass().getSimpleName(), st );
	}

	public void info(String st, Exception e)
	{
		_log.atInfo().withStackTrace(com.google.common.flogger.StackSize.FULL).withCause(e).log( "%s: %s", lazy(() -> getClass().getSimpleName()), st );
	}

	public void info(String st)
	{
		_log.atInfo().log( "%s: %s", lazy(() -> getClass().getSimpleName()), st );
	}
}
