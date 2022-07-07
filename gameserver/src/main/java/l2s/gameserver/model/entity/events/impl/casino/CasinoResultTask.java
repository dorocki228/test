package l2s.gameserver.model.entity.events.impl.casino;

import java.util.HashMap;
import java.util.Map;
import l2s.gameserver.Config;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.service.CasinoEventService;
import l2s.gameserver.service.PaidActionsStatsService;
import l2s.gameserver.service.PaidActionsStatsService.PaidActionType;
import l2s.gameserver.utils.Functions;
import l2s.gameserver.utils.Language;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author KRonst
 */
public class CasinoResultTask implements Runnable {

    private final Logger logger = LoggerFactory.getLogger(CasinoEventService.class);
    private final CasinoRoom room;
    private final Player creator;
    private final Player participant;

    public CasinoResultTask(CasinoRoom room, Player creator, Player participant) {
        this.room = room;
        this.creator = creator;
        this.participant = participant;
    }

    @Override
    public void run() {
        long prize = calcPrize(room);
        if (!room.hasWinner()) {
            sendPrize(creator, prize, room.getId());
            sendPrize(participant, prize, room.getId());
        } else {
            sendPrize(room.getWinner(), prize, room.getId());
        }

        if (!room.hasWinner()) {
            announceDraw(creator, participant);
        } else {
            announceWinner(room.getWinner(), prize);
        }
    }

    private long calcPrize(CasinoRoom room) {
        long commission;
        long prize;
        if (!room.hasWinner()) {
            commission = (long) room.getBet() * Config.GVE_CASINO_TAX_PERCENT / 100;
            prize = room.getBet() - commission;
        } else {
            int bank = room.getBet() * 2;
            commission = (long) bank * Config.GVE_CASINO_TAX_PERCENT / 100;
            prize = bank - commission;
        }
        PaidActionsStatsService.getInstance()
            .updateStats(PaidActionType.CASINO, commission);
        return prize;
    }

    private void sendPrize(Player player, long amount, int roomId) {
        if (!player.isOnline()) {
            Map<Integer, Long> rewards = new HashMap<>();
            rewards.put(57, amount);
            String title = player.getLanguage() == Language.ENGLISH ? "Casino" : "Казино";
            String body = new CustomMessage("services.casino.mail.reward").toString(player.getLanguage());
            Functions.sendSystemMail(player, title, body, rewards);
            logger.info("Adena sent by mail (Room " + roomId + "): " + player.getName() + "[" + player.getObjectId()
                + "] (" + amount + ")");
        } else {
            player.addAdena(amount, true);
            logger.info("Adena sent to inventory (Room " + roomId + "): " + player.getName() + "["
                + player.getObjectId() + "] (" + amount + ")");
        }
    }

    private void announceDraw(Player player1, Player player2) {
        CustomMessage message = new CustomMessage("services.casino.npc.end.draw")
            .addString(player1.getName())
            .addString(player2.getName());
        sendNpcMessage(message);
    }

    private void announceWinner(Player winner, long prize) {
        CustomMessage message = new CustomMessage("services.casino.npc.end.winner")
            .addString(winner.getName())
            .addNumber(prize);
        sendNpcMessage(message);
    }

    private void sendNpcMessage(CustomMessage message) {
        GameObjectsStorage.getAllByNpcId(Config.GVE_CASINO_NPC_ID, true)
            .forEach(npc -> Functions.npcSayCustomInRange(npc, message, 1000));
    }
}
