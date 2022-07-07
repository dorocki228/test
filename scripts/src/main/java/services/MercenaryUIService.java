package services;

import l2s.gameserver.handler.bypass.Bypass;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.components.HtmlMessage;
import l2s.gameserver.service.MercenaryService;

/**
 * @author mangol
 */
public class MercenaryUIService {
	@Bypass("services.MercenaryUIService:transition")
	public void transition(Player player, NpcInstance npc, String[] param) {
		if(player == null || npc == null) {
			return;
		}
		if(!npc.canBypassCheck(player)) {
			return;
		}
		if(param.length < 1) {
			return;
		}
		int indexTime = Integer.parseInt(param[0]);
		if(MercenaryService.getInstance().isCanNotTransitionForFraction(player)) {
			HtmlMessage message = new HtmlMessage(0);
			message.setFile("gve/mercenaries/mercenary_index.htm");
			message.addVar("canNotTransitionForFraction", true);
			player.sendPacket(message);
			return;
		}
		MercenaryService.getInstance().transition(player, indexTime);
	}
}
