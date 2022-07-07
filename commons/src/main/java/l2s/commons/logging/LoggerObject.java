package l2s.commons.logging;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class LoggerObject
{
	protected final Logger _log;

	public LoggerObject()
	{
		_log = LogManager.getLogger(getClass());
	}

	public void error(String st, Exception e)
	{
		_log.error(getClass().getSimpleName() + ": " + st, e);
	}

	public void error(String st)
	{
		_log.error(getClass().getSimpleName() + ": " + st);
	}

	public void warn(String st, Exception e)
	{
		_log.warn(getClass().getSimpleName() + ": " + st, e);
	}

	public void warn(String st)
	{
		_log.warn(getClass().getSimpleName() + ": " + st);
	}

	public void info(String st, Exception e)
	{
		_log.info(getClass().getSimpleName() + ": " + st, e);
	}

	public void info(String st)
	{
		_log.info(getClass().getSimpleName() + ": " + st);
	}
}
