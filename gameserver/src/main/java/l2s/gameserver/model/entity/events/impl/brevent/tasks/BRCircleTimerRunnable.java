package l2s.gameserver.model.entity.events.impl.brevent.tasks;

import l2s.gameserver.model.entity.events.impl.brevent.BREvent;

/**
 * @author : Nami
 * @date : 21.06.2018
 * @time : 17:19
 * <p/>
 */
public class BRCircleTimerRunnable implements Runnable {
    private BREvent brEvent;

    public BRCircleTimerRunnable(BREvent brEvent)
    {
        this.brEvent = brEvent;
    }

    @Override
    public void run() {
        brEvent.nextCircle();
    }
}
