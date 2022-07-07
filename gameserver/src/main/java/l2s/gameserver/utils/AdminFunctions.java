package l2s.gameserver.utils;

import l2s.gameserver.Announcements;
import l2s.gameserver.Config;
import l2s.gameserver.dao.CharacterDAO;
import l2s.gameserver.logging.LogService;
import l2s.gameserver.logging.LoggerType;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.components.ChatType;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.punishment.PunishmentService;
import l2s.gameserver.punishment.PunishmentType;
import org.apache.logging.log4j.message.ParameterizedMessage;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

public final class AdminFunctions
{
	public static final Location JAIL_SPAWN = new Location(-114648, -249384, -2984);

	private AdminFunctions() {}

	/**
	 * Кикнуть игрока из игры.
	 *
	 * @param player - имя игрока
	 * @param reason - причина кика
	 * @return true если успешно, иначе false
	 */
	public static boolean kick(String player, String reason)
	{
		Player plyr = GameObjectsStorage.getPlayer(player);
		if (plyr == null)
			return false;

		return kick(plyr, reason);
	}


	public static boolean kick(Player player, String reason)
	{
		if(player.isInOfflineMode())
			player.setOfflineMode(false);

		player.kick();

		return true;
	}

	public static String banChat(Player adminChar, String adminName, String charName, int val, String reason, boolean silent)
	{
		Player player = GameObjectsStorage.getPlayer(charName);

		int objectId;
		if(player != null) {
			charName = player.getName();
			objectId = player.getObjectId();
		} else {
			objectId = CharacterDAO.getInstance().getObjectIdByName(charName);
			if(objectId == 0)
				return "Игрок " + charName + " не найден.";
		}

		if((adminName == null || adminName.isEmpty()) && adminChar != null)
			adminName = adminChar.getName();

		if(reason == null || reason.isEmpty())
			reason = "не указана"; // if no args, then "не указана" default.

		String result, announce = null;
		if(val == 0) //unban
		{
			if(adminChar != null && !adminChar.getPlayerAccess().CanUnBanChat)
				return "Вы не имеете прав на снятие бана чата.";
			if(Config.BANCHAT_ANNOUNCE)
				announce = Config.BANCHAT_ANNOUNCE_NICK && adminName != null && !adminName.isEmpty() ? adminName + " снял бан чата с игрока " + charName + "." : "С игрока " + charName + " снят бан чата.";

			String messagePattern = "Moderator {} removed chat ban from player {}.";
			ParameterizedMessage message = new ParameterizedMessage(messagePattern, adminName, charName);
			LogService.getInstance().log(LoggerType.ADMIN_ACTIONS, message);

			result = "Вы сняли бан чата с игрока " + charName + ".";
		}
		else if(val < 0)
		{
			if(Config.BANCHAT_ANNOUNCE)
				announce = Config.BANCHAT_ANNOUNCE_NICK && adminName != null && !adminName.isEmpty() ? adminName + " забанил чат игроку " + charName + " на бессрочный период, причина: " + reason + "." : "Забанен чат игроку " + charName + " на бессрочный период, причина: " + reason + ".";

			String messagePattern = "Moderator {} disable chat for player {}, reason: {}.";
			ParameterizedMessage message = new ParameterizedMessage(messagePattern, adminName, charName, reason);
			LogService.getInstance().log(LoggerType.ADMIN_ACTIONS, message);

			result = "Вы забанили чат игроку " + charName + " на бессрочный период.";
		}
		else
		{
			if(adminChar != null && !adminChar.getPlayerAccess().CanUnBanChat
					&& (player == null || PunishmentService.INSTANCE.isPunished(PunishmentType.CHAT, player)))
				return "Вы не имеете права изменять время бана.";
			if(Config.BANCHAT_ANNOUNCE)
				announce = Config.BANCHAT_ANNOUNCE_NICK && adminName != null && !adminName.isEmpty() ? adminName + " забанил чат игроку " + charName + " на " + val + " минут, причина: " + reason + "." : "Забанен чат игроку " + charName + " на " + val + " минут, причина: " + reason + ".";

			String messagePattern = "Moderator {} add chat ban to player {} for {} minutes, reason: {}.";
			ParameterizedMessage message = new ParameterizedMessage(messagePattern, adminName, charName, val, reason);
			LogService.getInstance().log(LoggerType.ADMIN_ACTIONS, message);

			result = "Вы забанили чат игроку " + charName + " на " + val + " минут.";
		}

		if(player != null)
			updateNoChannel(player, val, reason, silent);

		var bannedUntil = ZonedDateTime.now().plus(val, ChronoUnit.MINUTES);
		PunishmentService.INSTANCE.addPunishment(PunishmentType.CHAT, String.valueOf(objectId), bannedUntil, reason, adminName);

		if(announce != null && !silent)
			if(Config.BANCHAT_ANNOUNCE_FOR_ALL_WORLD)
				Announcements.getInstance().announceToAll(announce);
			else
				Announcements.shout(adminChar, announce, ChatType.CRITICAL_ANNOUNCE);

		return result;
	}

	private static void updateNoChannel(Player player, int time, String reason, boolean silent)
	{
		if (time < 0)
			player.broadcastPrivateStoreInfo();

		if(silent)
			return;

		if(time == 0)
			player.sendMessage(new CustomMessage("common.ChatUnBanned"));
		else if(time > 0)
		{
			if(reason == null || reason.isEmpty())
				player.sendMessage(new CustomMessage("common.ChatBanned").addNumber(time));
			else
				player.sendMessage(new CustomMessage("common.ChatBannedWithReason").addNumber(time).addString(reason));
		}
		else if(reason == null || reason.isEmpty())
			player.sendMessage(new CustomMessage("common.ChatBannedPermanently"));
		else
			player.sendMessage(new CustomMessage("common.ChatBannedPermanentlyWithReason").addString(reason));
	}
}
