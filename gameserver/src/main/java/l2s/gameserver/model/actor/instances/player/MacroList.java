package l2s.gameserver.model.actor.instances.player;

import l2s.commons.dbutils.DbUtils;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.s2c.MacroListPacket;
import l2s.gameserver.utils.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

public class MacroList
{
	private static final Logger _log;
	private final Player _owner;
	private final Map<Integer, Macro> _macroses;
	private int _macroId;

	public MacroList(Player player)
	{
		_macroses = new HashMap<>();
		_owner = player;
		_macroId = 1000;
	}

	public Macro[] getAllMacroses()
	{
		return _macroses.values().toArray(new Macro[0]);
	}

	public int size()
	{
		return _macroses.size();
	}

	public Macro getMacro(int id)
	{
		return _macroses.get(id - 1);
	}

	public void registerMacro(Macro macro)
	{
		if(macro.id == 0)
		{
			macro.id = _macroId++;
			while(_macroses.get(macro.id) != null)
				macro.id = _macroId++;
			_macroses.put(macro.id, macro);
			registerMacroInDb(macro);
			_owner.sendPacket(new MacroListPacket(macro.id, MacroListPacket.Action.ADD, 1, macro));
		}
		else
		{
			Macro old = _macroses.put(macro.id, macro);
			if(old != null)
				deleteMacroFromDb(old);
			registerMacroInDb(macro);
			_owner.sendPacket(new MacroListPacket(macro.id, MacroListPacket.Action.UPDATE, 1, macro));
		}
	}

	public void deleteMacro(int id)
	{
		Macro toRemove = _macroses.get(id);
		if(toRemove != null)
			deleteMacroFromDb(toRemove);
		_macroses.remove(id);
		_owner.sendPacket(new MacroListPacket(id, MacroListPacket.Action.DELETE, 0, null));
	}

	public void sendMacroses()
	{
		int size = size();
		if(size == 0)
			_owner.sendPacket(new MacroListPacket(0, MacroListPacket.Action.ADD, 0, null));
		else
			for(Macro macro : _macroses.values())
				_owner.sendPacket(new MacroListPacket(0, MacroListPacket.Action.ADD, size, macro));
	}

	private void registerMacroInDb(Macro macro)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("REPLACE INTO character_macroses (char_obj_id,id,icon,name,descr,acronym,commands) values(?,?,?,?,?,?,?)");
			statement.setInt(1, _owner.getObjectId());
			statement.setInt(2, macro.id);
			statement.setInt(3, macro.icon);
			statement.setString(4, macro.name);
			statement.setString(5, macro.descr);
			statement.setString(6, macro.acronym);
			StringBuilder sb = new StringBuilder();
			for(Macro.L2MacroCmd cmd : macro.commands)
			{
				sb.append(cmd.type).append(',');
				sb.append(cmd.d1).append(',');
				sb.append(cmd.d2);
				if(cmd.cmd != null && !cmd.cmd.isEmpty())
					sb.append(',').append(cmd.cmd);
				sb.append(';');
			}
			statement.setString(7, sb.toString());
			statement.execute();
		}
		catch(Exception e)
		{
			_log.error("could not store macro: " + macro, e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	private void deleteMacroFromDb(Macro macro)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("DELETE FROM character_macroses WHERE char_obj_id=? AND id=?");
			statement.setInt(1, _owner.getObjectId());
			statement.setInt(2, macro.id);
			statement.execute();
		}
		catch(Exception e)
		{
			_log.error("could not delete macro:", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	public void restore()
	{
		_macroses.clear();
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT char_obj_id, id, icon, name, descr, acronym, commands FROM character_macroses WHERE char_obj_id=?");
			statement.setInt(1, _owner.getObjectId());
			rset = statement.executeQuery();
			while(rset.next())
			{
				int id = rset.getInt("id");
				int icon = rset.getInt("icon");
				String name = Strings.stripSlashes(rset.getString("name"));
				String descr = Strings.stripSlashes(rset.getString("descr"));
				String acronym = Strings.stripSlashes(rset.getString("acronym"));
				List<Macro.L2MacroCmd> commands = new ArrayList<>();
				StringTokenizer st1 = new StringTokenizer(rset.getString("commands"), ";");
				while(st1.hasMoreTokens())
				{
					StringTokenizer st2 = new StringTokenizer(st1.nextToken(), ",");
					int type = Integer.parseInt(st2.nextToken());
					int d1 = Integer.parseInt(st2.nextToken());
					int d2 = Integer.parseInt(st2.nextToken());
					String cmd = "";
					if(st2.hasMoreTokens())
						cmd = st2.nextToken();
					Macro.L2MacroCmd mcmd = new Macro.L2MacroCmd(commands.size(), type, d1, d2, cmd);
					commands.add(mcmd);
				}
				Macro m = new Macro(id, icon, name, descr, acronym, commands.toArray(new Macro.L2MacroCmd[0]));
				_macroses.put(m.id, m);
			}
		}
		catch(Exception e)
		{
			_log.error("could not restore shortcuts:", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
	}

	static
	{
		_log = LoggerFactory.getLogger(MacroList.class);
	}
}