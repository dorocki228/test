package l2s.gameserver.dao;

import com.google.common.flogger.FluentLogger;
import l2s.commons.dbutils.DbUtils;
import l2s.gameserver.database.DatabaseFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * @author VISTALL
 * @date 15:39/10.03.2011
 */
public class CastleDoorUpgradeDAO
{
	private static final FluentLogger _log = FluentLogger.forEnclosingClass();

	private static final CastleDoorUpgradeDAO _instance = new CastleDoorUpgradeDAO();

	public static final String SELECT_SQL_QUERY = "SELECT hp FROM castle_door_upgrade WHERE door_id=?";
	public static final String REPLACE_SQL_QUERY = "REPLACE INTO castle_door_upgrade (door_id, hp) VALUES (?,?)";
	public static final String DELETE_SQL_QUERY = "DELETE FROM castle_door_upgrade WHERE door_id=?";

	public static CastleDoorUpgradeDAO getInstance()
	{
		return _instance;
	}

	public int load(int doorId)
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(SELECT_SQL_QUERY);
			statement.setInt(1, doorId);
			rset = statement.executeQuery();

			if(rset.next())
				return rset.getInt("hp");
		}
		catch(Exception e)
		{
			_log.atSevere().withStackTrace(com.google.common.flogger.StackSize.FULL).withCause(e).log( "CastleDoorUpgradeDAO:load(int): %s", e );
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}

		return 0;
	}

	public void insert(int uId, int val)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(REPLACE_SQL_QUERY);
			statement.setInt(1, uId);
			statement.setInt(2, val);
			statement.execute();
		}
		catch(Exception e)
		{
			_log.atSevere().withStackTrace(com.google.common.flogger.StackSize.FULL).withCause(e).log( "CastleDoorUpgradeDAO:insert(int, int): %s", e );
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	public void delete(int uId)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(DELETE_SQL_QUERY);
			statement.setInt(1, uId);
			statement.execute();
		}
		catch(Exception e)
		{
			_log.atSevere().withStackTrace(com.google.common.flogger.StackSize.FULL).withCause(e).log( "CastleDoorUpgradeDAO:delete(int): %s", e );
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}
}
