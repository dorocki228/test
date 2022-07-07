package l2s.gameserver.taskmanager.tasks;

import l2s.gameserver.ThreadPoolManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ScheduledFuture;

public abstract class AutomaticTask implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(AutomaticTask.class);
    private ScheduledFuture<?> future;

    public abstract void doTask() throws Exception;

    public abstract long reCalcTime(boolean p0);

    public void init(boolean start) {
        stopTask();
        future = ThreadPoolManager.getInstance().schedule(this, reCalcTime(start) - System.currentTimeMillis());
    }

    @Override
    public void run() {
        try {
            doTask();
        } catch (Exception e) {
            LOGGER.error("Exception: AutomaticTask.run(): ", e);
        } finally {
            init(false);
        }
    }

    public void stopTask() {
        if(future != null)
            future.cancel(false);
        future = null;
    }
}
