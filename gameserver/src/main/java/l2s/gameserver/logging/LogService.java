package l2s.gameserver.logging;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.message.Message;

public class LogService
{
	private static final LogService INSTANCE = new LogService();

	public void log(Level level, LoggerType loggerType, Message message)
	{
		loggerType.getLogger().log(level, message);
	}

	public void log(LoggerType loggerType, Message message)
	{
		loggerType.getLogger().info(message);
	}

	public static LogService getInstance()
	{
		return INSTANCE;
	}
}
