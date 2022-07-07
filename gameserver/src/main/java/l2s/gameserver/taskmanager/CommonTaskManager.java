package l2s.gameserver.taskmanager;

import l2s.commons.threading.SteppingRunnableQueueManager;
import l2s.gameserver.ThreadPoolManager;

public final class CommonTaskManager extends SteppingRunnableQueueManager {
    private static volatile CommonTaskManager instance;
    private static final long tick = 500L;

    public static CommonTaskManager getInstance() {
        CommonTaskManager local = instance;
        if (local == null) {
            synchronized (CommonTaskManager.class) {
                local = instance;
                if (local == null) {
                    local = instance = new CommonTaskManager();
                }
            }
        }
        return local;
    }

    private CommonTaskManager() {
        super(tick);
        ThreadPoolManager.getInstance().scheduleAtFixedRate(this, tick, tick);
        ThreadPoolManager.getInstance().scheduleAtFixedRate(this::purge, 60000L, 60000L);
    }
}
