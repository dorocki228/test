package l2s.gameserver.dao;

import com.google.common.flogger.FluentLogger;
import l2s.commons.dao.JdbcEntityState;
import l2s.commons.dbutils.DbUtils;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.model.entity.residence.clanhall.InstantClanHall;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.tables.ClanTable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * @author Bonux
**/
public class InstantClanHallDAO
{
	private static final FluentLogger _log = FluentLogger.forEnclosingClass();

	private static final InstantClanHallDAO _instance = new InstantClanHallDAO();

	private static final String SELECT_INFO_SQL_QUERY = "SELECT * FROM instant_clanhall_info WHERE id = ?";
	private static final String REPLACE_INFO_SQL_QUERY = "REPLACE INTO instant_clanhall_info (id,siege_date) VALUES (?,?)";
	private static final String SELECT_OWNER_SQL_QUERY = "SELECT owner_id FROM instant_clanhall_owners WHERE id = ?";
	private static final String REPLACE_OWNER_SQL_QUERY = "REPLACE INTO instant_clanhall_owners (owner_id,id) VALUES (?,?)";
	private static final String DELETE_OWNER_SQL_QUERY = "DELETE FROM instant_clanhall_owners WHERE owner_id=? AND id=?";

	public static InstantClanHallDAO getInstance()
	{
		return _instance;
	}

	public void select(InstantClanHall clanHall)
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(SELECT_OWNER_SQL_QUERY);
			statement.setInt(1, clanHall.getInstantId());
			rset = statement.executeQuery();
			while(rset.next())
				clanHall.addOwner(ClanTable.getInstance().getClan(rset.getInt("owner_id")), false);

			DbUtils.closeQuietly(statement, rset);

			statement = con.prepareStatement(SELECT_INFO_SQL_QUERY);
			statement.setInt(1, clanHall.getInstantId());
			rset = statement.executeQuery();
			if(rset.next())
				clanHall.getSiegeDate().setTimeInMillis(rset.getInt("siege_date") * 1000L);
		}
		catch(Exception e)
		{
			_log.atSevere().withStackTrace(com.google.common.flogger.StackSize.FULL).withCause(e).log( "InstantClanHallDAO.select(InstantClanHall):%s", e );
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
	}

	public void update(InstantClanHall clanHall)
	{
		if(!clanHall.getJdbcState().isUpdatable())
			return;

		clanHall.setJdbcState(JdbcEntityState.STORED);
		update0(clanHall);
	}

	private void update0(InstantClanHall clanHall)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(REPLACE_INFO_SQL_QUERY);
			statement.setInt(1, clanHall.getInstantId());
			statement.setInt(2, (int) (clanHall.getSiegeDate().getTimeInMillis() / 1000L));
			statement.execute();
		}
		catch(Exception e)
		{
			_log.atWarning().withStackTrace(com.google.common.flogger.StackSize.FULL).withCause(e).log( "InstantClanHallDAO#update0(InstantClanHall): %s", e );
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	public boolean insert(InstantClanHall clanHall, Clan owner)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(REPLACE_OWNER_SQL_QUERY);
			statement.setInt(1, owner.getClanId());
			statement.setInt(2, clanHall.getInstantId());
			statement.execute();
		}
		catch(Exception e)
		{
			_log.atWarning().withStackTrace(com.google.common.flogger.StackSize.FULL).withCause(e).log( "InstantClanHallDAO.insert(InstantClanHall,Clan): %s", e );
			return false;
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
		return true;
	}

	public boolean delete(InstantClanHall clanHall, Clan owner)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(DELETE_OWNER_SQL_QUERY);
			statement.setInt(1, owner.getClanId());
			statement.setInt(2, clanHall.getInstantId());
			statement.execute();
		}
		catch(Exception e)
		{
			_log.atWarning().withStackTrace(com.google.common.flogger.StackSize.FULL).withCause(e).log( "InstantClanHallDAO.delete(InstantClanHall,Clan): %s", e );
			return false;
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
		return true;
	}
}
