package l2s.authserver.database.dao;

import l2s.authserver.accounts.Account;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

/**
 * @author Java-man
 * @since 08.01.2019
 */
public interface AccountLogDAO
{
	@SqlUpdate("INSERT INTO account_log (time, login, ip) VALUES(:lastAccess, :login, :lastIP)")
	void insert(@BindBean Account account);
}