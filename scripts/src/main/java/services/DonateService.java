package services;

import l2s.gameserver.Config;
import l2s.gameserver.handler.bypass.Bypass;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.utils.ItemFunctions;

/**
 * @author mangol
 */
public class DonateService {

	@Bypass("services.DonateService:factionColorDonate")
	public void factionColorDonate(Player player, NpcInstance npc, String[] param) {
		if(!npc.canBypassCheck(player)) {
			return;
		}
		if(player.getVarBoolean("factionColor", false)) {
			player.sendMessage(new CustomMessage("factionColor.s1"));
			return;
		}
		int[] price = Config.DONATE_FACTION_COLORS_PRICE;
		tryChangeColor(player, price);
	}

	@Bypass("services.DonateService:factionColorAdena")
	public void factionColorAdena(Player player, NpcInstance npc, String[] param) {
		if(!npc.canBypassCheck(player)) {
			return;
		}
		if(player.getVarBoolean("factionColor", false)) {
			player.sendMessage(new CustomMessage("factionColor.s1"));
			return;
		}
		int[] price = Config.ADENA_FACTION_COLORS_PRICE;
		tryChangeColor(player, price);
	}

	private void tryChangeColor(Player player, int[] price) {
		int id = price[0];
		int count = price[1];
		if(!ItemFunctions.deleteItem(player, id, count)) {
			player.sendPacket(SystemMsg.INCORRECT_ITEM_COUNT);
			return;
		}
		player.setVar("factionColor", true);
		player.broadcastCharInfo();
		player.sendMessage(new CustomMessage("factionColor.s2"));
	}
}
