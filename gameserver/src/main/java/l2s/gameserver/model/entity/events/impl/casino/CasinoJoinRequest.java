package l2s.gameserver.model.entity.events.impl.casino;

import java.util.concurrent.atomic.AtomicBoolean;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.listener.actor.player.OnAnswerListener;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Request;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.service.CasinoEventService;

/**
 * @author KRonst
 */
public class CasinoJoinRequest implements OnAnswerListener {

    private final Request request;
    private final Player creator;
    private final Player participant;
    private final AtomicBoolean active;

    public CasinoJoinRequest(Request request, Player creator, Player participant) {
        this.request = request;
        this.creator = creator;
        this.participant = participant;
        this.active = new AtomicBoolean(true);
        ThreadPoolManager.getInstance()
            .schedule(
                () -> {
                    if (active.get()) {
                        CasinoEventService.getInstance().cancelJoinRequest(creator.getObjectId());
                        participant.sendMessage(new CustomMessage("services.casino.join.cancel"));
                    }
                },
                15000
            );
    }

    @Override
    public void sayYes() {
        boolean success = true;
        Request participantRequest = participant.getRequest();
        if (participantRequest == null || participantRequest != request) {
            success = false;
        }
        Request creatorRequest = creator.getRequest();
        if (creatorRequest == null || creatorRequest != request) {
            success = false;
        }
        if (!request.isInProgress()) {
            success = false;
        }
        if (request.getRequestor() != participant) {
            success = false;
        }
        if (participant.isLogoutStarted() || creator.isLogoutStarted()) {
            success = false;
        }
        if (success) {
            participant.sendMessage(new CustomMessage("services.casino.join.request.success"));
            CasinoEventService.getInstance().play(creator, participant);
        } else {
            CustomMessage failedMessage = new CustomMessage("services.casino.join.request.failed");
            participant.sendMessage(failedMessage);
            creator.sendMessage(failedMessage);
            CasinoEventService.getInstance().cancelJoinRequest(creator.getObjectId());
        }
        active.compareAndSet(true, false);
    }

    @Override
    public void sayNo() {
        CasinoEventService.getInstance().cancelJoinRequest(creator.getObjectId());
        participant.sendMessage(new CustomMessage("services.casino.join.cancel"));
        request.cancel();
        active.compareAndSet(true, false);
    }
}
