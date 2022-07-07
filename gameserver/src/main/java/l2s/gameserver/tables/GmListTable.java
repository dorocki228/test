package l2s.gameserver.tables;

import l2s.gameserver.Config;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.L2GameServerPacket;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;

import java.util.List;
import java.util.stream.Collectors;

public class GmListTable
{
	public static List<Player> getAllGMs()
	{
		return GameObjectsStorage.getPlayers().stream()
				.filter(Player::isGM).collect(Collectors.toList());
	}

	public static List<Player> getAllVisibleGMs()
	{
		return GameObjectsStorage.getPlayers().stream()
				.filter(player -> player.isGM() && !player.isGMInvisible() && !Config.HIDE_GM_STATUS)
				.collect(Collectors.toList());
	}

	public static void sendListToPlayer(Player player)
	{
		List<Player> gmList = getAllVisibleGMs();
		if(gmList.isEmpty())
		{
			player.sendPacket(SystemMsg.THERE_ARE_NO_GMS_CURRENTLY_VISIBLE_IN_THE_PUBLIC_LIST_AS_THEY_MAY_BE_PERFORMING_OTHER_FUNCTIONS_AT_THE_MOMENT);
			return;
		}
		player.sendPacket(SystemMsg.GM_LIST);
		gmList.stream()
				.map(gm -> new SystemMessagePacket(SystemMsg.GM__C1).addName(gm))
				.forEach(player::sendPacket);
	}

	public static void broadcastToGMs(L2GameServerPacket packet)
	{
		getAllGMs()
				.forEach(gm -> gm.sendPacket(packet));
	}

	public static void broadcastMessageToGMs(String message)
	{
		getAllGMs()
				.forEach(gm -> gm.sendMessage(message));
	}
}
