package l2s.gameserver.handler.voicecommands.impl;

import l2s.gameserver.Config;
import l2s.gameserver.GameServer;
import l2s.gameserver.handler.voicecommands.IVoicedCommandHandler;
import l2s.gameserver.model.Player;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ServerInfo implements IVoicedCommandHandler
{
	private final String[] _commandList;
	private static final DateFormat DATE_FORMAT;

	public ServerInfo()
	{
		_commandList = new String[] { "rev", "ver", "date", "time" };
	}

	@Override
	public String[] getVoicedCommandList()
	{
		return _commandList;
	}

	@Override
	public boolean useVoicedCommand(String command, Player player, String args)
	{
		if(!Config.ALLOW_VOICED_COMMANDS)
			return false;
		if("rev".equals(command) || "ver".equals(command))
		{
			player.sendMessage("Project Revision: L2s [21963]");
			player.sendMessage("Build Revision: " + GameServer.getInstance().getVersion().getRevisionNumber());
			player.sendMessage("Update: Classic 1.5");
			player.sendMessage("Build date: " + GameServer.getInstance().getVersion().getBuildDate());
		}
		else if("date".equals(command) || "time".equals(command))
		{
			player.sendMessage(DATE_FORMAT.format(new Date(System.currentTimeMillis())));
			return true;
		}
		return false;
	}

	static
	{
		DATE_FORMAT = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
	}
}
