package l2s.gameserver.handler.usercommands;

import l2s.gameserver.model.Player;

public interface IUserCommandHandler
{
	boolean useUserCommand(int p0, Player p1);

	int[] getUserCommandList();
}
