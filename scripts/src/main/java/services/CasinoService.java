package services;

import java.util.List;
import java.util.stream.Collectors;
import l2s.gameserver.Config;
import l2s.gameserver.handler.bypass.Bypass;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.events.impl.casino.CasinoRoom;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.network.l2.components.HtmlMessage;
import l2s.gameserver.service.CasinoEventService;
import l2s.gameserver.utils.Pagination;
import l2s.gameserver.utils.Util;

/**
 * @author KRonst
 */
public class CasinoService {

    @Bypass("services.Casino:list")
    public void roomList(Player player, NpcInstance npc, String[] param) {
        if (checkCasinoEnabled(player)) {
            if(param.length < 1) {
                return;
            }
            int page = Integer.parseInt(param[0]);
            List<CasinoRoom> availableRooms = CasinoEventService.getInstance().getRooms()
                .stream()
                .filter(r -> r.getParticipant() == null)
                .filter(r -> GameObjectsStorage.getPlayer(r.getCreatorId()) != null)
                .collect(Collectors.toList());
            Pagination<CasinoRoom> rooms = new Pagination<>(availableRooms, 8);
            rooms.setPage(page);
            HtmlMessage htmlMessage = new HtmlMessage(0).setFile("gve/casino/casino_list.htm");
            htmlMessage.addVar("rooms", rooms);
            htmlMessage.addVar("player", player);
            htmlMessage.addVar("util", Util.class);
            player.sendPacket(htmlMessage);
        }
    }

    @Bypass("services.Casino:prepare")
    public void prepareRoom(Player player, NpcInstance npc, String[] param) {
        if (checkCasinoEnabled(player)) {
            HtmlMessage message = new HtmlMessage(0).setFile("gve/casino/casino_choose_bet.htm");
            player.sendPacket(message);
        }
    }

    @Bypass("services.Casino:create")
    public void createRoom(Player player, NpcInstance npc, String[] param) {
        if (checkCasinoEnabled(player)) {
            if (param.length == 0) {
                prepareRoom(player, npc, param);
                return;
            }
            String betStr = param[0];
            if (betStr.isEmpty()) {
                prepareRoom(player, npc, param);
                return;
            }
            int bet = Integer.parseInt(betStr);
            if (bet < Config.GVE_CASINO_MIN_BED || bet > Config.GVE_CASINO_MAX_BED) {
                player.sendMessage(
                    new CustomMessage("services.casino.bet.limits")
                        .addNumber(Config.GVE_CASINO_MIN_BED)
                        .addNumber(Config.GVE_CASINO_MAX_BED)
                );
                return;
            }
            long leftAfterBet = player.getAdena() + player.getWarehouse().getCountOfAdena() - bet;
            if (leftAfterBet < Config.GVE_CASINO_MIN_ADENA_LEFT) {
                player.sendMessage(
                    new CustomMessage("services.casino.bet.min.left")
                        .addNumber(Config.GVE_CASINO_MIN_ADENA_LEFT)
                );
                return;
            }
            CasinoEventService.getInstance().createRoom(player, bet);
            CasinoEventService.getInstance().showWelcomeHtml(player);
        }
    }

    @Bypass("services.Casino:close")
    public void closeRoom(Player player, NpcInstance npc, String[] param) {
        if (checkCasinoEnabled(player)) {
            CasinoEventService.getInstance().closeRoom(player);
            CasinoEventService.getInstance().showWelcomeHtml(player);
        }
    }

    @Bypass("services.Casino:join")
    public void joinRoom(Player player, NpcInstance npc, String[] param) {
        if (checkCasinoEnabled(player)) {
            if(param.length < 1) {
                return;
            }
            String idStr = param[0];
            if (idStr.isEmpty()) {
                return;
            }
            int id = Integer.parseInt(idStr);
            boolean success = CasinoEventService.getInstance().joinRoom(player, id);
            if (success) {
                player.sendMessage(new CustomMessage("services.casino.join.request.participant"));
            } else {
                player.sendMessage(new CustomMessage("services.casino.join.request.failed"));
            }
        }
    }

    private boolean checkCasinoEnabled(Player player) {
        if (Config.GVE_CASINO_ENABLED) {
            return true;
        } else {
            player.sendMessage(new CustomMessage("services.casino.disabled"));
            return false;
        }
    }
}
