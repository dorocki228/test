package org.strixplatform.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import l2s.commons.database.AbstractDatabaseFactory;
import l2s.gameserver.database.DatabaseFactory;
import org.strixplatform.configs.MainConfig;
import org.strixplatform.logging.Log;

public class DatabaseManager
{
	private static final String CREATE_IF_NOT_EXIST = "CREATE TABLE IF NOT EXISTS strix_platform_hwid_ban (id INTEGER NOT NULL AUTO_INCREMENT PRIMARY KEY, hwid VARCHAR(32), time_expire BIGINT, reason VARCHAR(255), gm_name VARCHAR(50))";

	public static DatabaseManager getInstance() throws SQLException
	{
		return LazyHolder.INSTANCE;
	}

	public DatabaseManager()
	{
		checkTableExist(CREATE_IF_NOT_EXIST);
	}

	private void checkTableExist(final String quietly)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = getConnection();
			statement = con.prepareStatement(quietly);
			statement.execute();
		}
		catch(final Exception e)
		{
			Log.error("Exception in function DatabaseManager::checkTableExist. Exception: " + e.getLocalizedMessage());
			closeQuietly(con, statement);
			return;
			
		}
		finally
		{
			closeQuietly(con, statement);
			Log.info("Initialized database factory complete");
		}
	}

	public Connection getConnection() throws SQLException
	{
		return DatabaseFactory.getInstance().getConnection();
	}

	public static void close(final Connection conn) throws SQLException
	{
		if(conn != null)
		{
			conn.close();
		}
	}

	public static void close(final Statement stmt) throws SQLException
	{
		if(stmt != null)
		{
			stmt.close();
		}
	}

	public static void close(final ResultSet rs) throws SQLException
	{
		if(rs != null)
		{
			rs.close();
		}
	}

	public static void closeQuietly(final Connection conn)
	{
		try
		{
			close(conn);
		}
		catch(SQLException e)
		{
			// quiet
		}
	}

	public static void closeQuietly(final Statement stmt)
	{
		try
		{
			close(stmt);
		}
		catch(SQLException e)
		{
			// quiet
		}
	}

	public static void closeQuietly(final ResultSet rs)
	{
		try
		{
			close(rs);
		}
		catch(SQLException e)
		{
			// quiet
		}
	}

	public static void closeQuietly(final Connection conn, final Statement stmt)
	{
		try
		{
			closeQuietly(stmt);
		}
		finally
		{
			closeQuietly(conn);
		}
	}

	public static void closeQuietly(final Connection conn, final Statement stmt, final ResultSet rs)
	{

		try
		{
			closeQuietly(rs);
		}
		finally
		{
			try
			{
				closeQuietly(stmt);
			}
			finally
			{
				closeQuietly(conn);
			}
		}
	}

	private static class LazyHolder
	{
		private static final DatabaseManager INSTANCE = new DatabaseManager();
	}
}