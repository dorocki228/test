package l2s.gameserver.handler.admincommands.impl;

import l2s.gameserver.handler.admincommands.IAdminCommandHandler;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.s2c.CameraModePacket;
import l2s.gameserver.network.l2.s2c.SpecialCameraPacket;
import l2s.gameserver.punishment.PunishmentService;
import l2s.gameserver.punishment.PunishmentType;

import java.time.ZonedDateTime;

public class AdminCamera implements IAdminCommandHandler
{
	@Override
	public boolean useAdminCommand(Enum<?> comm, String[] wordList, String fullString, Player activeChar)
	{
		Commands command = (Commands) comm;
		if(!activeChar.getPlayerAccess().Menu)
			return false;
		switch(command)
		{
			case admin_freelook:
			{
				if(fullString.length() > 15)
				{
					fullString = fullString.substring(15);
					int mode = Integer.parseInt(fullString);
					if(mode == 1)
					{
						activeChar.setInvisible(true);
						activeChar.setInvul(true);
						var endDate = ZonedDateTime.now().plusYears(100);
						PunishmentService.INSTANCE.addPunishment(PunishmentType.CHAT, activeChar, endDate,
								activeChar.getName(), "freelook");
						activeChar.setFlying(true);
					}
					else
					{
						activeChar.setInvisible(false);
						activeChar.setInvul(false);
						activeChar.setFlying(false);
					}
					activeChar.sendPacket(new CameraModePacket(mode));
					break;
				}
				activeChar.sendMessage("Usage: //freelook 1 or //freelook 0");
				return false;
			}
			case admin_cinematic:
			{
				int id = Integer.parseInt(wordList[1]);
				int dist = Integer.parseInt(wordList[2]);
				int yaw = Integer.parseInt(wordList[3]);
				int pitch = Integer.parseInt(wordList[4]);
				int time = Integer.parseInt(wordList[5]);
				int duration = Integer.parseInt(wordList[6]);
				activeChar.sendPacket(new SpecialCameraPacket(id, dist, yaw, pitch, time, duration));
				break;
			}
		}
		return true;
	}

	@Override
	public Enum<?>[] getAdminCommandEnum()
	{
		return Commands.values();
	}

	private enum Commands
	{
		admin_freelook,
		admin_cinematic
    }
}
