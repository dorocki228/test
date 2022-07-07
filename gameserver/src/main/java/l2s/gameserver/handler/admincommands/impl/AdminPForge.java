package l2s.gameserver.handler.admincommands.impl;

import l2s.gameserver.handler.admincommands.IAdminCommandHandler;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.components.HtmlMessage;
import l2s.gameserver.network.l2.s2c.AdminForgePacket;

public class AdminPForge implements IAdminCommandHandler
{
	@Override
	public boolean useAdminCommand(Enum<?> comm, String[] wordList, String fullString, Player activeChar)
	{
		Commands command = (Commands) comm;
		if(activeChar == null || !activeChar.getPlayerAccess().CanUseGMCommand)
			return false;
		switch(command)
		{
			case admin_forge:
			{
				showMainPage(activeChar);
				break;
			}
			case admin_forge2:
			{
				if(wordList.length == 2)
				{
					showPage2(activeChar, wordList[1]);
					break;
				}
				activeChar.sendMessage("Usage: //forge2 format");
				break;
			}
			case admin_forge3:
			{
				try
				{
					String values = wordList[1];
					boolean broadcast = false;
					if("broadcast".equals(values.toLowerCase()))
					{
						values = wordList[2];
						broadcast = true;
					}
					AdminForgePacket sp = new AdminForgePacket();
					byte[] bytes = values.getBytes();
					for(int i = 0; i < values.length(); ++i)
					{
						String val = "0x00";
						int paramId = (broadcast ? 3 : 2) + i;
						if(wordList.length > paramId)
							val = wordList[paramId];
						if("$objid".equals(val.toLowerCase()))
							val = String.valueOf(activeChar.getObjectId());
						else if("$tobjid".equals(val.toLowerCase()))
							val = String.valueOf(activeChar.getTarget().getObjectId());
						else if("$clanid".equals(val.toLowerCase()))
							val = String.valueOf(activeChar.getObjectId());
						else if("$allyid".equals(val.toLowerCase()))
							val = String.valueOf(activeChar.getAllyId());
						else if("$tclanid".equals(val.toLowerCase()))
							val = String.valueOf(activeChar.getTarget().getObjectId());
						else if("$tallyid".equals(val.toLowerCase()))
							val = String.valueOf(((Player) activeChar.getTarget()).getAllyId());
						else if("$x".equals(val.toLowerCase()))
							val = String.valueOf(activeChar.getX());
						else if("$y".equals(val.toLowerCase()))
							val = String.valueOf(activeChar.getY());
						else if("$z".equals(val.toLowerCase()))
							val = String.valueOf(activeChar.getZ());
						else if("$heading".equals(val.toLowerCase()))
							val = String.valueOf(activeChar.getHeading());
						else if("$tx".equals(val.toLowerCase()))
							val = String.valueOf(activeChar.getTarget().getX());
						else if("$ty".equals(val.toLowerCase()))
							val = String.valueOf(activeChar.getTarget().getY());
						else if("$tz".equals(val.toLowerCase()))
							val = String.valueOf(activeChar.getTarget().getZ());
						else if("$theading".equals(val.toLowerCase()))
							val = String.valueOf(activeChar.getTarget().getHeading());
						sp.addPart(bytes[i], val);
					}
					if(broadcast)
						activeChar.broadcastPacket(sp);
					else if(activeChar.getTarget() == null || !activeChar.getTarget().isPlayer())
						activeChar.sendPacket(sp);
					else
						((Player) activeChar.getTarget()).sendPacket(sp);
					showPage3(activeChar, values, fullString);
				}
				catch(Exception ex)
				{
					ex.printStackTrace();
				}
				break;
			}
		}
		return true;
	}

	private void showMainPage(Player activeChar)
	{
		AdminHelpPage.showHelpHtml(activeChar, "pforge_menu1.htm");
	}

	private void showPage2(Player activeChar, String format)
	{
		HtmlMessage adminReply = new HtmlMessage(5);
		adminReply.setFile("admin/pforge_menu2.htm");
		adminReply.replace("%format%", format);
		StringBuilder valueditors = new StringBuilder();
		for(int i = 0; i < format.length(); ++i)
			valueditors.append(format.charAt(i) + " : <edit var=\"val" + i + "\" width=100><br1>");
		adminReply.replace("%valueditors%", valueditors.toString());
		StringBuilder send = new StringBuilder();
		for(int j = 0; j < format.length(); ++j)
		{
			if(j != 0)
				send.append(" ");
			send.append("$val" + j);
		}
		adminReply.replace("%send%", send.toString());
		activeChar.sendPacket(adminReply);
	}

	private void showPage3(Player activeChar, String format, String command)
	{
		HtmlMessage adminReply = new HtmlMessage(5);
		adminReply.setFile("admin/pforge_menu3.htm");
		adminReply.replace("%format%", format);
		adminReply.replace("%command%", command);
		activeChar.sendPacket(adminReply);
	}

	@Override
	public Enum<?>[] getAdminCommandEnum()
	{
		return Commands.values();
	}

	private enum Commands
	{
		admin_forge,
		admin_forge2,
		admin_forge3
    }
}
