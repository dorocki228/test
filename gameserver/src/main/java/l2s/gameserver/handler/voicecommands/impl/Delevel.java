package l2s.gameserver.handler.voicecommands.impl;

import l2s.gameserver.Config;
import l2s.gameserver.handler.voicecommands.IVoicedCommandHandler;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.base.Experience;

public class Delevel implements IVoicedCommandHandler
{
	private final String[] _commandList;

	public Delevel()
	{
		_commandList = new String[] { "delevel" };
	}

	@Override
	public String[] getVoicedCommandList()
	{
		return _commandList;
	}

	@Override
	public boolean useVoicedCommand(String command, Player player, String args)
	{
		if(!Config.ALLOW_VOICED_COMMANDS || !Config.ALLOW_DELEVEL_COMMAND)
			return false;
		if("delevel".equals(command))
		{
			int _old_level = player.getLevel();
			if(_old_level == 1)
				return false;
			Long exp_add = Experience.LEVEL[_old_level - 1] - player.getExp();
			player.addExpAndSp(exp_add, 0L, true);
		}
		return false;
	}
}
