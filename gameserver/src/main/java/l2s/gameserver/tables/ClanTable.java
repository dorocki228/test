package l2s.gameserver.tables;

import com.google.common.collect.ImmutableRangeMap;
import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import l2s.commons.dbutils.DbUtils;
import l2s.gameserver.Config;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.dao.ClanLeaderRequestDAO;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.idfactory.IdFactory;
import l2s.gameserver.logging.LogService;
import l2s.gameserver.logging.LoggerType;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.base.Experience;
import l2s.gameserver.model.pledge.*;
import l2s.gameserver.model.pledge.ClanWar.ClanWarPeriod;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.utils.TimeUtils;
import l2s.gameserver.utils.Util;
import org.apache.logging.log4j.message.ParameterizedMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class ClanTable
{
	private static final Logger _log = LoggerFactory.getLogger(ClanTable.class);

	private static final long CLAN_WAR_STORE_DELAY = TimeUnit.HOURS.toMillis(1);

	private static final ClanTable INSTANCE = new ClanTable();

	private final Map<Integer, Clan> _clans = new ConcurrentHashMap<>();
	private final Map<Integer, Alliance> _alliances = new ConcurrentHashMap<>();

	private final Map<Integer, ClanChangeLeaderRequest> _changeRequests = new ConcurrentHashMap<>();
	private final List<ClanWar> _clanWarUpdateCache = new ArrayList<>();

	private static final RangeMap<Integer, Integer> levelUpReward;

	static
	{
		levelUpReward = ImmutableRangeMap.<Integer, Integer>builder()
				.put(Range.closed(20, 60), 1)
				.put(Range.closed(61, Experience.getMaxLevel()), 5)
				.build();
	}

	private ScheduledFuture<?> changeClanLeaderFuture;

	private ClanTable()
	{
	}

	public Clan[] getClans()
	{
		return _clans.values().toArray(new Clan[0]);
	}

	public Alliance[] getAlliances()
	{
		return _alliances.values().toArray(new Alliance[0]);
	}

	public void load()
    {
        restoreClans();
        restoreAllies();
        restoreWars();

        _changeRequests.putAll(ClanLeaderRequestDAO.getInstance().select());
    }

	public Clan getClan(int clanId)
	{
		if(clanId <= 0)
			return null;
		return _clans.get(clanId);
	}

	public String getClanName(int clanId)
	{
		Clan c = getClan(clanId);
		return c != null ? c.getName() : "";
	}

	public Clan getClanByCharId(int charId)
	{
		if(charId <= 0)
			return null;
		for(Clan clan : getClans())
			if(clan != null && clan.isAnyMember(charId))
				return clan;
		return null;
	}

	public Alliance getAlliance(int allyId)
	{
		if(allyId <= 0)
			return null;
		return _alliances.get(allyId);
	}

	public Alliance getAllianceByCharId(int charId)
	{
		if(charId <= 0)
			return null;
		Clan charClan = getClanByCharId(charId);
		return charClan == null ? null : charClan.getAlliance();
	}

	public Map.Entry<Clan, Alliance> getClanAndAllianceByCharId(int charId)
	{
		Player player = GameObjectsStorage.getPlayer(charId);
		Clan charClan = player != null ? player.getClan() : getClanByCharId(charId);
		return new AbstractMap.SimpleEntry<>(charClan, charClan == null ? null : charClan.getAlliance());
	}

	public void restoreClans()
	{
		List<Integer> clanIds = new ArrayList<>();
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet result = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT clan_id FROM clan_data");
			result = statement.executeQuery();
			while(result.next())
				clanIds.add(result.getInt("clan_id"));
		}
		catch(Exception e)
		{
			_log.warn("Error while restoring clans!!! " + e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, result);
		}
		for(int clanId : clanIds)
		{
			Clan clan = Clan.restore(clanId);
			if(clan == null)
				_log.warn("Error while restoring clanId: " + clanId);
			else if(clan.getAllSize() <= 0)
				_log.warn("membersCount = 0 for clanId: " + clanId);
			else if(clan.getLeader() == null)
				_log.warn("Not found leader for clanId: " + clanId);
			else
				_clans.put(clan.getClanId(), clan);
		}
	}

	public void restoreAllies()
	{
		List<Integer> allyIds = new ArrayList<>();
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet result = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT ally_id FROM ally_data");
			result = statement.executeQuery();
			while(result.next())
				allyIds.add(result.getInt("ally_id"));
		}
		catch(Exception e)
		{
			_log.warn("Error while restoring allies!!! " + e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, result);
		}
		for(int allyId : allyIds)
		{
			Alliance ally = new Alliance(allyId);
			if(ally.getMembersCount() <= 0)
				_log.warn("membersCount = 0 for allyId: " + allyId);
			else if(ally.getLeader() == null)
				_log.warn("Not found leader for allyId: " + allyId);
			else
				_alliances.put(ally.getAllyId(), ally);
		}
	}

	public Clan getClanByName(String clanName)
	{
		if(!Util.isMatchingRegexp(clanName, Config.CLAN_NAME_TEMPLATE))
			return null;
		for(Clan clan : _clans.values())
			if(clan.getName().equalsIgnoreCase(clanName))
				return clan;
		return null;
	}

	public int getClansSizeByName(String clanName)
	{
		int result = 0;
		for(Clan clan : _clans.values())
			if(clan.getName().equalsIgnoreCase(clanName))
				++result;
		return result;
	}

	public Alliance getAllyByName(String allyName)
	{
		if(!Util.isMatchingRegexp(allyName, Config.ALLY_NAME_TEMPLATE))
			return null;
		for(Alliance ally : _alliances.values())
			if(ally.getAllyName().equalsIgnoreCase(allyName))
				return ally;
		return null;
	}

	public Clan createClan(Player player, String clanName)
	{
		if(getClanByName(clanName) == null)
		{
			UnitMember leader = new UnitMember(player);
			leader.setLeaderOf(0);
			Clan clan = new Clan(IdFactory.getInstance().getNextId());
			SubUnit unit = new SubUnit(clan, 0, leader, clanName, false);
			unit.addUnitMember(leader);
			clan.addSubUnit(unit, false);
			clan.store();
			player.setPledgeType(0);
			player.setClan(clan);
			player.setPowerGrade(6);
			leader.setPlayerInstance(player, false);
			_clans.put(clan.getClanId(), clan);
			clan.onEnterClan(player);
			return clan;
		}
		return null;
	}

	public void dissolveClan(Clan clan)
	{
		int leaderId = clan.getLeaderId();
		clan.flush();
		deleteClanFromDb(clan.getClanId(), leaderId);
		_clans.remove(clan.getClanId());
	}

	public static void deleteClanFromDb(int clanId, int leaderId)
	{
		long curtime = System.currentTimeMillis();
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("UPDATE characters SET clanid=0,title='',pledge_type=0,pledge_rank=0,lvl_joined_academy=0,apprentice=0,leaveclan=? WHERE clanid=?");
			statement.setLong(1, curtime / 1000L);
			statement.setInt(2, clanId);
			statement.execute();
			DbUtils.close(statement);
			statement = con.prepareStatement("UPDATE characters SET deleteclan=? WHERE obj_Id=?");
			statement.setLong(1, curtime / 1000L);
			statement.setInt(2, leaderId);
			statement.execute();
			DbUtils.close(statement);
			statement = con.prepareStatement("DELETE FROM clan_data WHERE clan_id=?");
			statement.setInt(1, clanId);
			statement.execute();
			DbUtils.close(statement);
			statement = con.prepareStatement("DELETE FROM clan_subpledges WHERE clan_id=?");
			statement.setInt(1, clanId);
			statement.execute();
			DbUtils.close(statement);
			statement = con.prepareStatement("DELETE FROM clan_privs WHERE clan_id=?");
			statement.setInt(1, clanId);
			statement.execute();
			DbUtils.close(statement);
			statement = con.prepareStatement("DELETE FROM siege_clans WHERE clan_id=?");
			statement.setInt(1, clanId);
			statement.execute();
			DbUtils.close(statement);
			statement = con.prepareStatement("DELETE FROM siege_players WHERE clan_id=?");
			statement.setInt(1, clanId);
			statement.execute();
			DbUtils.close(statement);
			statement = con.prepareStatement("DELETE FROM clan_wars WHERE attacker_clan=? OR opposing_clan=?");
			statement.setInt(1, clanId);
			statement.setInt(2, clanId);
			statement.execute();
			DbUtils.close(statement);
			statement = con.prepareStatement("DELETE FROM clan_skills WHERE clan_id=?");
			statement.setInt(1, clanId);
			statement.execute();
		}
		catch(Exception e)
		{
			_log.warn("could not dissolve clan:" + e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	public Alliance createAlliance(Player player, String allyName)
	{
		Alliance alliance = null;
		if(getAllyByName(allyName) == null)
		{
			Clan leader = player.getClan();
			alliance = new Alliance(IdFactory.getInstance().getNextId(), allyName, leader);
			alliance.store();
			_alliances.put(alliance.getAllyId(), alliance);
			player.getClan().setAllyId(alliance.getAllyId());
			for(Player temp : player.getClan().getOnlineMembers(0))
				temp.broadcastCharInfo();
		}
		return alliance;
	}

	public void dissolveAlly(Player player)
	{
		int allyId = player.getAllyId();
		for(Clan member : player.getAlliance().getMembers())
		{
			member.setAllyId(0);
			member.broadcastClanStatus(false, true, false);
			member.broadcastToOnlineMembers(SystemMsg.YOU_HAVE_WITHDRAWN_FROM_THE_ALLIANCE);
			member.setLeavedAlly();
		}
		deleteAllyFromDb(allyId);
		_alliances.remove(allyId);
		player.sendPacket(SystemMsg.THE_ALLIANCE_HAS_BEEN_DISSOLVED);
		player.getClan().setDissolvedAlly();
	}

	public void deleteAllyFromDb(int allyId)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("UPDATE clan_data SET ally_id=0 WHERE ally_id=?");
			statement.setInt(1, allyId);
			statement.execute();
			DbUtils.close(statement);
			statement = con.prepareStatement("DELETE FROM ally_data WHERE ally_id=?");
			statement.setInt(1, allyId);
			statement.execute();
		}
		catch(Exception e)
		{
			_log.warn("could not dissolve clan:" + e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	public void storeClanWar(ClanWar war, boolean force)
	{
		if(force)
			storeClanWar0(war);
		else if(!_clanWarUpdateCache.contains(war))
			_clanWarUpdateCache.add(war);
	}

	public void storeClanWar0(ClanWar war)
	{
		Clan attackerClan = war.getAttackerClan();
		Clan opposingClan = war.getOpposingClan();
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("REPLACE INTO clan_wars (attacker_clan, opposing_clan, period, period_start_time, last_kill_time, attackers_kill_counter, opposers_kill_counter) VALUES(?,?,?,?,?,?,?)");
			statement.setInt(1, attackerClan.getClanId());
			statement.setInt(2, opposingClan.getClanId());
			statement.setString(3, war.getPeriod().toString());
			statement.setInt(4, war.getCurrentPeriodStartTime());
			statement.setInt(5, war.getLastKillTime());
			statement.setInt(6, war.getAttackersKillCounter());
			statement.setInt(7, war.getOpposersKillCounter());
			statement.execute();
		}
		catch(Exception e)
		{
			_log.warn(getClass().getSimpleName() + ": Could not store clan war data:" + e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	public void storeClanWars()
	{
		synchronized (_clanWarUpdateCache)
		{
			for(ClanWar war : _clanWarUpdateCache)
				storeClanWar0(war);
			_clanWarUpdateCache.clear();
		}
	}

	public void deleteClanWar(ClanWar war)
	{
		Clan attackerClan = war.getAttackerClan();
		Clan opposingClan = war.getOpposingClan();
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("DELETE FROM clan_wars WHERE attacker_clan=? AND opposing_clan=?");
			statement.setInt(1, attackerClan.getClanId());
			statement.setInt(2, opposingClan.getClanId());
			statement.execute();
		}
		catch(Exception e)
		{
			_log.warn(getClass().getSimpleName() + ": Error removing clan wars data.", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	private void restoreWars()
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT attacker_clan, opposing_clan, period, period_start_time, last_kill_time, attackers_kill_counter, opposers_kill_counter FROM clan_wars");
			rset = statement.executeQuery();
			while(rset.next())
			{
				int attackerClanId = rset.getInt("attacker_clan");
				int opposinClanId = rset.getInt("opposing_clan");
				Clan attackerClan = getClan(attackerClanId);
				Clan opposinClan = getClan(opposinClanId);
				ClanWarPeriod period = ClanWarPeriod.valueOf(rset.getString("period"));
				int periodStartTime = rset.getInt("period_start_time");
				int lastKillTime = rset.getInt("last_kill_time");
				int attackersKillCounter = rset.getInt("attackers_kill_counter");
				int opposersKilLCounter = rset.getInt("opposers_kill_counter");
				if(attackerClan != null && opposinClan != null)
					new ClanWar(attackerClan, opposinClan, period, periodStartTime, lastKillTime, attackersKillCounter, opposersKilLCounter);
				else
					_log.warn(getClass().getSimpleName() + ": restorewars one of clans is null attacker_clan:" + attackerClanId + " opposing_clan:" + opposinClanId);
			}
		}
		catch(Exception e)
		{
			_log.warn(getClass().getSimpleName() + ": Error restoring clan wars data.", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}

		ThreadPoolManager.getInstance().scheduleAtFixedRate(this::storeClanWars, CLAN_WAR_STORE_DELAY, CLAN_WAR_STORE_DELAY);
	}

	private void checkClans()
	{
		long currentTime = System.currentTimeMillis();
		Arrays.stream(getClans())
				.filter(clan -> clan.getDisbandEndTime() > 0L && clan.getDisbandEndTime() < currentTime)
				.forEach(this::dissolveClan);

		_changeRequests.values().stream()
				.filter(changeLeaderRequest -> changeLeaderRequest.getTime() < System.currentTimeMillis())
				.forEach(changeLeaderRequest -> {
					Clan clan2 = getClan(changeLeaderRequest.getClanId());
					if (clan2 != null) {
						SubUnit subUnit = clan2.getSubUnit(0);
						if (subUnit != null) {
							UnitMember newLeader = subUnit.getUnitMember(changeLeaderRequest.getNewLeaderId());
							if (newLeader != null) {
								subUnit.setLeader(newLeader, true);
								clan2.broadcastClanStatus(true, true, false);
							}

						}
					}
					cancelRequest(changeLeaderRequest, true);
				});


		if(Config.CLAN_CHANGE_LEADER_TIME_SECOND == -1) {
			if(changeClanLeaderFuture != null) {
				changeClanLeaderFuture.cancel(false);
				changeClanLeaderFuture = null;
			}
			Instant next = Clan.CHANGE_LEADER_TIME_PATTERN.next(Instant.now());
			long millis = ChronoUnit.MILLIS.between(Instant.now(), next);
			ThreadPoolManager.getInstance().schedule(this::checkClans, millis);
		}
	}

	public void cancelRequest(ClanChangeLeaderRequest changeLeaderRequest, boolean done)
	{
		_changeRequests.remove(changeLeaderRequest.getClanId());
		ClanLeaderRequestDAO.getInstance().delete(changeLeaderRequest);

        String messagePattern = "{} Clan: {}, newLeaderId: {}, endTime: {}";
        String str = done ? "ClanChangeLeaderRequestDone" : "ClanChangeLeaderRequestCancel";
        ParameterizedMessage message = new ParameterizedMessage(messagePattern, str,
                changeLeaderRequest.getClanId(), changeLeaderRequest.getNewLeaderId(),
                TimeUtils.toSimpleFormat(changeLeaderRequest.getTime()));
        LogService.getInstance().log(LoggerType.CLAN, message);
	}

	public ClanChangeLeaderRequest getRequest(int clanId)
	{
		return _changeRequests.get(clanId);
	}

	public void addRequest(ClanChangeLeaderRequest request)
	{
		_changeRequests.put(request.getClanId(), request);
		ClanLeaderRequestDAO.getInstance().insert(request);

        String messagePattern = "ClanChangeLeaderRequestAdd Clan: {}, newLeaderId: {}, endTime: {}";
        ParameterizedMessage message = new ParameterizedMessage(messagePattern, request.getClanId(),
                request.getNewLeaderId(), TimeUtils.toSimpleFormat(request.getTime()));
        LogService.getInstance().log(LoggerType.CLAN, message);
	}

	public OptionalInt getLevelUpReward(int level)
	{
		Integer value = levelUpReward.get(level);
		return value == null ? OptionalInt.empty() : OptionalInt.of(value);
	}

	public static ClanTable getInstance()
    {
		return INSTANCE;
	}

	public void init() {
		checkClans();
		if(Config.CLAN_CHANGE_LEADER_TIME_SECOND != -1) {
			if(changeClanLeaderFuture != null) {
				return;
			}
			long millis = TimeUnit.MINUTES.toMillis(3);
			changeClanLeaderFuture = ThreadPoolManager.getInstance().scheduleAtFixedDelay(this::checkClans, millis, millis);
		}
	}
}
