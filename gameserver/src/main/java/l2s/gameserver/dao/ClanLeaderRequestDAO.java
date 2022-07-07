package l2s.gameserver.dao;

import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.model.pledge.ClanChangeLeaderRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

public class ClanLeaderRequestDAO
{
	private static final Logger _log = LoggerFactory.getLogger(ClanHallDAO.class);

	private static final ClanLeaderRequestDAO _instance = new ClanLeaderRequestDAO();

	private static final String SELECT_SQL = "SELECT * FROM clan_leader_request";
	private static final String INSERT_SQL = "INSERT INTO  clan_leader_request(clan_id, new_leader_id, time) VALUES (?,?,?)";
	private static final String DELETE_SQL = "DELETE  FROM clan_leader_request WHERE clan_id=?";

	public static ClanLeaderRequestDAO getInstance()
	{
		return _instance;
	}

	public Map<Integer, ClanChangeLeaderRequest> select()
	{
		Map<Integer, ClanChangeLeaderRequest> requestList = new HashMap<>();

		try (Connection con = DatabaseFactory.getInstance().getConnection();
			 Statement statement = con.createStatement();
			 ResultSet rset = statement.executeQuery(SELECT_SQL))
		{
			while(rset.next())
			{
				int clanId = rset.getInt("clan_id");
				ClanChangeLeaderRequest request = new ClanChangeLeaderRequest(clanId,
						rset.getInt("new_leader_id"), rset.getLong("time") * 1000L);
				requestList.put(clanId, request);
			}
		}
		catch(Exception e)
		{
			_log.error("ClanLeaderRequestDAO.select(): " + e, e);
		}

		return Map.copyOf(requestList);
	}

	public void delete(ClanChangeLeaderRequest changeLeaderRequest)
	{
		try (Connection con = DatabaseFactory.getInstance().getConnection();
			 PreparedStatement statement = con.prepareStatement(DELETE_SQL)) {
			statement.setInt(1, changeLeaderRequest.getClanId());
			statement.execute();
		}
		catch(Exception e)
		{
			_log.error("ClanLeaderRequestDAO.delete(ClanChangeLeaderRequest): " + e, e);
		}
	}

	public void insert(ClanChangeLeaderRequest request)
	{
		try (Connection con = DatabaseFactory.getInstance().getConnection();
			 PreparedStatement statement = con.prepareStatement(INSERT_SQL))
		{
			statement.setInt(1, request.getClanId());
			statement.setInt(2, request.getNewLeaderId());
			statement.setLong(3, request.getTime() / 1000L);
			statement.execute();
		}
		catch(Exception e)
		{
			_log.error("ClanLeaderRequestDAO.insert(ClanChangeLeaderRequest): " + e, e);
		}
	}
}
