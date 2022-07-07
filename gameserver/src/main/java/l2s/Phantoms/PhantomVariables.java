package  l2s.Phantoms;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.commons.dbutils.DbUtils;
import  l2s.gameserver.database.DatabaseFactory;
import  l2s.gameserver.templates.StatsSet;

public class PhantomVariables
{
	private static final Logger _log = LoggerFactory.getLogger(PhantomVariables.class);

	private static StatsSet phantom_vars = null;

	private static StatsSet getVars()
	{
		if(phantom_vars == null)
		{
			phantom_vars = new StatsSet();
			LoadFromDB();
		}
		return phantom_vars;
	}

	public static void LoadFromDB()
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT * FROM phantom_variables");
			rs = statement.executeQuery();
			while(rs.next())
				phantom_vars.set(rs.getString("name"), rs.getString("value"));
		}
		catch(Exception e)
		{
			_log.error("", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rs);
		}
	}

	private static void SaveToDB(String name)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			String value = getVars().getString(name, "");
			if(value.isEmpty())
			{
				statement = con.prepareStatement("DELETE FROM phantom_variables WHERE name = ?");
				statement.setString(1, name);
				statement.execute();
			}
			else
			{
				statement = con.prepareStatement("REPLACE INTO phantom_variables (name, value) VALUES (?,?)");
				statement.setString(1, name);
				statement.setString(2, value);
				statement.execute();
			}
		}
		catch(Exception e)
		{
			_log.error("", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	public static boolean getBool(String name)
	{
		return getVars().getBool(name);
	}

	public static boolean getBool(String name, boolean defult)
	{
		return getVars().getBool(name, defult);
	}

	public static int getInt(String name)
	{
		return getVars().getInteger(name);
	}

	public static int getInt(String name, int defult)
	{
		return getVars().getInteger(name, defult);
	}

	public static long getLong(String name)
	{
		return getVars().getLong(name);
	}

	public static long getLong(String name, long defult)
	{
		return getVars().getLong(name, defult);
	}

	public static double getFloat(String name)
	{
		return getVars().getDouble(name);
	}

	public static double getFloat(String name, double defult)
	{
		return getVars().getDouble(name, defult);
	}

	public static String getString(String name)
	{
		return getVars().getString(name);
	}

	public static String getString(String name, String defult)
	{
		return getVars().getString(name, defult);
	}

	public static void set(String name, boolean value)
	{
		getVars().set(name, value);
		SaveToDB(name);
	}

	public static void set(String name, int value)
	{
		getVars().set(name, value);
		SaveToDB(name);
	}

	public static void set(String name, long value)
	{
		getVars().set(name, value);
		SaveToDB(name);
	}

	public static void set(String name, double value)
	{
		getVars().set(name, value);
		SaveToDB(name);
	}

	public static void set(String name, String value)
	{
		getVars().set(name, value);
		SaveToDB(name);
	}

	public static void unset(String name)
	{
		getVars().unset(name);
		SaveToDB(name);
	}
}