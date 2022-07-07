package handler.admincommands;

import l2s.gameserver.instancemanager.FenceBuilderManager;
import l2s.gameserver.model.Player;

public class AdminFence extends ScriptAdminCommand
{
	private enum Commands
	{
		admin_fence,
		admin_delallspawned,
		admin_dellastspawned,
		admin_fbuilder,
		admin_fb,
		admin_fbx
	}

	@Override
	public boolean useAdminCommand(Enum<?> comm, String[] wordList, String fullString, Player activeChar)
	{
		Commands command = (Commands) comm;

		if(!activeChar.getPlayerAccess().IsEventGm)
		{
			return false;
		}

		switch(command)
		{
			case admin_fence:
				if(wordList.length < 5)
				{
					activeChar.sendMessage("Not all arguments was set");
					activeChar.sendMessage("USAGE: //fence type width height size");
					return false;
				}
				FenceBuilderManager.getInstance().spawnFence(activeChar, Integer.parseInt(wordList[1]), Integer.parseInt(wordList[2]), Integer.parseInt(wordList[3]), Integer.parseInt(wordList[4]));
				break;
			case admin_delallspawned:
				FenceBuilderManager.getInstance().deleteAllFences(activeChar);
				break;
			case admin_dellastspawned:
				FenceBuilderManager.getInstance().deleteLastFence(activeChar);
				break;
			case admin_fbuilder:
			case admin_fb:
				FenceBuilderManager.getInstance().fenceMenu(activeChar);
				break;
			case admin_fbx:
				if(wordList.length < 2)
				{
					activeChar.sendMessage("Not all arguments was set");
					activeChar.sendMessage("USAGE: //fbx type");
					return false;
				}
				FenceBuilderManager.getInstance().changeFenceType(activeChar, Integer.parseInt(wordList[1]));
				break;
		}
		return true;
	}

	@Override
	public Enum<?>[] getAdminCommandEnum()
	{
		return Commands.values();
	}
}
