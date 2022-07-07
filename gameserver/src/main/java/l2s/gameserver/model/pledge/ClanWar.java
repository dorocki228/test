package l2s.gameserver.model.pledge;

import l2s.gameserver.Config;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;
import l2s.gameserver.tables.ClanTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class ClanWar
{
	private static final Logger _log = LoggerFactory.getLogger(ClanWar.class);

	public static final long PREPARATION_PERIOD_DURATION = TimeUnit.DAYS.toMillis(Config.CLAN_WAR_PREPARATION_DAYS_PERIOD);

	private final Clan _attackerClan;
	private final Clan _opposingClan;
	private ClanWarPeriod _period;
	private int _lastKillTime;
	private int _currentPeriodStartTime;
	private Future<?> _peaceTask;
	private int _attackersKillCounter;
	private int _opposersKillCounter;

	public ClanWar(Clan attackerClan, Clan opposingClan, ClanWarPeriod period, int currentPeriodStartTime, int lastKillTime, int attackersKillCounter, int opposersKillCounter)
	{
		_attackerClan = attackerClan;
		_opposingClan = opposingClan;
		_period = period;
		_currentPeriodStartTime = currentPeriodStartTime;
		_lastKillTime = lastKillTime;
		_attackersKillCounter = attackersKillCounter;
		_opposersKillCounter = opposersKillCounter;
		_attackerClan.addClanWar(this);
		_opposingClan.addClanWar(this);
		onChange(null);
	}

	public void onKill(Player killer, Player victim)
	{
		Clan killerClan = killer.getClan();
		if(killerClan == null)
			return;
		Clan victimClan = victim.getClan();
		if(victimClan == null)
			return;

		_lastKillTime = (int) (System.currentTimeMillis() / 1000L);
		if(killerClan == getAttackerClan())
			++_attackersKillCounter;
		else if(killerClan == getOpposingClan() && (getPeriod() != ClanWarPeriod.PREPARATION || !victim.isPK()))
			++_opposersKillCounter;
		save(false);

		if(_period == ClanWarPeriod.PREPARATION && _opposersKillCounter == 5)
		{
			setPeriod(killerClan, ClanWarPeriod.MUTUAL);
			return;
		}

		if(_period != ClanWarPeriod.MUTUAL)
			return;

		if(victimClan.getReputationScore() > 0)
			killerClan.incReputation(Config.CLAN_WAR_REPUTATION_SCORE_PER_KILL, true, "ClanWar");
		if(killerClan.getReputationScore() > 0 && victim.getPledgeType() != -1)
			victimClan.incReputation(-Config.CLAN_WAR_REPUTATION_SCORE_PER_KILL, true, "ClanWar");

/*
		if(victim.getPledgeType() != -1)
			victimClan.broadcastToOnlineMembers(new SystemMessagePacket(SystemMsg.BECAUSE_C1_WAS_KILLED_BY_A_CLAN_MEMBER_OF_S2_CLAN_FAME_POINTS_DECREASED_BY_1).addName(victim).addString(killerClan.getName()));
		killerClan.broadcastToOnlineMembers(new SystemMessagePacket(SystemMsg.BECAUSE_A_CLAN_MEMBER_OF_S1_WAS_KILLED_BY_C2_CLAN_FAME_POINTS_INCREASED_BY_1).addString(victimClan.getName()).addName(killer));
*/
	}

	public int getAttackersKillCounter()
	{
		return _attackersKillCounter;
	}

	public int getPointDiff(Clan clan)
	{
		return getAttackerClan() == clan ? getAttackersKillCounter() - getOpposersKillCounter() : getOpposersKillCounter() - getAttackersKillCounter();
	}

	public WarProgress calculateWarProgress(int pointDiff)
	{
		if(pointDiff <= -50)
			return WarProgress.VERY_LOW;
		if(pointDiff > -50 && pointDiff <= -20)
			return WarProgress.LOW;
		if(pointDiff > -20 && pointDiff <= 19)
			return WarProgress.NORMAL;
		if(pointDiff > 19 && pointDiff <= 49)
			return WarProgress.HIGH;
		return WarProgress.VERY_HIGH;
	}

	public ClanWarState getClanWarState(Clan clan)
	{
		if(_period == ClanWarPeriod.PREPARATION)
			return ClanWarState.PREPARATION;
		if(_period == ClanWarPeriod.MUTUAL)
			return ClanWarState.MUTUAL;
		if(_period != ClanWarPeriod.PEACE)
			return ClanWarState.REJECTED;
		int points = getPointDiff(clan);
		if(points == 0)
			return ClanWarState.TIE;
		if(points < 0)
			return ClanWarState.LOSS;
		return ClanWarState.WIN;
	}

	public boolean isAttacker(Clan clan)
	{
		return getAttackerClan() == clan;
	}

	public boolean isOpposing(Clan clan)
	{
		return getOpposingClan() == clan;
	}

	public Clan getAttackerClan()
	{
		return _attackerClan;
	}

	public Clan getOpposingClan()
	{
		return _opposingClan;
	}

	public int getOpposersKillCounter()
	{
		return _opposersKillCounter;
	}

	public int getLastKillTime()
	{
		return _lastKillTime;
	}

	public ClanWarPeriod getPeriod()
	{
		return _period;
	}

	public long getPeriodDuration()
	{
		switch(_period)
		{
			case PREPARATION:
				return System.currentTimeMillis() - _currentPeriodStartTime * 1000L;
			case MUTUAL:
				return 0L;
			case PEACE:
				return System.currentTimeMillis() - _currentPeriodStartTime * 1000L;
			default:
				return 0L;
		}
	}

	public int getCurrentPeriodStartTime()
	{
		return _currentPeriodStartTime;
	}

	public void accept(Clan requestor)
	{
		if(requestor == getOpposingClan())
			setPeriod(requestor, ClanWarPeriod.MUTUAL);
	}

	public void cancel(Clan requester)
	{
		Clan winnerClan = requester == getAttackerClan() ? getOpposingClan() : getAttackerClan();
		Clan looserClan = requester == getAttackerClan() ? getAttackerClan() : getOpposingClan();
		_attackersKillCounter = 0;
		_opposersKillCounter = 0;
		looserClan.incReputation(-500, true, "ClanWar");
		looserClan.broadcastToOnlineMembers(new SystemMessagePacket(SystemMsg.YOU_HAVE_SURRENDERED_TO_THE_S1_CLAN).addString(winnerClan.getName()));
		winnerClan.broadcastToOnlineMembers(new SystemMessagePacket(SystemMsg.YOU_HAVE_WON_THE_WAR_OVER_THE_S1_CLAN).addString(looserClan.getName()));

		//_attackerClan = looserClan;
		//_opposingClan = winnerClan;

		setPeriod(requester, ClanWarPeriod.PREPARATION);
	}

	public void setPeriod(Clan requester, ClanWarPeriod period)
	{
		if(_period == period)
			return;
		_period = period;
		_currentPeriodStartTime = (int) (System.currentTimeMillis() / 1000L);

		onChange(requester);
	}

	private void onChange(Clan requester)
	{
		if(_period == ClanWarPeriod.PREPARATION)
		{
			getAttackerClan().updateClanWarStatus(this, requester);
			getOpposingClan().updateClanWarStatus(this, requester);

			long peacePeriodStartTime = _currentPeriodStartTime * 1000L + PREPARATION_PERIOD_DURATION - System.currentTimeMillis();
			if(peacePeriodStartTime > 0L)
			{
				_peaceTask = ThreadPoolManager.getInstance().schedule(new PeaceTask(), peacePeriodStartTime);
				Calendar scheduleTime = Calendar.getInstance();
				scheduleTime.setTimeInMillis(System.currentTimeMillis() + peacePeriodStartTime);
				_log.info(getClass().getSimpleName() + ": Clan war between clans " + getAttackerClan().getClanId() + " and " + getOpposingClan().getClanId() + " in preparation mode. Scheduled for end war at " + scheduleTime.getTime());
			}
			else
			{
				setPeriod(requester, ClanWarPeriod.PEACE);
				return;
			}
		}
		else if(_period == ClanWarPeriod.MUTUAL)
		{
			getAttackerClan().updateClanWarStatus(this, requester);
			getAttackerClan().broadcastToOnlineMembers(new SystemMessagePacket(SystemMsg.A_CLAN_WAR_WITH_CLAN_S1_HAS_STARTED).addString(getOpposingClan().getName()));
			getOpposingClan().updateClanWarStatus(this, requester);
			getOpposingClan().broadcastToOnlineMembers(new SystemMessagePacket(SystemMsg.A_CLAN_WAR_WITH_CLAN_S1_HAS_STARTED).addString(getAttackerClan().getName()));

			_log.info(getClass().getSimpleName() + ": Clan war between clans " + getAttackerClan().getClanId() + " and " + getOpposingClan().getClanId() + " in mutal mode.");
		}
		else if(_period == ClanWarPeriod.PEACE)
		{
			getAttackerClan().updateClanWarStatus(this, requester);
			getAttackerClan().broadcastToOnlineMembers(new SystemMessagePacket(SystemMsg.WAR_WITH_THE_S1_CLAN_HAS_ENDED).addString(getOpposingClan().getName()));
			getOpposingClan().updateClanWarStatus(this, requester);
			getOpposingClan().broadcastToOnlineMembers(new SystemMessagePacket(SystemMsg.WAR_WITH_THE_S1_CLAN_HAS_ENDED).addString(getAttackerClan().getName()));
			getAttackerClan().removeClanWar(this);
			getOpposingClan().removeClanWar(this);
			ClanTable.getInstance().deleteClanWar(this);
			_log.info(getClass().getSimpleName() + ": Clan war between clans " + getAttackerClan().getName() + " and " + getOpposingClan().getName() + " has end.");
		}

		save(true);

		if(_period != ClanWarPeriod.PREPARATION && _peaceTask != null)
		{
			_peaceTask.cancel(false);
			_peaceTask = null;
		}

		if(_period == ClanWarPeriod.PREPARATION)
			return;

		Clan clan = getAttackerClan();
		if(clan != null)
			for(Player member : clan.getOnlineMembers(-1))
				member.broadcastCharInfo();
		clan = getOpposingClan();
		if(clan != null)
			for(Player member : clan.getOnlineMembers(-1))
				member.broadcastCharInfo();
	}

	private class PeaceTask implements Runnable
	{
		@Override
		public void run()
		{
			if(_period != ClanWarPeriod.PREPARATION)
				return;
			else
			{
				_log.info(getClass().getSimpleName() + ": Clan war " + getAttackerClan().getClanId() + " and " + getOpposingClan().getClanId() + " run PeaceTask ");

				setPeriod(null, ClanWarPeriod.PEACE);
				return;
			}
		}

	}

	private void save(boolean force)
	{
		ClanTable.getInstance().storeClanWar(this, force);
	}

	public enum ClanWarState
	{
		PREPARATION,
		REJECTED,
		MUTUAL,
		WIN,
		LOSS,
		TIE
    }

	public enum WarProgress
	{
		VERY_LOW,
		LOW,
		NORMAL,
		HIGH,
		VERY_HIGH
    }

	public enum ClanWarPeriod
	{
		PREPARATION,
		MUTUAL,
		PEACE
    }
}
