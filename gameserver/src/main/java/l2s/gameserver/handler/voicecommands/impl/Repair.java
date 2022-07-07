package l2s.gameserver.handler.voicecommands.impl;

import l2s.commons.dao.JdbcEntityState;
import l2s.commons.dbutils.DbUtils;
import l2s.gameserver.Config;
import l2s.gameserver.dao.CharacterVariablesDAO;
import l2s.gameserver.dao.ItemsDAO;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.handler.voicecommands.IVoicedCommandHandler;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.components.CustomMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.Map;

public class Repair implements IVoicedCommandHandler
{
	private static final Logger _log;
	private final String[] _commandList;

	public Repair()
	{
		_commandList = new String[] { "repair" };
	}

	@Override
	public String[] getVoicedCommandList()
	{
		return _commandList;
	}

	@Override
	public boolean useVoicedCommand(String command, Player player, String args)
	{
		if(!Config.ALLOW_VOICED_COMMANDS)
			return false;
		if(!args.isEmpty())
		{
			if(player.getName().equalsIgnoreCase(args))
			{
				player.sendMessage(new CustomMessage("voicedcommandhandlers.Repair.YouCantRepairYourself"));
				return false;
			}
			int objId = 0;
			for(Map.Entry<Integer, String> e : player.getAccountChars().entrySet())
				if(e.getValue().equalsIgnoreCase(args))
				{
					objId = e.getKey();
					break;
				}
			if(objId == 0)
			{
				player.sendMessage(new CustomMessage("voicedcommandhandlers.Repair.YouCanRepairOnlyOnSameAccount"));
				return false;
			}
			if(GameObjectsStorage.getPlayer(objId) != null)
			{
				player.sendMessage(new CustomMessage("voicedcommandhandlers.Repair.CharIsOnline"));
				return false;
			}
			Connection con = null;
			PreparedStatement statement = null;
			ResultSet rs = null;
			try
			{
				con = DatabaseFactory.getInstance().getConnection();
				statement = con.prepareStatement("SELECT karma FROM characters WHERE obj_Id=?");
				statement.setInt(1, objId);
				statement.execute();
				rs = statement.getResultSet();
                rs.next();
                int karma = rs.getInt("karma");
                DbUtils.close(statement, rs);
				if(karma > 0)
				{
					statement = con.prepareStatement("UPDATE characters SET x=17144, y=170156, z=-3502 WHERE obj_Id=?");
					statement.setInt(1, objId);
					statement.execute();
					DbUtils.close(statement);
				}
				else
				{
					statement = con.prepareStatement("UPDATE characters SET x=0, y=0, z=0 WHERE obj_Id=?");
					statement.setInt(1, objId);
					statement.execute();
					DbUtils.close(statement);
					Collection<ItemInstance> items = ItemsDAO.getInstance().getItemsByOwnerIdAndLoc(objId, ItemInstance.ItemLocation.PAPERDOLL);
					for(ItemInstance item : items)
					{
						item.setEquipped(false);
						item.setLocData(0);
						item.setLocation(ItemInstance.ItemLocation.INVENTORY);
						item.setJdbcState(JdbcEntityState.UPDATED);
						item.update();
					}
				}
				CharacterVariablesDAO.getInstance().delete(objId, "reflection");
				player.sendMessage(new CustomMessage("voicedcommandhandlers.Repair.RepairDone"));
				return true;
			}
			catch(Exception e2)
			{
				_log.error("", e2);
				return false;
			}
			finally
			{
				DbUtils.closeQuietly(con, statement, rs);
			}
		}
		player.sendMessage(".repair <name>");
		return false;
	}

	static
	{
		_log = LoggerFactory.getLogger(Repair.class);
	}
}
