package l2s.authserver.service;

import l2s.authserver.accounts.AccountStatistics;
import l2s.authserver.database.dao.AccountsDAO;

import java.util.List;

/**
 * @author Java-man
 * @since 29.12.2018
 */
public class AccountsService
{
    private static final AccountsService INSTANCE = new AccountsService();

    private final AccountsDAO accountsDAO = new AccountsDAO();

    public static AccountsService getInstance()
    {
        return INSTANCE;
    }

    public List<AccountStatistics> getAccountStatistics() {
        return accountsDAO.getAccountStatistics();
    }
}
