package l2s.gameserver.instancemanager.clansearch;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import l2s.commons.dbutils.DbUtils;
import l2s.gameserver.GameServer;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.listener.actor.player.OnPlayerEnterListener;
import l2s.gameserver.listener.game.OnShutdownListener;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.actor.listener.CharListenerList;
import l2s.gameserver.model.clansearch.ClanSearchClan;
import l2s.gameserver.model.clansearch.ClanSearchParams;
import l2s.gameserver.model.clansearch.ClanSearchPlayer;
import l2s.gameserver.model.clansearch.ClanSearchWaiterParams;
import l2s.gameserver.model.clansearch.base.ClanSearchListType;
import l2s.gameserver.model.clansearch.base.ClanSearchRequestType;
import l2s.gameserver.model.clansearch.base.ClanSearchSortOrder;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.network.l2.s2c.ExPledgeWaitingListAlarm;
import l2s.gameserver.tables.ClanTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class ClanSearchManager
{
	private static final Logger _log = LoggerFactory.getLogger(ClanSearchManager.class);
	public static final long CLAN_LOCK_TIME = 300000L;
	public static final long WAITER_LOCK_TIME = 300000L;
	public static final long APPLICANT_LOCK_TIME = 300000L;
	private static final long CLAN_SEARCH_SAVE_DELAY = 3600000L;
	private static final OnPlayerEnterListener PLAYER_ENTER_LISTENER = new PlayerEnterListener();
	private static final ClanSearchManager _instance = new ClanSearchManager();
	private final OnShutdownListener _shutdownListener;
	private final TIntObjectMap<ClanSearchClan> _registeredClans;
	private final List<ClanSearchClan> _registeredClansList;
	private final TIntObjectMap<ClanSearchPlayer> _waitingPlayers;
	private final TIntObjectMap<TIntObjectMap<ClanSearchPlayer>> _applicantPlayers;
	private final ClanSearchTask _scheduledTaskExecutor;

	public ClanSearchManager()
	{
		_shutdownListener = new ShutdownListener();
		_registeredClans = new TIntObjectHashMap<>();
		_registeredClansList = new ArrayList<>();
		_waitingPlayers = new TIntObjectHashMap<>();
		_applicantPlayers = new TIntObjectHashMap<>();
		_scheduledTaskExecutor = new ClanSearchTask();
	}

	public static ClanSearchManager getInstance()
	{
		return _instance;
	}

	public void load()
	{
		_log.info(getClass().getSimpleName() + ": Loading clan search data...");
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet result = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT `clan_id`, `search_type`, `request_type`, `desc` FROM `clan_search_registered_clans`");
			result = statement.executeQuery();
			while(result.next())
				try
				{
					int clanId = result.getInt("clan_id");
					ClanSearchListType searchType = ClanSearchListType.valueOf(result.getString("search_type"));
					ClanSearchRequestType requestType = ClanSearchRequestType.valueOf(result.getString("request_type"));

					String desc = result.getString("desc");
					ClanSearchClan clan = new ClanSearchClan(clanId, searchType, requestType, desc);
					if(ClanTable.getInstance().getClan(clanId) == null)
						_scheduledTaskExecutor.scheduleClanForRemoval(clanId);
					else
						addClan(clan);
				}
				catch(Exception e)
				{
					_log.error(getClass().getSimpleName() + ": Failed to load Clan Search Engine clan row.", e);
				}
			DbUtils.closeQuietly(statement, result);
			statement = con.prepareStatement("SELECT `char_id`, `char_name`, `char_level`, `char_class_id`, `preffered_clan_id`, `search_type`, `desc` FROM `clan_search_clan_applicants`");
			result = statement.executeQuery();
			while(result.next())
				try
				{
					int charId = result.getInt("char_id");
					String charName = result.getString("char_name");
					int charLevel = result.getInt("char_level");
					int charClassId = result.getInt("char_class_id");
					int prefferedClanId = result.getInt("preffered_clan_id");
					ClanSearchListType searchType2 = ClanSearchListType.valueOf(result.getString("search_type"));
					String desc2 = result.getString("desc");
					ClanSearchPlayer player = new ClanSearchPlayer(charId, charName, charLevel, charClassId, prefferedClanId, searchType2, desc2);
					addPlayer(player);
				}
				catch(Exception e)
				{
					_log.error(getClass().getSimpleName() + ": Failed to load Clan Search Engine applicant.", e);
				}
			statement = con.prepareStatement("SELECT `char_id`, `char_name`, `char_level`, `char_class_id`, `search_type` FROM `clan_search_waiting_players`");
			result = statement.executeQuery();
			while(result.next())
				try
				{
					int charId = result.getInt("char_id");
					String charName = result.getString("char_name");
					int charLevel = result.getInt("char_level");
					int charClassId = result.getInt("char_class_id");
					ClanSearchListType searchType3 = ClanSearchListType.valueOf(result.getString("search_type"));
					addPlayer(new ClanSearchPlayer(charId, charName, charLevel, charClassId, searchType3));
				}
				catch(Exception e)
				{
					_log.error(getClass().getSimpleName() + ": Failed to load Clan Search Engine waiter.", e);
				}
		}
		catch(SQLException e2)
		{
			_log.error(getClass().getSimpleName() + ": Failed to load Clan Search Engine clan list.", e2);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, result);
		}
		_log.info(getClass().getSimpleName() + ": Loaded " + _registeredClans.size() + " registered clans.");
		_log.info(getClass().getSimpleName() + ": Loaded " + _applicantPlayers.size() + " registered players.");
		ThreadPoolManager.getInstance().scheduleAtFixedRate(_scheduledTaskExecutor, CLAN_SEARCH_SAVE_DELAY, CLAN_SEARCH_SAVE_DELAY);
		GameServer.getInstance().addListener(_shutdownListener);
		CharListenerList.addGlobal(PLAYER_ENTER_LISTENER);
	}

	public int getPageCount(int paginationLimit)
	{
		return _registeredClans.size() / paginationLimit + (_registeredClans.size() % paginationLimit != 0 ? 1 : 0);
	}

	public ClanSearchClan getClan(int clanId)
	{
		return _registeredClans.get(clanId);
	}

	public List<ClanSearchClan> listClans(int paginationLimit, ClanSearchParams params)
	{
		List<ClanSearchClan> clanList = new ArrayList<>();
		int page = Math.min(params.getCurrentPage(), getPageCount(paginationLimit));
		for(int i = 0; i < paginationLimit; ++i)
		{
			int currentIndex = i + page * paginationLimit;
			if(currentIndex >= _registeredClansList.size())
				break;
			ClanSearchClan csClan = _registeredClansList.get(currentIndex);
			Clan clan = ClanTable.getInstance().getClan(csClan.getClanId());
			if(clan != null)
				if(params.getClanLevel() < 0 || clan.getLevel() == params.getClanLevel())
					if(params.getSearchType() == ClanSearchListType.SLT_ANY || csClan.getSearchType() == params.getSearchType())
					{
						if(!params.getName().isEmpty())
							switch(params.getTargetType())
							{
								case TARGET_TYPE_LEADER_NAME:
								{
									if(clan.getLeaderName().contains(params.getName()))
										continue;
									break;
								}
								case TARGET_TYPE_CLAN_NAME:
								{
									if(!clan.getName().contains(params.getName()))
										continue;
									break;
								}
							}
						clanList.add(csClan);
					}
		}
		if(params.getSortOrder() != ClanSearchSortOrder.NONE)
			clanList.sort((csClanLeft, csClanRight) ->
			{
				if(csClanLeft == csClanRight)
					return 0;
				else
				{
					Clan clanLeft = ClanTable.getInstance().getClan(csClanLeft.getClanId());
					Clan clanRight = ClanTable.getInstance().getClan(csClanRight.getClanId());
					int left = 1;
					int right = -1;
					if(params.getSortOrder() == ClanSearchSortOrder.DESC)
					{
						left = -1;
						right = 1;
					}
					switch(params.getSortType())
					{
						case SORT_TYPE_CLAN_NAME:
						{
							return clanLeft.getLevel() > clanRight.getLevel() ? left : right;
						}
						case SORT_TYPE_LEADER_NAME:
						{
							return clanLeft.getName().compareTo(clanRight.getName()) == 1 ? left : right;
						}
						case SORT_TYPE_MEMBER_COUNT:
						{
							return clanLeft.getLeaderName().compareTo(clanRight.getName()) == 1 ? left : right;
						}
						case SORT_TYPE_CLAN_LEVEL:
						{
							return clanLeft.getAllSize() > clanRight.getAllSize() ? left : right;
						}
						case SORT_TYPE_SEARCH_LIST_TYPE:
						{
							return csClanLeft.getSearchType().ordinal() > csClanRight.getSearchType().ordinal() ? left : right;
						}
						case SORT_TYPE_REQUEST:
						{
							return csClanLeft.getRequestType().ordinal() > csClanRight.getRequestType().ordinal() ? left : right;
						}
						default:
						{
							return 0;
						}
					}
				}
			});
		return clanList;
	}

	public boolean isClanRegistered(int clanId)
	{
		return _registeredClans.containsKey(clanId);
	}

	public boolean addClan(ClanSearchClan clan)
	{

		if(_scheduledTaskExecutor.isClanLocked(clan.getClanId()))
			return false;

		ClanSearchClan existedClan = getClan(clan.getClanId());
		if(existedClan != null)
		{
			existedClan.setSearchType(clan.getSearchType());
			existedClan.setRequestType(clan.getRequestType());
			existedClan.setDesc(clan.getDesc());
			_scheduledTaskExecutor.scheduleClanForAddition(existedClan);

			try
			{
				_scheduledTaskExecutor.run();
			}
			catch(Exception e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return true;
		}

		_registeredClansList.add(clan);
		_registeredClans.put(clan.getClanId(), clan);
		_scheduledTaskExecutor.scheduleClanForAddition(clan);

		try
		{
			_scheduledTaskExecutor.run();
		}
		catch(Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}

	public boolean removeClan(ClanSearchClan clan)
	{
		if(_registeredClans.containsKey(clan.getClanId()))
		{
			_registeredClansList.remove(clan);
			_registeredClans.remove(clan.getClanId());
			_scheduledTaskExecutor.lockClan(clan.getClanId(), CLAN_LOCK_TIME);
			return true;
		}
		return false;
	}

	public ClanSearchPlayer getWaiter(int charId)
	{
		return _waitingPlayers.get(charId);
	}

	public ClanSearchPlayer findAnyApplicant(int charId)
	{
		for(TIntObjectMap<ClanSearchPlayer> players : _applicantPlayers.valueCollection())
			if(players.containsKey(charId))
				return players.get(charId);
		return null;
	}

	public ClanSearchPlayer getApplicant(int clanId, int charId)
	{
		TIntObjectMap<ClanSearchPlayer> players = _applicantPlayers.get(clanId);
		if(players == null)
			return null;
		return players.get(charId);
	}

	public boolean isApplicantRegistered(int clanId, int playerId)
	{
		TIntObjectMap<ClanSearchPlayer> players = _applicantPlayers.get(clanId);
		return players != null && players.containsKey(playerId);
	}

	public boolean isWaiterRegistered(int playerId)
	{
		return _waitingPlayers.containsKey(playerId);
	}

	public boolean addPlayer(ClanSearchPlayer player)
	{
		if(player.isApplicant())
		{
			if(_scheduledTaskExecutor.isApplicantLocked(player.getCharId()))
				return false;
			if(!isApplicantRegistered(player.getPrefferedClanId(), player.getCharId()))
			{
				TIntObjectMap<ClanSearchPlayer> players = _applicantPlayers.get(player.getPrefferedClanId());
				if(players == null)
				{
					players = new TIntObjectHashMap<>(1);
					_applicantPlayers.put(player.getPrefferedClanId(), players);
				}
				players.put(player.getCharId(), player);
				_scheduledTaskExecutor.scheduleApplicantForAddition(player);
				return true;
			}
		}
		else
		{
			if(_scheduledTaskExecutor.isWaiterLocked(player.getCharId()))
				return false;
			if(!isWaiterRegistered(player.getCharId()))
			{
				_waitingPlayers.put(player.getCharId(), player);
				_scheduledTaskExecutor.scheduleWaiterForAddition(player);
			}
		}
		return false;
	}

	public void removeApplicant(int clanId, int charId)
	{
		TIntObjectMap<ClanSearchPlayer> players = _applicantPlayers.get(clanId);
		if(players == null)
			return;
		if(!players.containsKey(charId))
			return;
		_scheduledTaskExecutor.scheduleApplicantForRemoval(charId);
		players.remove(charId);
		_scheduledTaskExecutor.lockApplicant(charId, APPLICANT_LOCK_TIME);
	}

	public List<ClanSearchPlayer> listWaiters(ClanSearchWaiterParams params)
	{
		List<ClanSearchPlayer> list = new ArrayList<>();
		for(ClanSearchPlayer csPlayer : _waitingPlayers.valueCollection())
		{
			if(csPlayer.getLevel() < params.getMinLevel())
				continue;
			if(csPlayer.getLevel() > params.getMaxLevel())
				continue;
			if(!params.getRole().isClassRole(csPlayer.getClassId()))
				continue;
			if(params.getCharName() != null && !params.getCharName().isEmpty() && !csPlayer.getName().toLowerCase().contains(params.getCharName()))
				continue;
			list.add(csPlayer);
		}
		if(params.getSortOrder() != ClanSearchSortOrder.NONE)
			Collections.sort(list, (playerLeft, playerRight) -> {
				if(playerLeft == playerRight)
					return 0;
				else
				{
					int left = -1;
					int right = 1;
					if(params.getSortOrder() == ClanSearchSortOrder.DESC)
					{
						left = 1;
						right = -1;
					}
					switch(params.getSortType())
					{
						case SORT_TYPE_NAME:
							return playerLeft.getName().compareToIgnoreCase(playerRight.getName()) < 0 ? left : right;
						case SORT_TYPE_SEARCH_TYPE:
							return playerLeft.getSearchType().ordinal() < playerRight.getSearchType().ordinal() ? left : right;
						case SORT_TYPE_ROLE:
							return playerLeft.getClassId() > playerRight.getClassId() ? left : right;
						case SORT_TYPE_LEVEL:
							return playerLeft.getLevel() > playerRight.getLevel() ? left : right;
						default:
							return 0;
					}
				}
			});
		return list;
	}

	public Collection<ClanSearchPlayer> applicantsCollection(int clanId)
	{
		TIntObjectMap<ClanSearchPlayer> players = _applicantPlayers.get(clanId);
		if(players == null)
			return Collections.emptyList();
		return players.valueCollection();
	}

	public boolean removeWaiter(int charId)
	{
		if(!_waitingPlayers.containsKey(charId))
			return false;
		_waitingPlayers.remove(charId);
		_scheduledTaskExecutor.lockWaiter(charId, WAITER_LOCK_TIME);
		return true;
	}

	public int getClanLockTime(int clanId)
	{
		return (int) (_scheduledTaskExecutor.getClanLockTime(clanId) / 1000L / 60L);
	}

	public int getWaiterLockTime(int charId)
	{
		return (int) (_scheduledTaskExecutor.getWaiterLockTime(charId) / 1000L / 60L);
	}

	public int getApplicantLockTime(int charId)
	{
		return (int) (_scheduledTaskExecutor.getApplicantLockTime(charId) / 1000L / 60L);
	}

	public void save()
	{
		try
		{
			_scheduledTaskExecutor.run();
		}
		catch(Exception e)
		{
			_log.error(getClass().getSimpleName() + ": Failed run save task.", e);
		}
	}

	private static class PlayerEnterListener implements OnPlayerEnterListener
	{
		@Override
		public void onPlayerEnter(Player player)
		{
			Clan clan = player.getClan();
			if(clan == null || player.isClanLeader() && clan.getClanMembersLimit() > clan.getAllSize())
				player.sendPacket(new ExPledgeWaitingListAlarm());
		}
	}

	private class ShutdownListener implements OnShutdownListener
	{
		@Override
		public void onShutdown()
		{
			save();
		}
	}
}
