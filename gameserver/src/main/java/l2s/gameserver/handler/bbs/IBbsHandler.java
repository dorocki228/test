package l2s.gameserver.handler.bbs;

import l2s.gameserver.model.Player;

public interface IBbsHandler
{
	String[] getBypassCommands();

	void onBypassCommand(Player player, String bypass);

	void onWriteCommand(Player player, String bypass, String arg1, String arg2, String arg3, String arg4, String arg5);
}