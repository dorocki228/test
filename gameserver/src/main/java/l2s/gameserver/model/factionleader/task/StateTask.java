package l2s.gameserver.model.factionleader.task;

import l2s.gameserver.enums.FactionLeaderStateType;
import l2s.gameserver.service.FactionLeaderService;

public class StateTask implements Runnable {
    private final FactionLeaderService service;
    private FactionLeaderStateType nextState;

    public StateTask(FactionLeaderService service, FactionLeaderStateType nextState) {
        this.service = service;
        this.nextState = nextState;
    }

    @Override
    public void run() {
        service.changeState(nextState);
    }
}
