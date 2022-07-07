package l2s.gameserver.handler.admincommands.impl;

import l2s.gameserver.handler.admincommands.IAdminCommandHandler;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.GameObject;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.components.SystemMsg;

public class AdminHeal implements IAdminCommandHandler
{
	@Override
	public boolean useAdminCommand(Enum<?> comm, String[] wordList, String fullString, Player activeChar)
	{
		Commands command = (Commands) comm;
		if(!activeChar.getPlayerAccess().Heal)
			return false;
		switch(command)
		{
			case admin_heal:
			{
				if(wordList.length == 1)
				{
                    handleRes(activeChar);
					break;
				}
                handleRes(activeChar, wordList[1]);
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

	private void handleRes(Player activeChar)
	{
        handleRes(activeChar, null);
	}

	private void handleRes(Player activeChar, String player)
	{
		GameObject obj = activeChar.getTarget();
		if(player != null)
		{
			Player plyr = GameObjectsStorage.getPlayer(player);
			if(plyr == null)
			{
				int radius = Math.max(Integer.parseInt(player), 100);
				for(Creature character : activeChar.getAroundCharacters(radius, 200))
				{
					character.setCurrentHpMp(character.getMaxHp(), character.getMaxMp());
					if(character.isPlayer())
						character.setCurrentCp(character.getMaxCp());
				}
				activeChar.sendMessage("Healed within " + radius + " unit radius.");
				return;
			}
			obj = plyr;
		}
		if(obj == null)
			obj = activeChar;
		if(obj instanceof Creature)
		{
			Creature target = (Creature) obj;
			target.setCurrentHpMp(target.getMaxHp(), target.getMaxMp());
			if(target.isPlayer())
				target.setCurrentCp(target.getMaxCp());
		}
		else
			activeChar.sendPacket(SystemMsg.INVALID_TARGET);
	}

	private enum Commands
	{
		admin_heal
    }
}
