package l2s.gameserver.handler.voicecommands.impl;

import l2s.gameserver.Config;
import l2s.gameserver.GameServer;
import l2s.gameserver.data.htm.HtmCache;
import l2s.gameserver.handler.voicecommands.IVoicedCommandHandler;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.authcomm.gs2as.ChangePassword;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.utils.Functions;
import l2s.gameserver.utils.Util;

public class Password implements IVoicedCommandHandler
{
	private final String[] _commandList;

	public Password()
	{
		_commandList = new String[] { "password" };
	}

	@Override
	public boolean useVoicedCommand(String command, Player player, String args)
	{
		if("password".equals(command) && (args == null || args.isEmpty()))
		{
			String dialog = "";
			if(Config.SERVICES_CHANGE_PASSWORD)
				dialog = HtmCache.getInstance().getHtml("command/password.htm", player);
			else
				dialog = HtmCache.getInstance().getHtml("command/nopassword.htm", player);
			Functions.show(dialog, player);
			return true;
		}
		String[] parts = args.split(" ");
		if(parts.length != 3)
		{
			Functions.show(new CustomMessage("scripts.commands.user.password.IncorrectValues"), player);
			return false;
		}
		if(!parts[1].equals(parts[2]))
		{
			Functions.show(new CustomMessage("scripts.commands.user.password.IncorrectConfirmation"), player);
			return false;
		}
		if(parts[1].equals(parts[0]))
		{
			Functions.show(new CustomMessage("scripts.commands.user.password.NewPassIsOldPass"), player);
			return false;
		}
		if(parts[1].length() < 5 || parts[1].length() > 20)
		{
			Functions.show(new CustomMessage("scripts.commands.user.password.IncorrectSize"), player);
			return false;
		}
		if(!Util.isMatchingRegexp(parts[1], Config.APASSWD_TEMPLATE))
		{
			Functions.show(new CustomMessage("scripts.commands.user.password.IncorrectInput"), player);
			return false;
		}
		GameServer.getInstance().getAuthServerCommunication().sendPacket(new ChangePassword(player.getAccountName(), parts[0], parts[1], "0"));
		return true;
	}

	@Override
	public String[] getVoicedCommandList()
	{
		return _commandList;
	}
}
