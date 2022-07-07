package l2s.gameserver.handler.admincommands.impl;

import l2s.gameserver.handler.admincommands.IAdminCommandHandler;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.utils.ItemFunctions;

import java.util.ArrayList;
import java.util.List;

public class AdminGiveAll implements IAdminCommandHandler
{
	private static final List<String> _l;

	@Override
	public boolean useAdminCommand(Enum<?> comm, String[] wordList, String fullString, Player activeChar)
	{
		if(wordList.length >= 3)
		{
			int _id = 0;
			int _count = 0;
			try
			{
				_id = Integer.parseInt(wordList[1]);
				_count = Integer.parseInt(wordList[2]);
			}
			catch(NumberFormatException e)
			{
				activeChar.sendMessage("only numbers");
				return false;
			}
			for(Player player : GameObjectsStorage.getPlayers())
			{
				if(player == null)
					continue;
				if(!checkPlayersHWID(player))
					continue;
				ItemFunctions.addItem(player, _id, _count);
				player.sendMessage("You have been rewarded!");
			}
			_l.clear();
			return true;
		}
		activeChar.sendMessage("use: //giveall itemId count");
		return false;
	}

	private static boolean checkPlayersHWID(Player player)
	{
		if (player == null)
			return false;
		
		if (_l.contains(player.getHwidHolder().asString()))
			return false;
		
		_l.add(player.getHwidHolder().asString());
		
		return true;
	}

	@Override
	public Enum<?>[] getAdminCommandEnum()
	{
		return Commands.values();
	}

	static
	{
		_l = new ArrayList<>();
	}

	enum Commands
	{
		admin_giveall
    }
}
