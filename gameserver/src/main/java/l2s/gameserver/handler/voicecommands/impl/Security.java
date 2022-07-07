package l2s.gameserver.handler.voicecommands.impl;

import l2s.gameserver.Config;
import l2s.gameserver.GameServer;
import l2s.gameserver.handler.voicecommands.IVoicedCommandHandler;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.authcomm.gs2as.ChangeAllowedHwid;
import l2s.gameserver.network.authcomm.gs2as.ChangeAllowedIp;
import l2s.gameserver.network.l2.components.HtmlMessage;

public class Security implements IVoicedCommandHandler
{
	private final String[] _commandList;

	public Security()
	{
		_commandList = new String[] { "lock", "unlock", "lockIp", "lockHwid", "unlockIp", "unlockHwid" };
	}

	@Override
	public boolean useVoicedCommand(String command, Player player, String args)
	{
		if("lock".equalsIgnoreCase(command))
		{
			HtmlMessage html = new HtmlMessage(player.getObjectId());
			html.setFile("mods/lock/lock.htm");
			html.replace("%ip_block%", IpBlockStatus());
			html.replace("%hwid_block%", HwidBlockStatus());
			html.replace("%hwid_val%", HwidBlockBy());
			html.replace("%curIP%", player.getIP());
			player.sendPacket(html);
			return true;
		}
		if("lockIp".equalsIgnoreCase(command))
		{
			if(!Config.ALLOW_IP_LOCK)
				return true;
			GameServer.getInstance().getAuthServerCommunication().sendPacket(new ChangeAllowedIp(player.getAccountName(), player.getIP()));
			HtmlMessage html = new HtmlMessage(player.getObjectId());
			html.setFile("mods/lock/lock_ip.htm");
			html.replace("%curIP%", player.getIP());
			player.sendPacket(html);
			return true;
		}
		else if("lockHwid".equalsIgnoreCase(command))
		{
			if(!Config.ALLOW_HWID_LOCK)
				return true;
			ChangeAllowedHwid packet = new ChangeAllowedHwid(player.getAccountName(), player.getNetConnection().getHwidHolder());
			GameServer.getInstance().getAuthServerCommunication().sendPacket(packet);
			HtmlMessage html = new HtmlMessage(player.getObjectId());
			html.setFile("mods/lock/lock_hwid.htm");
			player.sendPacket(html);
			return true;
		}
		else
		{
			if("unlockIp".equalsIgnoreCase(command))
			{
				GameServer.getInstance().getAuthServerCommunication().sendPacket(new ChangeAllowedIp(player.getAccountName(), ""));
				HtmlMessage html = new HtmlMessage(player.getObjectId());
				html.setFile("mods/lock/unlock_ip.htm");
				html.replace("%curIP%", player.getIP());
				player.sendPacket(html);
				return true;
			}
			if("unlockHwid".equalsIgnoreCase(command))
			{
				ChangeAllowedHwid packet = new ChangeAllowedHwid(player.getAccountName(), "");
				GameServer.getInstance().getAuthServerCommunication().sendPacket(packet);
				HtmlMessage html = new HtmlMessage(player.getObjectId());
				html.setFile("mods/lock/unlock_hwid.htm");
				player.sendPacket(html);
				return true;
			}
			return true;
		}
	}

	private String IpBlockStatus()
	{
		if(Config.ALLOW_IP_LOCK)
			return "\u0420\u0430\u0437\u0440\u0435\u0448\u0435\u043d\u043e";
		return "\u0417\u0430\u043f\u0440\u0435\u0449\u0435\u043d\u043e";
	}

	private String HwidBlockStatus()
	{
		if(Config.ALLOW_HWID_LOCK)
			return "\u0420\u0430\u0437\u0440\u0435\u0448\u0435\u043d\u043e";
		return "\u0417\u0430\u043f\u0440\u0435\u0449\u0435\u043d\u043e";
	}

	private String HwidBlockBy()
	{
		String result = "(CPU/HDD)";
		switch(Config.HWID_LOCK_MASK)
		{
			case 2:
			{
				result = "(HDD)";
				break;
			}
			case 4:
			{
				result = "(BIOS)";
				break;
			}
			case 6:
			{
				result = "(BIOS/HDD)";
				break;
			}
			case 8:
			{
				result = "(CPU)";
				break;
			}
			case 10:
			{
				result = "(CPU/HDD)";
				break;
			}
			case 12:
			{
				result = "(CPU/BIOS)";
				break;
			}
			case 14:
			{
				result = "(CPU/HDD/BIOS)";
				break;
			}
			default:
			{
				result = "(unknown)";
				break;
			}
		}
		return result;
	}

	@Override
	public String[] getVoicedCommandList()
	{
		return _commandList;
	}
}
