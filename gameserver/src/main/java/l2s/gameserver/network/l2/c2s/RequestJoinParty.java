package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.Config;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Party;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Request;
import l2s.gameserver.model.entity.events.Event;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.network.l2.components.IBroadcastPacket;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.AskJoinPartyPacket;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;
import l2s.gameserver.service.PartyClassLimitService;

public class RequestJoinParty extends L2GameClientPacket
{
	private String _name;
	private int _itemDistribution;

	@Override
	protected void readImpl()
	{
		_name = readS(Config.CNAME_MAXLEN);
		_itemDistribution = readD();
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
		if(activeChar.isPartyBlocked())
		{
			activeChar.sendMessage("Error party zone");
			return;
		}
		Player target = GameObjectsStorage.getPlayer(_name);
		if(target == null)
			activeChar.sendPacket(SystemMsg.THAT_PLAYER_IS_NOT_ONLINE);
		else
		{
			if(target == activeChar)
			{
				activeChar.sendPacket(SystemMsg.THAT_IS_AN_INCORRECT_TARGET);
				activeChar.sendActionFailed();
				return;
			}
			if(activeChar.getFraction().canAttack(target.getFraction()))
			{
				activeChar.sendPacket(SystemMsg.THAT_IS_AN_INCORRECT_TARGET);
				return;
			}

			for(Event event : activeChar.getEvents())
				if(!event.canJoinParty(activeChar, target))
				{
					activeChar.sendPacket(SystemMsg.THAT_IS_AN_INCORRECT_TARGET);
					activeChar.sendActionFailed();
					return;
				}

			if(target.isBusy())
			{
				activeChar.sendPacket(new SystemMessagePacket(SystemMsg.C1_IS_ON_ANOTHER_TASK).addName(target));
				return;
			}
			IBroadcastPacket problem = target.canJoinParty(activeChar);
			if(problem != null)
			{
				activeChar.sendPacket(problem);
				return;
			}
			if(activeChar.isInParty())
			{
				if(activeChar.getParty().getMemberCount() >= Party.MAX_SIZE)
				{
					activeChar.sendPacket(SystemMsg.THE_PARTY_IS_FULL);
					return;
				}
				if(Config.PARTY_LEADER_ONLY_CAN_INVITE && !activeChar.getParty().isLeader(activeChar))
				{
					activeChar.sendPacket(SystemMsg.ONLY_THE_LEADER_CAN_GIVE_OUT_INVITATIONS);
					return;
				}
			}
			if (!PartyClassLimitService.getInstance().canJoin(activeChar, target)) {
				int limit = PartyClassLimitService.getInstance().getLimit(target.getClassId());
				activeChar.sendMessage(new CustomMessage("services.party.limit.error.request").addNumber(limit));
				return;
			}
			new Request(Request.L2RequestType.PARTY, activeChar, target).setTimeout(10000L).set("itemDistribution", _itemDistribution);
			target.sendPacket(new AskJoinPartyPacket(activeChar.getName(), _itemDistribution));
			activeChar.sendPacket(new SystemMessagePacket(SystemMsg.C1_HAS_BEEN_INVITED_TO_THE_PARTY).addName(target));
		}
	}
}
