package l2s.gameserver.dao;

import com.google.common.flogger.FluentLogger;
import l2s.commons.dbutils.DbUtils;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.model.actor.instances.player.CharacterVariable;
import l2s.gameserver.utils.Strings;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Bonux
 */
public class CharacterVariablesDAO
{
	private static final FluentLogger _log = FluentLogger.forEnclosingClass();

	private static final CharacterVariablesDAO _instance = new CharacterVariablesDAO();

	public static final String SELECT_SQL_QUERY = "SELECT name, value, expire_time FROM character_variables WHERE obj_id = ?";
	public static final String SELECT_FROM_PLAYER_SQL_QUERY = "SELECT value, expire_time FROM character_variables WHERE obj_id = ? AND name = ?";
	public static final String DELETE_SQL_QUERY = "DELETE FROM character_variables WHERE obj_id = ? AND name = ? LIMIT 1";
	public static final String DELETE_ALL_SQL_QUERY = "DELETE FROM character_variables WHERE name = ?";
	public static final String DELETE_EXPIRED_SQL_QUERY = "DELETE FROM character_variables WHERE expire_time > 0 AND expire_time < ?";
	public static final String INSERT_SQL_QUERY = "REPLACE INTO character_variables (obj_id, name, value, expire_time) VALUES (?,?,?,?)";

	public CharacterVariablesDAO()
	{
		deleteExpiredVars();
	}

	public static CharacterVariablesDAO getInstance()
	{
		return _instance;
	}

	private void deleteExpiredVars()
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(DELETE_EXPIRED_SQL_QUERY);
			statement.setLong(1, System.currentTimeMillis());
			statement.execute();
		}
		catch(final Exception e)
		{
			_log.atSevere().withStackTrace(com.google.common.flogger.StackSize.FULL).withCause(e).log( "CharacterVariablesDAO:deleteExpiredVars()" );
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	public boolean delete(int playerObjId, String varName)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(DELETE_SQL_QUERY);
			statement.setInt(1, playerObjId);
			statement.setString(2, varName);
			statement.execute();
		}
		catch(final Exception e)
		{
			_log.atSevere().withStackTrace(com.google.common.flogger.StackSize.FULL).withCause(e).log( "CharacterVariablesDAO:delete(playerObjId,varName)" );
			return false;
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
		return true;
	}

	public boolean delete(String varName)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(DELETE_ALL_SQL_QUERY);
			statement.setString(1, varName);
			statement.execute();
		}
		catch(final Exception e)
		{
			_log.atSevere().withStackTrace(com.google.common.flogger.StackSize.FULL).withCause(e).log( "CharacterVariablesDAO:delete(varName)" );
			return false;
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
		return true;
	}

	public boolean insert(int playerObjId, CharacterVariable var)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(INSERT_SQL_QUERY);
			statement.setInt(1, playerObjId);
			statement.setString(2, var.getName());
			statement.setString(3, var.getValue());
			statement.setLong(4, var.getExpireTime());
			statement.executeUpdate();
		}
		catch(final Exception e)
		{
			_log.atSevere().withStackTrace(com.google.common.flogger.StackSize.FULL).withCause(e).log( "CharacterVariablesDAO:insert(playerObjId,var)" );
			return false;
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
		return true;
	}

	public List<CharacterVariable> restore(int playerObjId)
	{
		List<CharacterVariable> result = new ArrayList<CharacterVariable>();

		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(SELECT_SQL_QUERY);
			statement.setInt(1, playerObjId);
			rset = statement.executeQuery();
			while(rset.next())
			{
				long expireTime = rset.getLong("expire_time");
				if(expireTime > 0 && expireTime < System.currentTimeMillis())
					continue;

				result.add(new CharacterVariable(rset.getString("name"), Strings.stripSlashes(rset.getString("value")), expireTime));
			}
		}
		catch(Exception e)
		{
			_log.atSevere().withStackTrace(com.google.common.flogger.StackSize.FULL).withCause(e).log( "CharacterVariablesDAO:restore(playerObjId)" );
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		return result;
	}

	public String getVarFromPlayer(int playerObjId, String var)
	{
		String value = null;

		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(SELECT_FROM_PLAYER_SQL_QUERY);
			statement.setInt(1, playerObjId);
			statement.setString(2, var);
			rset = statement.executeQuery();
			if(rset.next())
			{
				long expireTime = rset.getLong("expire_time");
				if(expireTime <= 0 || expireTime >= System.currentTimeMillis())
					value = Strings.stripSlashes(rset.getString("value"));
			}
		}
		catch(Exception e)
		{
			_log.atSevere().withStackTrace(com.google.common.flogger.StackSize.FULL).withCause(e).log( "CharacterVariablesDAO:getVarFromPlayer(playerObjId,var)" );
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		return value;
	}
}
