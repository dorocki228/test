package l2s.gameserver.handler.usercommands.impl;

import l2s.gameserver.handler.usercommands.IUserCommandHandler;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.pledge.Alliance;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.model.pledge.ClanWar;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;

import java.util.ArrayList;
import java.util.List;

public class ClanWarsList implements IUserCommandHandler
{
	private static final int[] COMMAND_IDS;

	@Override
	public boolean useUserCommand(int id, Player activeChar)
	{
		if(id != COMMAND_IDS[0] && id != COMMAND_IDS[1] && id != COMMAND_IDS[2])
			return false;
		Clan clan = activeChar.getClan();
		if(clan == null)
		{
			activeChar.sendPacket(SystemMsg.NOT_JOINED_IN_ANY_CLAN);
			return false;
		}
		List<Clan> data = new ArrayList<>();
		if(id == 88)
		{
			activeChar.sendPacket(SystemMsg.CLANS_YOUVE_DECLARED_WAR_ON);
			for(ClanWar war : clan.getClanWars())
				if(war.getPeriod() == ClanWar.ClanWarPeriod.PREPARATION)
				{
					if(!war.isAttacker(clan))
						continue;
					Clan opposingClan = war.getOpposingClan();
					if(opposingClan == null)
						continue;
					data.add(opposingClan);
				}
		}
		else if(id == 89)
		{
			activeChar.sendPacket(SystemMsg.CLANS_THAT_HAVE_DECLARED_WAR_ON_YOU);
			for(ClanWar war : clan.getClanWars())
				if(war.getPeriod() == ClanWar.ClanWarPeriod.PREPARATION)
				{
					if(!war.isOpposing(clan))
						continue;
					Clan attackerClan = war.getAttackerClan();
					if(attackerClan == null)
						continue;
					data.add(attackerClan);
				}
		}
		else
		{
			activeChar.sendPacket(SystemMsg.WAR_LIST);
			for(ClanWar war : clan.getClanWars())
			{
				if(war.getPeriod() != ClanWar.ClanWarPeriod.MUTUAL)
					continue;
				Clan opposingClan = war.getOpposingClan();
				if(opposingClan == null)
					continue;
				data.add(opposingClan);
			}
		}
		for(Clan c : data)
		{
			String clanName = c.getName();
			Alliance alliance = c.getAlliance();
			SystemMessagePacket sm;
			if(alliance != null)
				sm = new SystemMessagePacket(SystemMsg._S1_S2_ALLIANCE).addString(clanName).addString(alliance.getAllyName());
			else
				sm = new SystemMessagePacket(SystemMsg._S1_NO_ALLIANCE_EXISTS).addString(clanName);
			activeChar.sendPacket(sm);
		}
		activeChar.sendPacket(SystemMsg.LINE_490);
		return true;
	}

	@Override
	public int[] getUserCommandList()
	{
		return COMMAND_IDS;
	}

	static
	{
		COMMAND_IDS = new int[] { 88, 89, 90 };
	}
}
