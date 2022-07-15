package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.Party;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.s2c.ExMpccShowPartyMemberInfo;

public class RequestExMPCCShowPartyMembersInfo implements IClientIncomingPacket
{
	private int _objectId;

	@Override
	public boolean readImpl(l2s.gameserver.network.l2.GameClient client, l2s.commons.network.PacketReader packet)
	{
		_objectId = packet.readD();
		return true;
	}

	@Override
	public void run(l2s.gameserver.network.l2.GameClient client)
	{
		Player activeChar = client.getActiveChar();

		if(activeChar == null || !activeChar.isInParty() || !activeChar.getParty().isInCommandChannel())
			return;

		for(Party party : activeChar.getParty().getCommandChannel().getParties())
		{
			Player leader = party.getPartyLeader();
			if(leader != null && leader.getObjectId() == _objectId)
			{
				activeChar.sendPacket(new ExMpccShowPartyMemberInfo(party));
				break;
			}
		}
	}
}