package l2s.gameserver.instancemanager.clansearch;

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.TIntLongMap;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntLongHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import l2s.commons.dbutils.DbUtils;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.model.clansearch.ClanSearchClan;
import l2s.gameserver.model.clansearch.ClanSearchPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ClanSearchTask implements Runnable
{
	private static final Logger _log;
	private final TIntObjectMap<ClanSearchClan> _newClans;
	private final TIntObjectMap<ClanSearchPlayer> _newWaiters;
	private final TIntObjectMap<ClanSearchPlayer> _newApplicants;
	private final TIntList _removalClans;
	private final TIntList _removalWaiters;
	private final TIntList _removalApplicants;
	private final TIntLongMap _clanLocks;
	private final TIntLongMap _applicantLocks;
	private final TIntLongMap _waiterLocks;

	public ClanSearchTask()
	{
		_newClans = new TIntObjectHashMap<>();
		_newWaiters = new TIntObjectHashMap<>();
		_newApplicants = new TIntObjectHashMap<>();
		_removalClans = new TIntArrayList();
		_removalWaiters = new TIntArrayList();
		_removalApplicants = new TIntArrayList();
		_clanLocks = new TIntLongHashMap();
		_applicantLocks = new TIntLongHashMap();
		_waiterLocks = new TIntLongHashMap();
	}

	public void scheduleClanForAddition(ClanSearchClan clan)
	{
		_newClans.put(clan.getClanId(), clan);
	}

	public void scheduleWaiterForAddition(ClanSearchPlayer player)
	{
		_newWaiters.put(player.getCharId(), player);
	}

	public void scheduleApplicantForAddition(ClanSearchPlayer player)
	{
		_newApplicants.put(player.getCharId(), player);
	}

	public void scheduleClanForRemoval(int clanId)
	{
		_removalClans.add(clanId);
	}

	public void scheduleWaiterForRemoval(int playerId)
	{
		_removalWaiters.add(playerId);
	}

	public void scheduleApplicantForRemoval(int playerId)
	{
		_removalApplicants.add(playerId);
	}

	@Override
	public void run()
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			for(ClanSearchClan csClan : _newClans.valueCollection())
			{
				statement = con.prepareStatement("INSERT INTO `clan_search_registered_clans`(`clan_id`, `search_type`, `request_type`, `desc`, `timestamp`) VALUES (?, ?, ?, ?, UNIX_TIMESTAMP()) ON DUPLICATE KEY UPDATE `search_type` = ?, `request_type` = ?, `desc` = ?");
				statement.setInt(1, csClan.getClanId());
				statement.setString(2, csClan.getSearchType().name());
				statement.setString(3, csClan.getRequestType().name());
				statement.setString(4, csClan.getDesc());
				statement.setString(5, csClan.getSearchType().name());
				statement.setString(6, csClan.getRequestType().name());
				statement.setString(7, csClan.getDesc());
				statement.executeUpdate();
				DbUtils.closeQuietly(statement);
			}
		}
		catch(SQLException e)
		{
			failed(e);
		}
		if(!_newWaiters.isEmpty())
		{
            try
			{
				statement = con.prepareStatement(ClanSearchQueries.getAddWaitingPlayerQuery(_newWaiters.size()));
                int offset = 0;
                for(ClanSearchPlayer csPlayer : _newWaiters.valueCollection())
				{
					statement.setInt(++offset, csPlayer.getCharId());
					statement.setString(++offset, csPlayer.getName());
					statement.setInt(++offset, csPlayer.getLevel());
					statement.setInt(++offset, csPlayer.getClassId());
					statement.setString(++offset, csPlayer.getSearchType().name());
				}
				statement.executeUpdate();
			}
			catch(SQLException e2)
			{
				failed(e2);
			}
			finally
			{
				DbUtils.closeQuietly(statement);
			}
		}
		if(!_newApplicants.isEmpty())
		{
            try
			{
				statement = con.prepareStatement(ClanSearchQueries.getAddApplicantPlayerQuery(_newApplicants.size()));
                int offset = 0;
                for(ClanSearchPlayer csPlayer : _newApplicants.valueCollection())
				{
					statement.setInt(++offset, csPlayer.getCharId());
					statement.setInt(++offset, csPlayer.getPrefferedClanId());
					statement.setString(++offset, csPlayer.getName());
					statement.setInt(++offset, csPlayer.getLevel());
					statement.setInt(++offset, csPlayer.getClassId());
					statement.setString(++offset, csPlayer.getSearchType().name());
					statement.setString(++offset, csPlayer.getDesc());
				}
				statement.executeUpdate();
			}
			catch(SQLException e2)
			{
				failed(e2);
			}
			finally
			{
				DbUtils.closeQuietly(statement);
			}
		}
		if(!_removalClans.isEmpty())
		{
			int offset = 0;
			try
			{
				statement = con.prepareStatement(ClanSearchQueries.getRemoveClanQuery(_removalClans.size()));
				for(int clanId : _removalClans.toArray())
					statement.setInt(++offset, clanId);
				statement.executeUpdate();
			}
			catch(SQLException e2)
			{
				failed(e2);
			}
			finally
			{
				DbUtils.closeQuietly(statement);
			}
			offset = 0;
			try
			{
				statement = con.prepareStatement(ClanSearchQueries.getRemoveClanApplicants(_removalClans.size()));
				for(int clanId : _removalClans.toArray())
					statement.setInt(++offset, clanId);
				statement.executeUpdate();
			}
			catch(SQLException e2)
			{
				failed(e2);
			}
			finally
			{
				DbUtils.closeQuietly(statement);
			}
		}
		if(!_removalWaiters.isEmpty())
		{
            try
			{
				statement = con.prepareStatement(ClanSearchQueries.getRemoveWaiterQuery(_removalWaiters.size()));
                int offset = 0;
                for(int playerId : _removalWaiters.toArray())
					statement.setInt(++offset, playerId);
				statement.executeUpdate();
			}
			catch(SQLException e2)
			{
				failed(e2);
			}
			finally
			{
				DbUtils.closeQuietly(statement);
			}
		}
		if(!_removalApplicants.isEmpty())
		{
            try
			{
				statement = con.prepareStatement(ClanSearchQueries.getRemoveApplicantQuery(_removalApplicants.size()));
                int offset = 0;
                for(int charId : _removalApplicants.toArray())
					statement.setInt(++offset, charId);
				statement.executeUpdate();
			}
			catch(SQLException e2)
			{
				failed(e2);
			}
			finally
			{
				DbUtils.closeQuietly(statement);
			}
		}
		try
		{
			statement = con.prepareStatement("DELETE FROM `clan_search_registered_clans` WHERE (UNIX_TIMESTAMP() - `timestamp`) >= 60 * 60 * 24 * 30");
			statement.executeUpdate();
		}
		catch(SQLException e)
		{
			failed(e);
		}
		finally
		{
			DbUtils.closeQuietly(statement);
		}
		try
		{
			statement = con.prepareStatement("DELETE FROM `clan_search_clan_applicants` WHERE (UNIX_TIMESTAMP() - `timestamp`) >= 60 * 60 * 24 * 30");
			statement.executeUpdate();
		}
		catch(SQLException e)
		{
			failed(e);
		}
		finally
		{
			DbUtils.closeQuietly(statement);
		}
		try
		{
			statement = con.prepareStatement("DELETE FROM `clan_search_waiting_players` WHERE (UNIX_TIMESTAMP() - `timestamp`) >= 60 * 60 * 24 * 30");
			statement.executeUpdate();
		}
		catch(SQLException e)
		{
			failed(e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
		_newClans.clear();
		_newWaiters.clear();
		_newApplicants.clear();
		_removalClans.clear();
		_removalApplicants.clear();
		_removalWaiters.clear();
	}

	private void failed(Exception e)
	{
		_log.error(getClass().getSimpleName() + ": Failed to update database for clan search system.", e);
	}

	public void lockClan(int clanId, long lockTime)
	{
		_clanLocks.put(clanId, System.currentTimeMillis() + lockTime);
		ThreadPoolManager.getInstance().schedule(() -> _clanLocks.remove(clanId), lockTime);
	}

	public boolean isClanLocked(int clanId)
	{
		return _clanLocks.containsKey(clanId);
	}

	public long getClanLockTime(int clanId)
	{
		return _clanLocks.containsKey(clanId) ? Math.max(0L, System.currentTimeMillis() - _clanLocks.get(clanId)) : 0L;
	}

	public void lockWaiter(int charId, long lockTime)
	{
		_waiterLocks.put(charId, System.currentTimeMillis() + lockTime);
		ThreadPoolManager.getInstance().schedule(() -> _waiterLocks.remove(charId), lockTime);
	}

	public boolean isWaiterLocked(int charId)
	{
		return _waiterLocks.containsKey(charId);
	}

	public long getWaiterLockTime(int clanId)
	{
		return _waiterLocks.containsKey(clanId) ? Math.max(0L, System.currentTimeMillis() - _waiterLocks.get(clanId)) : 0L;
	}

	public void lockApplicant(int charId, long lockTime)
	{
		_applicantLocks.put(charId, System.currentTimeMillis() + lockTime);
		ThreadPoolManager.getInstance().schedule(() -> _applicantLocks.remove(charId), lockTime);
	}

	public boolean isApplicantLocked(int charId)
	{
		return _applicantLocks.containsKey(charId);
	}

	public long getApplicantLockTime(int clanId)
	{
		return _applicantLocks.containsKey(clanId) ? Math.max(0L, System.currentTimeMillis() - _applicantLocks.get(clanId)) : 0L;
	}

	static
	{
		_log = LoggerFactory.getLogger(ClanSearchTask.class);
	}
}
