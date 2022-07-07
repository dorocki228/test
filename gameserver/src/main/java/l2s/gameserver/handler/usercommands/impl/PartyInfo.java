package l2s.gameserver.handler.usercommands.impl;

import l2s.gameserver.handler.usercommands.IUserCommandHandler;
import l2s.gameserver.model.Party;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.SystemMessage;

public class PartyInfo implements IUserCommandHandler
{
	private static final int[] COMMAND_IDS = { 81 };

	@Override
	public boolean useUserCommand(int id, Player activeChar)
	{
		if(id != COMMAND_IDS[0])
			return false;
		Party playerParty = activeChar.getParty();
		if(!activeChar.isInParty())
			return false;
		Player partyLeader = playerParty.getPartyLeader();
		if(partyLeader == null)
			return false;
		int memberCount = playerParty.getMemberCount();
		int lootDistribution = playerParty.getLootDistribution();
		activeChar.sendPacket(SystemMsg.PARTY_INFORMATION);
		switch(lootDistribution)
		{
			case 0:
			{
				activeChar.sendPacket(SystemMsg.LOOTING_METHOD_FINDERS_KEEPERS);
				break;
			}
			case 3:
			{
				activeChar.sendPacket(SystemMsg.LOOTING_METHOD_BY_TURN);
				break;
			}
			case 4:
			{
				activeChar.sendPacket(SystemMsg.LOOTING_METHOD_BY_TURN_INCLUDING_SPOIL);
				break;
			}
			case 1:
			{
				activeChar.sendPacket(SystemMsg.LOOTING_METHOD_RANDOM);
				break;
			}
			case 2:
			{
				activeChar.sendPacket(SystemMsg.LOOTING_METHOD_RANDOM_INCLUDING_SPOIL);
				break;
			}
		}
		activeChar.sendPacket(new SystemMessage(1611).addString(partyLeader.getName()));
		activeChar.sendMessage(new CustomMessage("usercommandhandlers.PartyInfo.Members").addNumber(memberCount));
		activeChar.sendPacket(SystemMsg.LINE_500);
		return true;
	}

	@Override
	public int[] getUserCommandList()
	{
		return COMMAND_IDS;
	}
}
