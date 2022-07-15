package l2s.gameserver.dao;

import com.google.common.flogger.FluentLogger;
import l2s.commons.dbutils.DbUtils;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.model.entity.residence.Residence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * @author VISTALL
 * @date 14:37/31.03.2011
 */
public class SiegePlayerDAO
{
	private static final FluentLogger _log = FluentLogger.forEnclosingClass();

	
	private static final SiegePlayerDAO _instance = new SiegePlayerDAO();
	public static final String INSERT_SQL_QUERY = "INSERT INTO siege_players(residence_id, object_id, clan_id) VALUES (?,?,?)";
	public static final String DELETE_SQL_QUERY = "DELETE FROM siege_players WHERE residence_id=? AND object_id=? AND clan_id=?";
	public static final String DELETE_SQL_QUERY2 = "DELETE FROM siege_players WHERE residence_id=?";
	public static final String SELECT_SQL_QUERY = "SELECT object_id FROM siege_players WHERE residence_id=? AND clan_id=?";

	public static SiegePlayerDAO getInstance()
	{
		return _instance;
	}

	public List<Integer> select(Residence residence, int clanId)
	{
		List<Integer> set = new ArrayList<Integer>();
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(SELECT_SQL_QUERY);
			statement.setInt(1, residence.getId());
			statement.setInt(2, clanId);
			rset = statement.executeQuery();
			while(rset.next())
				set.add(rset.getInt("object_id"));
		}
		catch(Exception e)
		{
			_log.atSevere().withStackTrace(com.google.common.flogger.StackSize.FULL).withCause(e).log( "SiegePlayerDAO.select(Residence, int): %s", e );
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		return set;
	}

	public void insert(Residence residence, int clanId, int playerId)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(INSERT_SQL_QUERY);
			statement.setInt(1, residence.getId());
			statement.setInt(2, playerId);
			statement.setInt(3, clanId);
			statement.execute();
		}
		catch(Exception e)
		{
			_log.atSevere().withStackTrace(com.google.common.flogger.StackSize.FULL).withCause(e).log( "SiegePlayerDAO.insert(Residence, int, int): %s", e );
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	public void delete(Residence residence, int clanId, int playerId)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(DELETE_SQL_QUERY);
			statement.setInt(1, residence.getId());
			statement.setInt(2, playerId);
			statement.setInt(3, clanId);
			statement.execute();
		}
		catch(Exception e)
		{
			_log.atSevere().withStackTrace(com.google.common.flogger.StackSize.FULL).withCause(e).log( "SiegePlayerDAO.delete(Residence, int, int): %s", e );
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	public void delete(Residence residence)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(DELETE_SQL_QUERY2);
			statement.setInt(1, residence.getId());
			statement.execute();
		}
		catch(Exception e)
		{
			_log.atSevere().withStackTrace(com.google.common.flogger.StackSize.FULL).withCause(e).log( "SiegePlayerDAO.delete(Residence): %s", e );
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}
}
