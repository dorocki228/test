package l2s.gameserver.handler.voicecommands;

import l2s.gameserver.model.Player;

public interface IVoicedCommandHandler
{
	boolean useVoicedCommand(String command, Player player, String args);

	String[] getVoicedCommandList();
}
