package l2s.gameserver.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import l2s.commons.util.Rnd;
import l2s.gameserver.Config;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.dao.CasinoDAO;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Request;
import l2s.gameserver.model.Request.L2RequestType;
import l2s.gameserver.model.entity.events.impl.casino.CasinoHistory;
import l2s.gameserver.model.entity.events.impl.casino.CasinoJoinRequest;
import l2s.gameserver.model.entity.events.impl.casino.CasinoResultTask;
import l2s.gameserver.model.entity.events.impl.casino.CasinoRoom;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.network.l2.components.HtmlMessage;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.ConfirmDlgPacket;
import l2s.gameserver.utils.Functions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author KRonst
 */
public class CasinoEventService {

    private static final CasinoEventService INSTANCE = new CasinoEventService();
    private final Logger logger = LoggerFactory.getLogger(CasinoEventService.class);
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final Map<Integer, CasinoRoom> rooms = new HashMap<>();

    private CasinoEventService() {

    }

    public static CasinoEventService getInstance() {
        return INSTANCE;
    }

    public void restore() {
        if (!Config.GVE_CASINO_ENABLED) {
            return;
        }
        Map<Integer, CasinoRoom> restoredRooms = CasinoDAO.getInstance().restore();
        rooms.putAll(restoredRooms);
        logger.info("Restored " + rooms.size() + " active Casino room(s)");
    }

    public List<CasinoRoom> getRooms() {
        readLock().lock();
        try {
            return new ArrayList<>(rooms.values());
        } finally {
            readLock().unlock();
        }
    }

    public void createRoom(Player player, int bet) {
        if (player == null) {
            return;
        }
        writeLock().lock();
        try {
            if (isRoomExists(player)) {
                player.sendMessage(new CustomMessage("services.casino.error.room.exists"));
                return;
            }
            if (!player.reduceAdena(bet, true)) {
                player.sendMessage(new CustomMessage("services.casino.error.room.adena"));
                return;
            }
            logger.info("Player " + player.getName() + "[" + player.getObjectId() + "] spent " + bet
                + " Adena for creating Casino Room");
            CasinoRoom room = CasinoDAO.getInstance().store(player.getObjectId(), player.getName(), bet);
            rooms.put(room.getId(), room);
            logger.info("Room №" + room.getId() + " created by " + player.getName() + "[" + player.getObjectId()
                + "]. Bet=" + room.getBet());
            player.sendMessage(new CustomMessage("services.casino.room.created").addNumber(room.getBet()));
        } finally {
            writeLock().unlock();
        }
    }

    public void showWelcomeHtml(Player player) {
        if(player == null) {
            return;
        }
        HtmlMessage htmlMessage = new HtmlMessage(0).setFile("gve/casino/casino_welcome.htm");
        htmlMessage.addVar("roomExists", isRoomExists(player));
        player.sendPacket(htmlMessage);
    }

    public void closeRoom(Player creator) {
        if (creator == null) {
            return;
        }
        writeLock().lock();
        try {
            CasinoRoom room = getRoomByCreatorId(creator.getObjectId());
            if (room == null) {
                return;
            }
            if (room.getParticipant() != null) {
                return;
            }
            deleteRoom(room);

            logger.info("Room №" + room.getId() + " closed by " + creator.getName() + "[" + creator.getObjectId() + "]");
            creator.addAdena(room.getBet(), true);
            logger.info("Player " + creator.getName() + "[" + creator.getObjectId() + "] closed Room №" + room.getId() + " and receives " + room.getBet() + " adena back");
        } finally {
            writeLock().unlock();
        }
    }

    private boolean isRoomExists(Player player) {
        return getRoomByCreatorId(player.getObjectId()) != null;
    }

    public CasinoRoom getRoomById(int id) {
        readLock().lock();
        try {
            return rooms.get(id);
        } finally {
            readLock().unlock();
        }
    }

    public boolean joinRoom(Player player, int id) {
        if (player == null) {
            return false;
        }
        writeLock().lock();
        try {
            CasinoRoom room = getRoomById(id);
            if (room == null) {
                return false;
            }
            if (room.getParticipant() != null) {
                return false;
            }
            Player creator = GameObjectsStorage.getPlayer(room.getCreatorId());
            if (creator == null) {
                return false;
            }
            long leftAfterBet = player.getAdena() + player.getWarehouse().getCountOfAdena() - room.getBet();
            if (leftAfterBet < Config.GVE_CASINO_MIN_ADENA_LEFT) {
                player.sendMessage(
                    new CustomMessage("services.casino.bet.min.left")
                        .addNumber(Config.GVE_CASINO_MIN_ADENA_LEFT)
                );
                return false;
            }
            room.setParticipant(player);
            sendRequestToCreator(creator, player);
            return true;
        } finally {
            writeLock().unlock();
        }
    }

