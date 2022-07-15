package l2s.gameserver.dao;

import com.google.common.flogger.FluentLogger;
import l2s.commons.ban.BanBindType;
import l2s.commons.ban.BanInfo;
import l2s.commons.dbutils.DbUtils;
import l2s.gameserver.database.DatabaseFactory;
import org.apache.commons.lang3.StringUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;

/**
 * @author Bonux (Head Developer L2-scripts.com)
 * 10.04.2019
 * Developed for L2-Scripts.com
 **/
public class GameBansDAO {
	private static final FluentLogger LOGGER = FluentLogger.forEnclosingClass();

	private static final GameBansDAO INSTANCE = new GameBansDAO();

	public static GameBansDAO getInstance() {
		return INSTANCE;
	}

	public static final String SELECT_SQL_QUERY = "SELECT bind_value, end_time, reason FROM game_bans WHERE bind_type=?";
	public static final String DELETE_SQL_QUERY = "DELETE FROM game_bans WHERE bind_type=? AND bind_value=?";
	public static final String INSERT_SQL_QUERY = "REPLACE INTO game_bans(bind_type, bind_value, end_time, reason) VALUES (?,?,?,?)";
	public static final String CLEAN_UP_SQL_QUERY = "DELETE FROM game_bans WHERE end_time < ? OR bind_value = ''";
	public static final String CLEAN_UP_BY_TYPE_SQL_QUERY = "DELETE FROM game_bans WHERE bind_type=?";

	public void select(Map<String, BanInfo> bans, BanBindType bindType) {
		if(!bindType.isGame())
			return;

		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try {
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(SELECT_SQL_QUERY);
			statement.setString(1, bindType.toString().toLowerCase());
			rset = statement.executeQuery();
			while(rset.next()) {
				int endTime = rset.getInt("end_time");
				if(endTime != -1 && endTime < (System.currentTimeMillis() / 1000))
					continue;

				String bindValue = rset.getString("bind_value");
				if(StringUtils.isEmpty(bindValue))
					continue;

				String reason = rset.getString("reason");
				bans.put(bindValue, new BanInfo(endTime, reason));
			}
		} catch(Exception e) {
			LOGGER.atSevere().withStackTrace(com.google.common.flogger.StackSize.FULL).withCause(e).log( "GameBansDAO.select(Map,BanBindType): " );
		} finally {
			DbUtils.closeQuietly(con, statement, rset);
		}
	}

	public boolean insert(BanBindType bindType, String bindValue, BanInfo banInfo) {
		if(!bindType.isGame())
			return false;

		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(INSERT_SQL_QUERY);
			statement.setString(1, bindType.toString().toLowerCase());
			statement.setString(2, bindValue);
			statement.setInt(3, banInfo.getEndTime());
			statement.setString(4, banInfo.getReason());
			statement.execute();
		}
		catch(Exception e)
		{
			LOGGER.atSevere().withStackTrace(com.google.common.flogger.StackSize.FULL).withCause(e).log( "GameBansDAO.insert(BanBindType,String,BanInfo): " );
			return false;
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
		return true;
	}

	public boolean delete(BanBindType bindType, String bindValue) {
		if(!bindType.isGame())
			return false;

		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(DELETE_SQL_QUERY);
			statement.setString(1, bindType.toString().toLowerCase());
			statement.setString(2, bindValue);
			statement.execute();
		}
		catch(Exception e)
		{
			LOGGER.atSevere().withStackTrace(com.google.common.flogger.StackSize.FULL).withCause(e).log( "GameBansDAO.delete(BanBindType,String): " );
			return false;
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
		return true;
	}

	public void cleanUp() {
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(CLEAN_UP_SQL_QUERY);
			statement.setInt(1, (int) (System.currentTimeMillis() / 1000));
			statement.execute();

			for(BanBindType bindType : BanBindType.VALUES) {
				if(bindType.isGame())
					continue;

				DbUtils.closeQuietly(statement);

				statement = con.prepareStatement(CLEAN_UP_BY_TYPE_SQL_QUERY);
				statement.setString(1, bindType.toString().toLowerCase());
				statement.execute();
			}
		}
		catch(Exception e)
		{
			LOGGER.atSevere().withStackTrace(com.google.common.flogger.StackSize.FULL).withCause(e).log( "GameBansDAO.cleanUp(): " );
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}
}
