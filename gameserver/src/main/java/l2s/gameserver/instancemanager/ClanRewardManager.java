package l2s.gameserver.instancemanager;

import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.dao.CharacterVariablesDAO;
import l2s.gameserver.dao.ClanRewardDAO;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.s2c.ExPledgeBonusMarkReset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.GregorianCalendar;

public class ClanRewardManager
{
	private static final Logger _log = LoggerFactory.getLogger(ClanRewardManager.class);
	private static final ClanRewardManager _instance = new ClanRewardManager();

	public static ClanRewardManager getInstance()
	{
		return _instance;
	}

	public void checkNewDay()
	{
		_log.info("ClanRewardManager: Check new day.");
		long newDayTime = ServerVariables.getLong("ClanRewardNewDay", 0);
		if(newDayTime == 0 || newDayTime <= System.currentTimeMillis())
		{
			_log.info("ClanRewardManager: Add new day time.");
			newDayTime = setNewDay();

			for(Player pl : GameObjectsStorage.getPlayers())
			{
				pl.unsetVar("isNewClanMember");
				pl.unsetVar("PledgeBonusOnline");
				pl.unsetVar("ClanRewardLoginAvailable");
				pl.unsetVar("ClanRewardHuntingAvailable");
			}

			CharacterVariablesDAO.getInstance().delete("isNewClanMember");
			CharacterVariablesDAO.getInstance().delete("PledgeBonusOnline");
			CharacterVariablesDAO.getInstance().delete("ClanRewardLoginAvailable");
			CharacterVariablesDAO.getInstance().delete("ClanRewardHuntingAvailable");

			for(Player player : GameObjectsStorage.getPlayers())
			{
				if(!player.isOnline() || player.getClan() == null)
					continue;
				player.startClanRewardLoginTask();
				player.sendPacket(new ExPledgeBonusMarkReset());
			}
		}
		ThreadPoolManager.getInstance().schedule(this::checkNewDay, newDayTime - System.currentTimeMillis());
	}

	private long setNewDay()
	{
		GregorianCalendar date = new GregorianCalendar();
		date.set(11, 6);
		date.set(12, 30);
		date.set(13, 0);
		date.set(14, 0);
		date.add(6, 1);
		ServerVariables.set("ClanRewardNewDay", date.getTimeInMillis());
		ClanRewardDAO.getInstance().newDay();
		return date.getTimeInMillis();
	}

	public void addLogin(int clanId, Player player)
	{
		if(player.getClan() == null || player.getClanId() != clanId)
			return;

		player.setClanRewardLoginTime(-1);
		setLogin(clanId, getLogin(player.getClanId()) + 1);
		player.getClan().broadcastClanStatus(true, true, true);
	}

	public void setLogin(int clanId, int login)
	{
		ClanRewardDAO.getInstance().setLogin(clanId, login);
	}

	public void addExp(int clanId, int exp)
	{
		ClanRewardDAO.getInstance().addExp(clanId, exp);
	}

	public int getLogin(int clanId)
	{
		return ClanRewardDAO.getInstance().getLogin(clanId);
	}

	public int getExp(int clanId)
	{
		return ClanRewardDAO.getInstance().getExp(clanId);
	}

	public int getYesterdayLogin(int clanId)
	{
		return ClanRewardDAO.getInstance().getYesterdayLogin(clanId);
	}

	public int getYesterdayExp(int clanId)
	{
		return ClanRewardDAO.getInstance().getYesterdayExp(clanId);
	}
}
