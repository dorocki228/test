package l2s.gameserver.model.factionleader.task;

import l2s.gameserver.service.FactionLeaderService;

public class EndCycleTask implements Runnable {
    private final FactionLeaderService service;

    public EndCycleTask(FactionLeaderService service) {
        this.service = service;
    }

    @Override
    public void run() {
        service.endCycle();
    }
}
