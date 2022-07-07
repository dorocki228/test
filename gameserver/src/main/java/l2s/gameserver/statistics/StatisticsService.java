package l2s.gameserver.statistics;

import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.util.concurrent.AbstractScheduledService;
import l2s.commons.time.cron.SchedulingPattern;
import l2s.gameserver.Config;
import l2s.gameserver.GameServer;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.network.authcomm.gs2as.AccountStatisticsRequest;
import l2s.gameserver.network.authcomm.vertx.AuthServerCommunication;
import l2s.gameserver.taskmanager.DelayedItemsManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jooq.lambda.Seq;
import org.jooq.lambda.tuple.Tuple2;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author Java-man
 * @since 29.12.2018
 */
public class StatisticsService extends AbstractScheduledService {
    private static final Logger LOGGER = LogManager.getLogger(StatisticsService.class);

    private static final StatisticsService INSTANCE = new StatisticsService();

    private final StatisticsDAO statisticsDAO = new StatisticsDAO();
    private final AuthServerCommunication authServerCommunication = GameServer.getInstance().getAuthServerCommunication();
    private final DelayedItemsManager delayedItemsManager = DelayedItemsManager.getInstance();

    private final SchedulingPattern schedulingPattern = Config.GVE.statisticsSchedule();

    private final Multimap<LocalDateTime, AccountStatistics> buffer = Multimaps.newListMultimap(
            new ConcurrentHashMap<>(), CopyOnWriteArrayList::new);

    public static StatisticsService getInstance() {
        return INSTANCE;
    }

    private StatisticsService() {
        LOGGER.info("scheduled at {}", schedulingPattern.description());
    }

    @Override
    protected void runOneIteration() throws Exception {
        ZonedDateTime now = ZonedDateTime.now();

        buffer.keySet().forEach(this::saveStatistics);

        authServerCommunication.sendPacket(new AccountStatisticsRequest(now.toLocalDateTime()));

        LOGGER.info("Statistics requested at {}.", now);
    }

    @Override
    protected Scheduler scheduler() {
        ZonedDateTime now = ZonedDateTime.now();
        Duration initialDelay = schedulingPattern.timeToNextExecution(now)
                .orElseThrow(() -> new IllegalArgumentException("Can't find next execution time."));
        ZonedDateTime start = now.plus(initialDelay);
        var next = schedulingPattern.next(start)
                .orElseThrow(() -> new IllegalArgumentException("Can't find next execution time."));;
        var period = Duration.between(start, next);
        return Scheduler.newFixedRateSchedule(initialDelay, period);
    }

    @Override
    protected ScheduledExecutorService executor() {
        return ThreadPoolManager.getInstance().getScheduledThreadPoolExecutor();
    }

    public void handleResponse(LocalDateTime dateTime, Collection<AccountStatistics> accountStatistics) {
        buffer.putAll(dateTime, accountStatistics);

        //buffer.keySet().forEach(this::saveStatistics);
    }

    public void saveStatistics(LocalDateTime next) {
        var temp = buffer.removeAll(next);

        if (temp.isEmpty()) {
            LOGGER.info("No statistics to save.");
            return;
        }

        LOGGER.info("Received statistics save request.");

        List<DelayedItemsStatistics> delayedItemsStatistics = delayedItemsManager.getStatistics();

        var statsBySource = Seq.seq(temp)
                .leftOuterJoin(Seq.seq(delayedItemsStatistics), (a, b) -> a.getLogin().equalsIgnoreCase(b.getLogin()))
                .collect(Collectors.groupingBy(t -> t.v1().getSource()));

        var result = statsBySource.entrySet().stream()
                .map(entry -> extractStatistics(next, entry.getKey(), entry.getValue()))
                .collect(Collectors.toUnmodifiableList());

        statisticsDAO.insert(result);

        var count = result.stream().mapToLong(Statistics::getAccountsTotalCount).sum();
        LOGGER.info("statistics saved {} account statistics for {} sources.", count, result.size());
    }

    private Statistics extractStatistics(LocalDateTime dateTime, String source,
                                         List<Tuple2<AccountStatistics, DelayedItemsStatistics>> statistics) {
        double accountsTotalCount = statistics.size();
        double accountsLoggedInAtLeastOneTimeCount = statistics.stream()
                .filter(temp -> temp.v1.getLoggedInCount() >= 1).count();
        double accountsLoggedInAtLeastThreeTimesCount = statistics.stream()
                .filter(temp -> temp.v1.getLoggedInCount() >= 3).count();

        List<DelayedItemsStatistics> delayedItemsStatisticsBySource = statistics.stream()
                .map(temp -> temp.v2)
                .filter(Objects::nonNull)
                .filter(temp -> temp.getDonateAmount() > 0.0)
                .collect(Collectors.toUnmodifiableList());

        double donatorMailCount = delayedItemsStatisticsBySource.size();

        double donateAmount = delayedItemsStatisticsBySource.stream()
                .mapToDouble(DelayedItemsStatistics::getDonateAmount).sum();

        double registeredToLoggedIn = accountsLoggedInAtLeastOneTimeCount / accountsTotalCount * 100;
        double activeToRegistered = accountsLoggedInAtLeastThreeTimesCount / accountsTotalCount * 100;
        double activeToLoggedIn = accountsLoggedInAtLeastThreeTimesCount / accountsLoggedInAtLeastOneTimeCount * 100;

        double donateOnRegistered = donateAmount / accountsTotalCount;
        double donateOnActive = donateAmount / accountsLoggedInAtLeastThreeTimesCount;

        double donatorToActive = donatorMailCount / accountsTotalCount * 100;

        double averageDonate = donateAmount / donatorMailCount;

        registeredToLoggedIn = checkValue(registeredToLoggedIn);
        activeToRegistered = checkValue(activeToRegistered);
        activeToLoggedIn = checkValue(activeToLoggedIn);
        donateOnRegistered = checkValue(donateOnRegistered);
        donateOnActive = checkValue(donateOnActive);
        donatorToActive = checkValue(donatorToActive);
        averageDonate = checkValue(averageDonate);

        return new Statistics(dateTime, source, accountsTotalCount, accountsLoggedInAtLeastOneTimeCount,
                accountsLoggedInAtLeastThreeTimesCount, donatorMailCount, donateAmount,
                registeredToLoggedIn, activeToRegistered, activeToLoggedIn,
                donateOnRegistered, donateOnActive, donatorToActive, averageDonate);
    }

    private double checkValue(double value) {
        return Double.isFinite(value) ? value : 0.0;
    }
}
