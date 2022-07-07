package l2s.gameserver.dao;

import l2s.commons.dbutils.DbUtils;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.model.Player;
import l2s.gameserver.skills.TimeStamp;
import l2s.gameserver.utils.SqlBatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;

public class CharacterGroupReuseDAO
{
	private static final Logger _log;
	private static final CharacterGroupReuseDAO _instance;
	public static final String DELETE_SQL_QUERY = "DELETE FROM character_group_reuse WHERE object_id=?";
	public static final String SELECT_SQL_QUERY = "SELECT * FROM character_group_reuse WHERE object_id=?";
	public static final String INSERT_SQL_QUERY = "REPLACE INTO `character_group_reuse` (`object_id`,`reuse_group`,`item_id`,`end_time`,`reuse`) VALUES";

	public static CharacterGroupReuseDAO getInstance()
	{
		return _instance;
	}

	public void select(Player player)
	{
		long curTime = System.currentTimeMillis();
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT * FROM character_group_reuse WHERE object_id=?");
			statement.setInt(1, player.getObjectId());
			rset = statement.executeQuery();
			while(rset.next())
			{
				int group = rset.getInt("reuse_group");
				int item_id = rset.getInt("item_id");
				long endTime = rset.getLong("end_time");
				long reuse = rset.getLong("reuse");
				if(endTime - curTime > 500L)
				{
					TimeStamp stamp = new TimeStamp(item_id, endTime, reuse);
					player.addSharedGroupReuse(group, stamp);
				}
			}
			DbUtils.close(statement);
			statement = con.prepareStatement("DELETE FROM character_group_reuse WHERE object_id=?");
			statement.setInt(1, player.getObjectId());
			statement.execute();
		}
		catch(Exception e)
		{
			_log.error("CharacterGroupReuseDAO.select(L2Player):", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
	}

	public void insert(Player player)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("DELETE FROM character_group_reuse WHERE object_id=?");
			statement.setInt(1, player.getObjectId());
			statement.execute();
			Map<Integer, TimeStamp> reuses = player.getSharedGroupReuses();
			if(reuses.isEmpty())
				return;
			SqlBatch b = new SqlBatch("REPLACE INTO `character_group_reuse` (`object_id`,`reuse_group`,`item_id`,`end_time`,`reuse`) VALUES");
			synchronized (reuses)
			{
				reuses.forEach((group, timeStamp) ->
				{
					if(!timeStamp.hasNotPassed())
					{
						return;
					}

					StringBuilder sb = new StringBuilder("(");
					sb.append(player.getObjectId()).append(",");
					sb.append(group).append(",");
					sb.append(timeStamp.getId()).append(",");
					sb.append(timeStamp.getEndTime()).append(",");
					sb.append(timeStamp.getReuseBasic()).append(")");
					b.write(sb.toString());
				});
			}
			if(!b.isEmpty())
				statement.executeUpdate(b.close());
		}
		catch(Exception e)
		{
			_log.error("CharacterGroupReuseDAO.insert(L2Player):", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	static
	{
		_log = LoggerFactory.getLogger(CharacterGroupReuseDAO.class);
		_instance = new CharacterGroupReuseDAO();
	}
}
