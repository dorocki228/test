package l2s.gameserver.dao;

import com.google.common.flogger.FluentLogger;
import l2s.commons.dbutils.DbUtils;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.model.items.ItemInstance;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import static com.google.common.flogger.LazyArgs.lazy;
/**
 * @author Iqman
 * @date 19:23/11.01.2012
 */
public class HidenItemsDAO
{
	private static final FluentLogger _log = FluentLogger.forEnclosingClass();

	private static ArrayList<Integer> _l = new ArrayList<Integer>();
	
	public static void LoadAllHiddenItems()
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			int hidden_obj = 0;
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT * FROM hidden_items");
			rset = statement.executeQuery();
			while(rset.next())
			{
				hidden_obj = rset.getInt("obj_id");
				_l.add(hidden_obj);
			}	
		}	
		catch(Exception e)
		{
			_log.atInfo().log( "not working?" );
		}
		finally
		{
			
			DbUtils.closeQuietly(con, statement, rset);
			_log.atInfo().log( "Hidden items loaded size: %s", lazy(() -> _l.size()) );
		}		
	}
	
	public static void addHiddenItem(ItemInstance item)
	{
		if(_l.contains(item.getObjectId()))
			return;
		Connection con = null;
		PreparedStatement statement = null;			
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("INSERT INTO hidden_items (obj_id) VALUES(?)");
			statement.setInt(1, item.getObjectId());		
			statement.execute();
		}
		catch(Exception e)
		{
			_log.atSevere().withStackTrace(com.google.common.flogger.StackSize.FULL).withCause(e).log( "Hidden Item:%s", e );
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
			_l.add(item.getObjectId());
		}
	}
	
	public static ArrayList<Integer> getAllHiddenItems() 
	{
		return _l;
	}
	public static boolean isHidden(ItemInstance item)
	{
		if(item == null)
			return false;
		if(_l.contains(item.getObjectId()))
			return true;
		return false;	
	}
}
