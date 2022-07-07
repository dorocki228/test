package l2s.gameserver.handler.admincommands;

import l2s.gameserver.model.Player;

public interface IAdminCommandHandler
{
	boolean useAdminCommand(Enum<?> p0, String[] p1, String p2, Player p3);

	Enum<?>[] getAdminCommandEnum();
}
