package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.*;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.ExAskJoinMPCCPacket;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;

public class RequestExMPCCAskJoin extends L2GameClientPacket
{
	private String _name;

	@Override
	protected void readImpl()
	{
		_name = readS(16);
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;
		if(activeChar.isOutOfControl())
		{
			activeChar.sendActionFailed();
			return;
		}
		if(activeChar.isProcessingRequest())
		{
			activeChar.sendPacket(SystemMsg.WAITING_FOR_ANOTHER_REPLY);
			return;
		}
		if(!activeChar.isInParty())
		{
			activeChar.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_AUTHORITY_TO_INVITE_SOMEONE_TO_THE_COMMAND_CHANNEL);
			return;
		}
		Player target = GameObjectsStorage.getPlayer(_name);
		if(target == null)
		{
			activeChar.sendPacket(SystemMsg.THAT_PLAYER_IS_NOT_CURRENTLY_ONLINE);
			return;
		}
		if(activeChar == target || !target.isInParty() || activeChar.getParty() == target.getParty() || !target.getParty().isLeader(target))
		{
			activeChar.sendPacket(SystemMsg.YOU_HAVE_INVITED_THE_WRONG_TARGET);
			return;
		}

		if(target.getParty().isInCommandChannel())
		{
			activeChar.sendPacket(new SystemMessagePacket(SystemMsg.C1S_PARTY_IS_ALREADY_A_MEMBER_OF_THE_COMMAND_CHANNEL).addName(target));
			return;
		}
		if(target.isBusy())
		{
			activeChar.sendPacket(new SystemMessagePacket(SystemMsg.C1_IS_ON_ANOTHER_TASK).addName(target));
			return;
		}
		Party activeParty = activeChar.getParty();
		if(activeParty.isInCommandChannel())
		{
			if(activeParty.getCommandChannel().getChannelLeader() != activeChar)
			{
				activeChar.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_AUTHORITY_TO_INVITE_SOMEONE_TO_THE_COMMAND_CHANNEL);
				return;
			}
			sendInvite(activeChar, target);
		}
		else if(CommandChannel.checkAuthority(activeChar))
			sendInvite(activeChar, target);
	}

	public static void sendInvite(Player requestor, Player target)
	{
		new Request(Request.L2RequestType.CHANNEL, requestor, target).setTimeout(10000L);
		target.sendPacket(new ExAskJoinMPCCPacket(requestor.getName()));
		requestor.sendMessage("You invited " + target.getName() + " to your Command Channel.");
	}
}
