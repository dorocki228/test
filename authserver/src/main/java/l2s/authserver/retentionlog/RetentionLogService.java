package l2s.authserver.retentionlog;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Java-man
 * @since 08.01.2019
 */
public class RetentionLogService
{
    private static final Logger LOGGER = LogManager.getLogger(RetentionLogService.class);

    private static final RetentionLogService INSTANCE = new RetentionLogService();

    public static RetentionLogService getInstance()
    {
        return INSTANCE;
    }

    private RetentionLogService() {
        //LOGGER.info("scheduled at {}", schedulingPattern.description());

        schedule();
    }

    private void schedule() {
        /*Optional<Duration> next = schedulingPattern.timeToNextExecution();
        long millis = next.map(Duration::toMillis)
                .orElseThrow(() -> new IllegalArgumentException("Can't find next execution time."));
        ThreadPoolManager.getInstance().schedule(this::requestStatistics, millis);*/
    }

    private void requestStatistics() {


        schedule();
    }
}
