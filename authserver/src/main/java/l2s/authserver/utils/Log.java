package l2s.authserver.utils;

import com.google.common.flogger.FluentLogger;
import l2s.authserver.Config;
import l2s.authserver.accounts.Account;
import l2s.authserver.database.DatabaseFactory;
import l2s.commons.dbutils.DbUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.text.SimpleDateFormat;

public class Log
{
	private static final FluentLogger _log = FluentLogger.forEnclosingClass();

	private static final SimpleDateFormat SIMPLE_FORMAT = new SimpleDateFormat("HH:mm dd.MM.yyyy");

	public static void LogAccount(Account account)
	{
		if(!Config.LOGIN_LOG)
			return;

		StringBuilder output = new StringBuilder();
		output.append("ACCOUNT[");
		output.append(account.getLogin());
		output.append("] IP[");
		output.append(account.getLastIP());
		output.append("] LAST_ACCESS_TIME[");
		output.append(SIMPLE_FORMAT.format(account.getLastAccess() * 1000L));
		output.append("]");
		_log.atInfo().log(output.toString());
			
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("INSERT INTO account_log (time, login, ip) VALUES(?,?,?)");
			statement.setInt(1, account.getLastAccess());
			statement.setString(2, account.getLogin());
			statement.setString(3, account.getLastIP());			
			statement.execute();
		}
		catch(Exception e)
		{
			_log.atSevere().withStackTrace(com.google.common.flogger.StackSize.FULL).withCause(e).log( "" );
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}
}
