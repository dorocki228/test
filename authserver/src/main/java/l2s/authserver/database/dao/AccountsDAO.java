package l2s.authserver.database.dao;

import l2s.authserver.accounts.AccountStatistics;
import l2s.authserver.database.DatabaseFactory;
import org.jdbi.v3.core.Jdbi;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Java-man
 * @since 29.12.2018
 */
public class AccountsDAO {
    private final Jdbi jdbi = DatabaseFactory.getInstance().getJdbi();

    private static final String SELECT_ACCOUNT_STATISTICS = "SELECT" +
            " accounts.login, accounts.email, accounts.source, count(account_log.time)" +
            " FROM accounts left JOIN account_log ON accounts.login = account_log.login GROUP BY accounts.login";

    public List<AccountStatistics> getAccountStatistics() {
        return jdbi.withHandle(handle ->
                handle.createQuery(SELECT_ACCOUNT_STATISTICS)
                        .reduceResultSet(new ArrayList<>(50000),
                                (list, resultSet, ctx) -> {
                                    String login = resultSet.getString(1);
                                    String email = resultSet.getString(2);
                                    String source = resultSet.getString(3);
                                    int loggedInCount = resultSet.getInt(4);
                                    list.add(new AccountStatistics(login, email, source, loggedInCount));

                                    return list;
                                }));
    }
}