package l2s.gameserver.dao;

import l2s.commons.dbutils.DbUtils;
import l2s.gameserver.database.DatabaseFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ClanRewardDAO
{
	private static final Logger _log = LoggerFactory.getLogger(ClanRewardDAO.class);
	private static final ClanRewardDAO _instance = new ClanRewardDAO();

	private static final String SELECT_LOGIN = "SELECT login FROM clan_reward WHERE clan_id=?";
	private static final String SELECT_EXP = "SELECT exp FROM clan_reward WHERE clan_id=?";
	private static final String SELECT_YESTERDAY_LOGIN = "SELECT yesterday_login FROM clan_reward WHERE clan_id=?";
	private static final String SELECT_YESTERDAY_EXP = "SELECT yesterday_exp FROM clan_reward WHERE clan_id=?";
	private static final String UPDATE_LOGIN = "UPDATE clan_reward SET login=? WHERE clan_id=?";
	private static final String UPDATE_EXP = "UPDATE clan_reward SET exp=? WHERE clan_id=?";
	private static final String INSERT = "REPLACE INTO clan_reward (clan_id, exp, login) VALUES (?, ?, ?)";
	private static final String SELECT_ALL = "SELECT * FROM clan_reward";
	private static final String UPDATE_ALL = "UPDATE clan_reward SET exp=0, login=0, yesterday_exp=?, yesterday_login=? WHERE clan_id=?";

	public static ClanRewardDAO getInstance()
	{
		return _instance;
	}

	public int getLogin(int clanId)
	{
        int result = 0;
        Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(SELECT_LOGIN);
			statement.setInt(1, clanId);
			rset = statement.executeQuery();
			if(rset.next())
				result = rset.getInt(1);
		}
		catch(Exception e)
		{
			_log.error("ClanRewardDAO.getLogin(Int): " + e, e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		return result;
	}

	public int getExp(int clanId)
	{
        int result = 0;
        Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(SELECT_EXP);
			statement.setInt(1, clanId);
			rset = statement.executeQuery();
			if(rset.next())
				result = rset.getInt(1);
		}
		catch(Exception e)
		{
			_log.error("ClanRewardDAO.getExp(Int): " + e, e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		return result;
	}

	public int getYesterdayLogin(int clanId)
	{
        int result = 0;
        Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(SELECT_YESTERDAY_LOGIN);
			statement.setInt(1, clanId);
			rset = statement.executeQuery();
			if(rset.next())
				result = rset.getInt(1);
		}
		catch(Exception e)
		{
			_log.error("ClanRewardDAO.getYesterdayLogin(Int): " + e, e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		return result;
	}

	public int getYesterdayExp(int clanId)
	{
        int result = 0;
        Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(SELECT_YESTERDAY_EXP);
			statement.setInt(1, clanId);
			rset = statement.executeQuery();
			if(rset.next())
				result = rset.getInt(1);
		}
		catch(Exception e)
		{
			_log.error("ClanRewardDAO.getYesterdayExp(Int): " + e, e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		return result;
	}

	@SuppressWarnings("resource")
	public void setLogin(int clanId, int login)
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(SELECT_LOGIN);
			statement.setInt(1, clanId);
			rset = statement.executeQuery();
			if(rset.next())
			{
				statement = con.prepareStatement(UPDATE_LOGIN);
				statement.setInt(1, Math.min(30, login));
				statement.setInt(2, clanId);
				statement.execute();
			}
			else
			{
				statement = con.prepareStatement(INSERT);
				statement.setInt(1, clanId);
				statement.setInt(2, 0);
				statement.setInt(3, Math.min(30, login));
				statement.execute();
			}
		}
		catch(Exception e)
		{
			_log.error("ClanRewardDAO.setLogin update row error: " + e, e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
	}

	@SuppressWarnings("resource")
	public void addExp(int clanId, int exp)
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(SELECT_EXP);
			statement.setInt(1, clanId);
			rset = statement.executeQuery();
			if(rset.next())
			{
				statement = con.prepareStatement(UPDATE_EXP);
				statement.setInt(1, Math.min(115200000, rset.getInt(1) + exp));
				statement.setInt(2, clanId);
				statement.execute();
			}
			else
			{
				statement = con.prepareStatement(INSERT);
				statement.setInt(1, clanId);
				statement.setInt(2, 0);
				statement.setInt(3, 0);
				statement.execute();
			}
		}
		catch(Exception e)
		{
			_log.error("ClanRewardDAO.addExp update row error: " + e, e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
	}

	public void newDay()
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(SELECT_ALL);
			rset = statement.executeQuery();
			while(rset.next())
			{
				statement = con.prepareStatement(UPDATE_ALL);
				statement.setInt(1, rset.getInt(2));
				statement.setInt(2, rset.getInt(3));
				statement.setInt(3, rset.getInt(1));
				statement.execute();
			}
		}
		catch(Exception e)
		{
			_log.error("ClanRewardDAO.newDay(Int): " + e, e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
	}
}