    public void cancelJoinRequest(int creatorId) {
        writeLock().lock();
        try {
            CasinoRoom room = getRoomByCreatorId(creatorId);
            if (room != null) {
                room.setParticipant(null);
            }
        } finally {
            writeLock().unlock();
        }
    }

    public void play(Player creator, Player participant) {
        if (creator == null || participant == null) {
            return;
        }
        writeLock().lock();
        try {
            CasinoRoom room = getRoomByCreatorId(creator.getObjectId());
            if (room == null) {
                return;
            }
            if (!participant.reduceAdena(room.getBet(), true)) {
                room.setParticipant(null);
                creator.sendMessage(
                    new CustomMessage("services.casino.error.participant.adena")
                        .addString(participant.getName())
                );
                participant.sendMessage(
                    new CustomMessage("services.casino.error.join.adena")
                );
                return;
            }

            announceStart(creator, participant);
            logger.info(
                "Game start (Room №" + room.getId() + "): " + creator.getName() + "[" + creator.getObjectId() + "] vs "
                    + participant.getName() + "[" + participant.getObjectId() + "]. Bet=" + room.getBet());

            int creatorResult = play();
            int participantResult = play();

            if (creatorResult > participantResult) {
                room.setWinner(creator);
            } else if (participantResult > creatorResult) {
                room.setWinner(participant);
            }

            logger.info("Game stop (Room №" + room.getId() + "): " + creator.getName() + " (" + creatorResult + "), "
                + participant.getName() + " (" + participantResult + ")");
            CasinoDAO.getInstance().store(new CasinoHistory(room));
            deleteRoom(room);

            ThreadPoolManager.getInstance().schedule(() -> {
                sendTurnMessages(creator, participant, creatorResult);

                ThreadPoolManager.getInstance().schedule(() -> {
                    sendTurnMessages(participant, creator, participantResult);

                    ThreadPoolManager.getInstance()
                        .schedule(new CasinoResultTask(room, creator, participant), 1000);
                }, 1000);
            }, 1000);

        } finally {
            writeLock().unlock();
        }
    }

    private void deleteRoom(CasinoRoom room) {
        rooms.remove(room.getId());
        CasinoDAO.getInstance().delete(room.getId());
    }

    private CasinoRoom getRoomByCreatorId(int creatorId) {
        readLock().lock();
        try {
            return getRooms().stream().filter(r -> r.getCreatorId() == creatorId).findFirst().orElse(null);
        } finally {
            readLock().unlock();
        }
    }

    private void sendRequestToCreator(Player creator, Player participant) {
        Request request = new Request(L2RequestType.CUSTOM, participant, creator);
        CasinoJoinRequest joinRequest = new CasinoJoinRequest(request, creator, participant);
        String message = new CustomMessage("services.casino.join.request.creator")
            .addString(participant.getName())
            .toString(creator.getLanguage());
        ConfirmDlgPacket packet = new ConfirmDlgPacket(SystemMsg.S1, 10000).addString(message);
        creator.ask(packet, joinRequest);
    }

    private ReentrantReadWriteLock.WriteLock writeLock() {
        return lock.writeLock();
    }

    private ReentrantReadWriteLock.ReadLock readLock() {
        return lock.readLock();
    }

    private int play() {
        return Rnd.get(1, 6);
    }

    private void sendTurnMessages(Player player1, Player player2, int result) {
        sendResultMessage(player1, player2, result, true);
        sendResultMessage(player2, player1, result, false);
    }

    private void sendResultMessage(Player player, Player opponent, int result, boolean self) {
        CustomMessage message;
        if (self) {
            message = new CustomMessage("services.casino.result.self")
                .addNumber(result);
        } else {
            message = new CustomMessage("services.casino.result.opponent")
                .addString(opponent.getName())
                .addNumber(result);
        }
        if (message != null) {
            player.sendPacket(message);
        }
    }

    private void announceStart(Player player1, Player player2) {
        CustomMessage message = new CustomMessage("services.casino.npc.start")
            .addString(player1.getName())
            .addString(player2.getName());

        GameObjectsStorage.getAllByNpcId(Config.GVE_CASINO_NPC_ID, true)
            .forEach(npc -> Functions.npcSayCustomInRange(npc, message, 1000));
    }
}
