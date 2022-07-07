package services;

import l2s.gameserver.handler.bypass.Bypass;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.events.impl.arena.ArenaRequest;
import l2s.gameserver.model.entity.events.impl.arena.enums.EArenaType;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.network.l2.components.HtmlMessage;
import l2s.gameserver.service.ArenaEventService;
import l2s.gameserver.utils.Pagination;
import l2s.gameserver.utils.Util;

import java.util.List;

/**
 * @author mangol
 */
public class ArenaService {
	@Bypass("services.ArenaService:cancelMatch")
	public void cancelMatch(Player player, NpcInstance npc, String[] param) {
		if(!npc.canBypassCheck(player)) {
			return;
		}
		boolean requestFromPlayer = ArenaEventService.getInstance().removeRequestFromPlayer(player, true);
		if(requestFromPlayer) {
			player.sendMessage(new CustomMessage("arena.s25"));
		}
		else {
			player.sendMessage(new CustomMessage("arena.s26"));
		}
	}

	@Bypass("services.ArenaService:createMatch")
	public void createMatch(Player player, NpcInstance npc, String[] param) {
		if(param.length < 2) {
			return;
		}
		if(!npc.canBypassCheck(player)) {
			return;
		}
		String type = param[0];
		String countStr = param[1];
		if(countStr.isEmpty()) {
			return;
		}
		int priceCount = Integer.parseInt(countStr);
		if(priceCount < 100) {
			player.sendMessage(new CustomMessage("arena.s22"));
			return;
		}
		EArenaType arenaType = EArenaType.getTypeFromName(type);
		ArenaEventService.getInstance().createBattle(arenaType, player, priceCount);
	}

    @Bypass("services.ArenaService:askBattle")
    public void askBattle(Player player, NpcInstance npc, String[] param) {
        if(param.length < 1) {
            return;
        }
        if(!npc.canBypassCheck(player)) {
            return;
        }
        int requestId = Integer.parseInt(param[0]);
        ArenaEventService.getInstance().askStartBattle(requestId, player);
    }

	@Bypass("services.ArenaService:matchList")
	public void matchList(Player player, NpcInstance npc, String[] param) {
		if(param.length < 1) {
			return;
		}
		if(!npc.canBypassCheck(player)) {
			return;
		}
		int page = Integer.parseInt(param[0]);
		List<ArenaRequest> requestList = ArenaEventService.getInstance().getRequestList();
		Pagination<ArenaRequest> pagination = new Pagination<>(requestList, 2);
		pagination.setPage(page);
		HtmlMessage htmlMessage = new HtmlMessage(0);
		htmlMessage.setFile("gve/pvparena/pvparena_match_list.htm");
		htmlMessage.addVar("pagination", pagination);
		htmlMessage.addVar("util", Util.class);
		player.sendPacket(htmlMessage);
	}
}
