package l2s.gameserver.statistics;

import l2s.gameserver.database.DatabaseFactory;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.PreparedBatch;

import java.util.List;

/**
 * @author Java-man
 * @since 29.12.2018
 */
public class StatisticsDAO
{
    private final Jdbi jdbi = DatabaseFactory.getInstance().getJdbi();

    private static final String INSERT_STATISTICS
            = "INSERT INTO statistics(dateTime, source, accountsTotalCount, accountsLoggedInAtLeastOneTimeCount," +
            "accountsLoggedInAtLeastThreeTimesCount, donatorMailCount, donateAmount," +
            "registeredToLoggedInRatio, activeToRegisteredRatio, activeToLoggedInRatio," +
            "donateOnRegisteredAmount, donateOnActiveAmount, donatorToActivePercentage, averageDonate)" +
            " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    public void insert(List<Statistics> statisticsList) {
        jdbi.useHandle(handle -> {
            PreparedBatch batch = handle.prepareBatch(INSERT_STATISTICS);
            statisticsList.forEach(statistics -> batch.add(statistics.getDateTime(), statistics.getSource(),
                    statistics.getAccountsTotalCount(), statistics.getAccountsLoggedInAtLeastOneTimeCount(),
                    statistics.getAccountsLoggedInAtLeastThreeTimesCount(), statistics.getDonatorMailCount(),
                    statistics.getDonateAmount(), statistics.getRegisteredToLoggedInRatio(),
                    statistics.getActiveToRegisteredRatio(), statistics.getActiveToLoggedInRatio(),
                    statistics.getDonateOnRegisteredAmount(), statistics.getDonateOnActiveAmount(),
                    statistics.getDonatorToActivePercentage(), statistics.getAverageDonate()));

            batch.execute();
        });
    }
}
