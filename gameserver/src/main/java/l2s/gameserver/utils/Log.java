package l2s.gameserver.utils;

import com.google.common.flogger.FluentLogger;
import com.google.common.flogger.GoogleLogger;
import l2s.commons.ban.BanBindType;
import l2s.commons.text.PrintfFormat;
import l2s.gameserver.Config;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.GameObject;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.utils.loggers.GameLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Log
{
	public static final PrintfFormat LOG_BOSS_KILLED = new PrintfFormat("%s: %s[%d] killed by %s at Loc(%d %d %d) in %s");
	public static final PrintfFormat LOG_BOSS_RESPAWN = new PrintfFormat("%s: %s[%d] scheduled for respawn in %s at %s");

	private static final Logger _logGm = LoggerFactory.getLogger("gmactions");
	private static final Logger _logDebug = LoggerFactory.getLogger("debug");
	private static final Logger _logBans = LoggerFactory.getLogger("bans");

	public static final String CommissionBuy = "CommissionBuy";
	public static final String CommissionSell = "CommissionSell";
	public static final String CommissionRegistered = "CommissionRegistered";
	public static final String CommissionUnregister = "CommissionUnregister";
	public static final String CommissionExpiredReturn = "CommissionExpiredReturn";
	public static final String ClanChangeLeaderRequestAdd = "ClanChangeLeaderRequestAdd";
	public static final String ClanChangeLeaderRequestDone = "ClanChangeLeaderRequestDone";
	public static final String ClanChangeLeaderRequestCancel = "ClanChangeLeaderRequestCancel";
	public static final String ItemMallBuy = "ItemMallBuy";

	public static final String ClanWar = "ClanWar";

	public static void add(String text, String cat, Player player)
	{
		GameLogger.INSTANCE.getLogger().atInfo()
				.with(GameLogger.INSTANCE.getCategoryKey(), cat)
				.with(GameLogger.INSTANCE.getPlayerKey(), player)
				.log(text);
	}

	public static void add(String text, String cat)
	{
		add(text, cat, null);
	}

	public static void debug(String text)
	{
		_logDebug.debug(text);
	}

	public static void debug(String text, Throwable t)
	{
		_logDebug.debug(text, t);
	}

	public static void LogPetition(Player fromChar, Integer Petition_type, String Petition_text)
	{
		//TODO: implement
	}

	public static void LogAudit(Player player, String type, String msg)
	{
		//TODO: implement
	}

	public static void LogBan(Player activeChar, String command, BanBindType bindType, Object bindValueObj, int endTime, String reason, boolean auth) {
		_logBans.info(String.format("%s[%s (%d)] %s BAN: BIND_TYPE[%s] BIND_VALUE[%s] END_TIME[%s] REASON[%s] COMMAND[%s]", activeChar.isGM() ? "GM" : "MODERATOR", activeChar.getName(), activeChar.getObjectId(), auth ? "AUTH" : "GAME", bindType, bindValueObj, endTime > 0 ? TimeUtils.toSimpleFormat(endTime) : "NEVER", reason, command));
	}

	public static void LogUnban(Player activeChar, String command, BanBindType bindType, Object bindValueObj, boolean auth) {
		_logBans.info(String.format("%s[%s (%d)] %s UNBAN: BIND_TYPE[%s] BIND_VALUE[%s] COMMAND[%s]", activeChar.isGM() ? "GM" : "MODERATOR", activeChar.getName(), activeChar.getObjectId(), auth ? "AUTH" : "GAME", bindType, bindValueObj, command));
	}
}