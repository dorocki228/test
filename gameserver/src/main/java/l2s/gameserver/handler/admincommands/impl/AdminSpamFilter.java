package l2s.gameserver.handler.admincommands.impl;

import l2s.gameserver.config.templates.SpamRule;
import l2s.gameserver.config.xml.holder.SpamFilterConfigHolder;
import l2s.gameserver.handler.admincommands.IAdminCommandHandler;
import l2s.gameserver.model.GameObject;
import l2s.gameserver.model.Player;

import java.util.regex.Matcher;

public class AdminSpamFilter implements IAdminCommandHandler
{
	@Override
	public boolean useAdminCommand(Enum<?> comm, String[] wordList, String fullString, Player activeChar)
	{
		Commands command = (Commands) comm;
		//		if(!activeChar.getPlayerAccess().Menu)
		//			return false;
		switch(command)
		{
			case admin_isspam:
			{
				for(SpamRule rule : SpamFilterConfigHolder.getInstance().getRules())
				{
					Matcher matcher = rule.getPattern().matcher(fullString);

					if(matcher.find())
						activeChar.sendMessage(rule.getPattern() + " " + rule.getPenalty());
				}

				return false;
			}
			case admin_setspamer:
			{
				GameObject target = activeChar.getTarget();
				if(target != null && target.isPlayer())
				{
					Player pc = target.getPlayer();
					pc.setSpamer(!pc.isSpamer(), true);
					activeChar.sendMessage(pc.getName() + " isSpamer=" + pc.isSpamer());
				}
				return false;
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
		admin_isspam,
		admin_setspamer
    }
}
