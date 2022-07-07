package l2s.gameserver.handler.admincommands.impl;

import l2s.gameserver.handler.admincommands.IAdminCommandHandler;
import l2s.gameserver.model.GameObject;
import l2s.gameserver.model.Player;

public class AdminRide implements IAdminCommandHandler
{
	@Override
	public boolean useAdminCommand(Enum<?> comm, String[] wordList, String fullString, Player activeChar)
	{
		Commands command = (Commands) comm;
		if(!activeChar.getPlayerAccess().Rider)
			return false;
		Player rideTarget = activeChar;
		if(command == Commands.admin_wr || command == Commands.admin_sr || command == Commands.admin_ur)
		{
			GameObject target = activeChar.getTarget();
			if(target == null || !target.isPlayer())
			{
				activeChar.sendMessage("Incorrect target!");
				return false;
			}
			rideTarget = (Player) target;
		}
		switch(command)
		{
			case admin_ride:
			{
				if(activeChar.isMounted() || activeChar.hasServitor())
				{
					activeChar.sendMessage("Already Have a Pet or Mounted.");
					return false;
				}
				if(wordList.length != 2)
				{
					activeChar.sendMessage("Incorrect id.");
					return false;
				}
				activeChar.setMount(0, Integer.parseInt(wordList[1]), activeChar.getLevel(), -1);
				break;
			}
			case admin_ride_wyvern:
			case admin_wr:
			{
				if(rideTarget.isMounted() || rideTarget.hasServitor())
				{
					activeChar.sendMessage("Already Have a Pet or Mounted.");
					return false;
				}
				rideTarget.setMount(0, 12621, rideTarget.getLevel(), -1);
				break;
			}
			case admin_ride_strider:
			case admin_sr:
			{
				if(rideTarget.isMounted() || rideTarget.hasServitor())
				{
					activeChar.sendMessage("Already Have a Pet or Mounted.");
					return false;
				}
				rideTarget.setMount(0, 12526, rideTarget.getLevel(), -1);
				break;
			}
			case admin_unride:
			case admin_ur:
			{
				if(rideTarget.isMounted())
				{
					rideTarget.setMount(null);
					break;
				}
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
		admin_ride,
		admin_ride_wyvern,
		admin_ride_strider,
		admin_unride,
		admin_wr,
		admin_sr,
		admin_ur
    }
}
