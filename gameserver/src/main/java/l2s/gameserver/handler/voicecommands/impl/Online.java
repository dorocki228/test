package l2s.gameserver.handler.voicecommands.impl;

import l2s.gameserver.Config;
import l2s.gameserver.handler.voicecommands.IVoicedCommandHandler;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;

public class Online implements IVoicedCommandHandler
{
	private final String[] _commandList;

	public Online()
	{
		_commandList = new String[] { "online" };
	}

	@Override
	public String[] getVoicedCommandList()
	{
		return _commandList;
	}

	@Override
	public boolean useVoicedCommand(String command, Player activeChar, String args)
	{
		if(!Config.ALLOW_VOICED_COMMANDS || !Config.ALLOW_TOTAL_ONLINE)
			return false;
		if("online".equals(command))
		{
			int i = 0;
			int j = 0;
			for(Player player : GameObjectsStorage.getPlayers())
			{
				++i;
				if(player.isInOfflineMode())
					++j;
			}
			if(activeChar.isLangRus())
			{
				activeChar.sendMessage("\u041d\u0430 \u0441\u0435\u0440\u0432\u0435\u0440\u0435 \u0438\u0433\u0440\u0430\u044e\u0442 " + i + " \u0438\u0433\u0440\u043e\u043a\u043e\u0432.");
				activeChar.sendMessage("\u0418\u0437 \u043d\u0438\u0445 " + j + " \u043d\u0430\u0445\u043e\u0434\u044f\u0442\u0441\u044f \u0432 \u043e\u0444\u0444\u043b\u0430\u0439\u043d \u0442\u043e\u0440\u0433\u0435.");
			}
			else
			{
				activeChar.sendMessage("Right now there are " + i + " players online.");
				activeChar.sendMessage("From them " + j + " are in offline trade mode.");
			}
			return true;
		}
		return false;
	}
}
