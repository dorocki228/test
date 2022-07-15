package services.totalonline;

import static com.google.common.flogger.LazyArgs.lazy;

import com.google.common.flogger.FluentLogger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.TreeSet;
import l2s.commons.dbutils.DbUtils;
import l2s.gameserver.Config;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.listener.script.OnInitScriptListener;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.tables.FakePlayersTable;

/**
 * Online -> real + fake
 * by l2scripts
 */
public class totalonline implements OnInitScriptListener
{
	private static final FluentLogger _log = FluentLogger.forEnclosingClass();

	

	@Override
	public void onInit()
	{
		_log.atInfo().log( "%s: Loaded Service: Parse Online [%s", lazy(() -> getClass().getSimpleName()), (Config.ALLOW_ONLINE_PARSE ? "enabled]" : "disabled]") );

		if(Config.ALLOW_ONLINE_PARSE)
			ThreadPoolManager.getInstance().scheduleAtFixedRate(new updateOnline(), Config.FIRST_UPDATE * 60000, Config.DELAY_UPDATE * 60000);
	}

	private class updateOnline implements Runnable
	{
		public void run()
		{
			int members = getOnlineMembers();
			int offMembers = getOfflineMembers();
			Connection con = null;
			PreparedStatement statement = null;
			try
			{
				con = DatabaseFactory.getInstance().getConnection();
				statement = con.prepareStatement("UPDATE online SET totalOnline = ?, totalOffline = ? WHERE `index` = 0");
				statement.setInt(1, members);
				statement.setInt(2, offMembers);
				statement.execute();
				DbUtils.closeQuietly(statement);		
		}
		
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}			
		}
	}
	//for future possibility of parsing names of players method is taking also name to array for init
	private int getOnlineMembers()
	{
		return GameObjectsStorage.getPlayers(true, true).size() + FakePlayersTable.getActiveFakePlayersCount();	
	}
	private int getOfflineMembers()
	{
		return GameObjectsStorage.getOfflinePlayers().size();	
	}
}