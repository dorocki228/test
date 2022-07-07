package l2s.gameserver.handler.usercommands.impl;

import l2s.gameserver.handler.usercommands.IUserCommandHandler;
import l2s.gameserver.model.Player;

public class ResetName implements IUserCommandHandler
{
	private static final int[] COMMAND_IDS;

	@Override
	public boolean useUserCommand(int id, Player activeChar)
	{
		if(COMMAND_IDS[0] != id)
			return false;
		if(activeChar.getVar("oldtitle") != null)
		{
			activeChar.setTitle(activeChar.getVar("oldtitle"));
			activeChar.broadcastUserInfo(true);
			return true;
		}
		return false;
	}

	@Override
	public final int[] getUserCommandList()
	{
		return COMMAND_IDS;
	}

	static
	{
		COMMAND_IDS = new int[] { 117 };
	}
}
