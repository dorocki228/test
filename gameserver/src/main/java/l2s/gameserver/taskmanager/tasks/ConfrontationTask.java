package l2s.gameserver.taskmanager.tasks;

import l2s.gameserver.Config;
import l2s.gameserver.instancemanager.ServerVariables;
import l2s.gameserver.service.ConfrontationService;

public class ConfrontationTask extends AutomaticTask {
    @Override
    public void doTask() {
        ConfrontationService.getInstance().newPeriod();
    }

    @Override
    public long reCalcTime(boolean p0) {
        final long next = Config.FACTION_WAR_START_PERIOD_PATTERN.next(System.currentTimeMillis());
        ServerVariables.set(ConfrontationService.NEXT_UPDATE_VARIABLE, next);
        return next;
    }
}
