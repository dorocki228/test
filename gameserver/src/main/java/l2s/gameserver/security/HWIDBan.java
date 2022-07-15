package l2s.gameserver.security;

import static com.google.common.flogger.LazyArgs.lazy;

import com.google.common.flogger.FluentLogger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import l2s.commons.dbutils.DbUtils;
import l2s.gameserver.database.DatabaseFactory;

public class HWIDBan
{
	private static final FluentLogger _log = FluentLogger.forEnclosingClass();

	

	private static HWIDBan _instance;

	private ArrayList<String> _banList = new ArrayList<String>();

	public static HWIDBan getInstance()
	{
		if(_instance == null)
			_instance = new HWIDBan();
		return _instance;
	}

	public void load()
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			String hwid = "";
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT * FROM ban_hwid");
			rset = statement.executeQuery();
			while(rset.next())
			{
				hwid = rset.getString("hwid");
				if(hwid != "")
					_banList.add(hwid);
			}	
		}	
		catch(Exception e)
		{
			_log.atInfo().log( "not loaded?" );
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
			_log.atInfo().log( "HWIDBan: Black list (Hwid) loaded size: %s", lazy(() -> _banList.size()) );
		}		
	}

	public void addToBlackList(String hwid)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("INSERT INTO ban_hwid (hwid) VALUES(?)");
			statement.setString(1, hwid);		
			statement.execute();
		}
		catch(Exception e)
		{
			//fuck the what don't care
		}
		finally
		{
			_banList.add(hwid);
			_log.atInfo().log( "HWIDBan: Adding hwid to black list(hwid) %s", hwid );		
			DbUtils.closeQuietly(con, statement);
		}	
	}

	public void deleteFromBlackList(String hwid)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("DELETE from ban_hwid WHERE hwid like ?");
			statement.setString(1, hwid);		
			statement.execute();
		}
		catch(Exception e)
		{
			//fuck the what don't care
		}
		finally
		{
			_banList.remove(hwid);
			_log.atInfo().log( "HWIDBan: Remove hwid from black list(hwid) %s", hwid );		
			DbUtils.closeQuietly(con, statement);
		}	
	}

	public ArrayList<String> getAllBannedHwid() 
	{
		return _banList;
	}	
}