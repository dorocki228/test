package l2s.gameserver.handler.voicecommands.impl;

import l2s.gameserver.Config;
import l2s.gameserver.data.htm.HtmCache;
import l2s.gameserver.handler.voicecommands.IVoicedCommandHandler;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.base.Experience;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.network.l2.s2c.RadarControlPacket;
import l2s.gameserver.utils.Functions;

public class Help implements IVoicedCommandHandler
{
	private final String[] _commandList;

	public Help()
	{
		_commandList = new String[] { "help", "exp", "whereis" };
	}

	@Override
	public boolean useVoicedCommand(String command, Player player, String args)
	{
		if(!Config.ALLOW_VOICED_COMMANDS)
			return false;
		command = command.intern();
		if("help".equalsIgnoreCase(command))
			return help(command, player, args);
		if("whereis".equalsIgnoreCase(command))
			return whereis(command, player, args);
		return "exp".equalsIgnoreCase(command) && exp(command, player, args);
	}

	private boolean exp(String command, Player activeChar, String args)
	{
		if(activeChar.getLevel() >= (activeChar.isBaseClassActive() ? Experience.getMaxLevel() : Experience.getMaxSubLevel()))
			activeChar.sendMessage(new CustomMessage("voicedcommandhandlers.Help.MaxLevel"));
		else
		{
			long exp = Experience.LEVEL[activeChar.getLevel() + 1] - activeChar.getExp();
			activeChar.sendMessage(new CustomMessage("voicedcommandhandlers.Help.ExpLeft").addNumber(exp));
		}
		return true;
	}

	private boolean whereis(String command, Player activeChar, String args)
	{
		Player friend = GameObjectsStorage.getPlayer(args);
		if(friend == null)
			return false;
		if(friend.getParty() == activeChar.getParty() || friend.getClan() == activeChar.getClan())
		{
			RadarControlPacket rc = new RadarControlPacket(0, 1, friend.getLoc());
			activeChar.sendPacket(rc);
			return true;
		}
		return false;
	}

	private boolean help(String command, Player activeChar, String args)
	{
		String dialog = HtmCache.getInstance().getHtml("command/help.htm", activeChar);
		Functions.show(dialog, activeChar);
		return true;
	}

	@Override
	public String[] getVoicedCommandList()
	{
		return _commandList;
	}
}
