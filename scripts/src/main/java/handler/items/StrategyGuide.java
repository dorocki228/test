package handler.items;

import l2s.gameserver.model.CommandChannel;
import l2s.gameserver.model.Party;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.c2s.RequestExMPCCAskJoin;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;

/**
 * @author KanuToIIIKa
 */

public class StrategyGuide extends SimpleItemHandler
{

	@Override
	protected boolean useItemImpl(Player activeChar, ItemInstance item, boolean ctrl)
	{
		if(activeChar == null)
			return false;
		if(activeChar.isOutOfControl())
		{
			activeChar.sendActionFailed();
			return false;
		}
		if(activeChar.isProcessingRequest())
		{
			activeChar.sendPacket(SystemMsg.WAITING_FOR_ANOTHER_REPLY);
			return false;
		}
		if(!activeChar.isInParty())
		{
			activeChar.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_AUTHORITY_TO_INVITE_SOMEONE_TO_THE_COMMAND_CHANNEL);
			return false;
		}

		Player target = activeChar.getTarget() != null ? activeChar.getTarget().getPlayer() : null;

		if(target == null)
		{
			activeChar.sendPacket(SystemMsg.THAT_PLAYER_IS_NOT_CURRENTLY_ONLINE);
			return false;
		}
		if(activeChar == target || !target.isInParty() || activeChar.getParty() == target.getParty() || !target.getParty().isLeader(target))
		{
			activeChar.sendPacket(SystemMsg.YOU_HAVE_INVITED_THE_WRONG_TARGET);
			return false;
		}

		if(target.getParty().isInCommandChannel())
		{
			activeChar.sendPacket(new SystemMessagePacket(SystemMsg.C1S_PARTY_IS_ALREADY_A_MEMBER_OF_THE_COMMAND_CHANNEL).addName(target));
			return false;
		}
		if(target.isBusy())
		{
			activeChar.sendPacket(new SystemMessagePacket(SystemMsg.C1_IS_ON_ANOTHER_TASK).addName(target));
			return false;
		}
		Party activeParty = activeChar.getParty();
		if(activeParty.isInCommandChannel())
		{
			if(activeParty.getCommandChannel().getChannelLeader() != activeChar)
			{
				activeChar.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_AUTHORITY_TO_INVITE_SOMEONE_TO_THE_COMMAND_CHANNEL);
				return false;
			}
			RequestExMPCCAskJoin.sendInvite(activeChar, target);
		}
		else if(CommandChannel.checkAuthority(activeChar))
			RequestExMPCCAskJoin.sendInvite(activeChar, target);

		return true;
	}

}
