package l2s.commons.database;

import com.google.common.flogger.FluentLogger;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Abstract database factory with usage of HikariCP.
 *
 * @author Java-man
 */
public abstract class AbstractDatabaseFactory
{
	protected final FluentLogger logger = FluentLogger.forEnclosingClass();

	private final HikariDataSource connectionPool;

	public AbstractDatabaseFactory()
	{
		try
		{
			String configPath = getConfigPath();
			HikariConfig config = new HikariConfig(configPath);
			connectionPool = new HikariDataSource(config);

			logger.atInfo().log("Database connection working.");
		}
		catch(RuntimeException e)
		{
			throw new IllegalArgumentException("Could not init database connection.", e);
		}
	}

	public void shutdown()
	{
		connectionPool.close();
	}

	public Connection getConnection()
	{
		try
		{
			return connectionPool.getConnection();
		}
		catch(SQLException e)
		{
			logger.atWarning().withStackTrace(com.google.common.flogger.StackSize.FULL).withCause(e).log("Can't get connection from database");
		}

		return null;
	}

	public HikariDataSource getDataSource() {
		return connectionPool;
	}

	protected abstract String getConfigPath();
}
