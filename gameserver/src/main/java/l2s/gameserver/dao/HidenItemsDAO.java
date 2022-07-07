package l2s.gameserver.dao;

import l2s.commons.dbutils.DbUtils;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.model.items.ItemInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

public class HidenItemsDAO
{
	private static final Logger _log;
	private static final ArrayList<Integer> _l;

	public static void LoadAllHiddenItems()
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
            con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT * FROM hidden_items");
			rset = statement.executeQuery();
            int hidden_obj = 0;
            while(rset.next())
			{
				hidden_obj = rset.getInt("obj_id");
				_l.add(hidden_obj);
			}
		}
		catch(Exception e)
		{
			_log.info("not working?");
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
			_log.info("Hidden items loaded size: " + _l.size());
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
			_log.error("Hidden Item:" + e, e);
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
		return item != null && _l.contains(item.getObjectId());
	}

	static
	{
		_log = LoggerFactory.getLogger(HidenItemsDAO.class);
		_l = new ArrayList<>();
	}
}
