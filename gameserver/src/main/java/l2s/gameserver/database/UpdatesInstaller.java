package l2s.gameserver.database;

import com.google.common.flogger.FluentLogger;
import l2s.commons.dbutils.DbUtils;
import l2s.commons.dbutils.ScriptRunner;
import l2s.gameserver.Config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Bonux
**/
public class UpdatesInstaller
{
	private static final FluentLogger _log = FluentLogger.forEnclosingClass();

	

	private static class UpdateFilenameFilter implements FilenameFilter
	{
		@Override
		public boolean accept(File dir, String name)
		{
			return name.matches(".+\\.sql");
		}
	}

	public static void checkAndInstall()
	{
		if(!Config.DATABASE_AUTOUPDATE)
		{
			_log.atInfo().log( "Disabled." );
			return;
		}

		List<String> installedUpdates = new ArrayList<String>();

		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT file_name FROM installed_updates");
			rset = statement.executeQuery();
			while(rset.next())
				installedUpdates.add(rset.getString("file_name").trim().toLowerCase());
		}
		catch(Exception e)
		{
			_log.atSevere().withStackTrace(com.google.common.flogger.StackSize.FULL).withCause(e).log( "Error while restore installed updates from database: %s", e );
			return;
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}

		File updatesDir = new File(Config.DATAPACK_ROOT, "sql/updates/");
		if(updatesDir == null || !updatesDir.isDirectory())
		{
			_log.atWarning().log( "Cannot find %s/sql/updates/ directory!", Config.DATAPACK_ROOT.getPath() );
			return;
		}

		File[] updateFiles = updatesDir.listFiles(new UpdateFilenameFilter());
		Arrays.sort(updateFiles);

		for(File f : updateFiles)
		{
			final String name = f.getName().trim().toLowerCase().replaceAll("^\\s*(.*?)\\s*\\.sql$", "$1");

			if(installedUpdates.stream().anyMatch(str -> str.equalsIgnoreCase(name)))
				continue;

			try
			{
				con = DatabaseFactory.getInstance().getConnection();
				ScriptRunner runner = new ScriptRunner(con, false, true);
				runner.runScript(new BufferedReader(new FileReader(f)));

				statement = con.prepareStatement("REPLACE INTO installed_updates (file_name) VALUES(?)");
				statement.setString(1, name);
				statement.execute();

				DbUtils.closeQuietly(statement);
			}
			catch(Exception e)
			{
				_log.atSevere().withStackTrace(com.google.common.flogger.StackSize.FULL).withCause(e).log( "Error while install database update [%s]: %s", name, e );
				return;
			}
			finally
			{
				DbUtils.closeQuietly(con);
				_log.atInfo().log( "Installed update: %s", name );
			}
		}
	}
}