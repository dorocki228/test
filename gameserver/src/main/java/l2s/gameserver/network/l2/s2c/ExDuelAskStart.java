package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingExPackets;

public class ExDuelAskStart implements IClientOutgoingPacket
{
	String _requestor;
	int _isPartyDuel;

	public ExDuelAskStart(String requestor, int isPartyDuel)
	{
		_requestor = requestor;
		_isPartyDuel = isPartyDuel;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_DUEL_ASK_START.writeId(packetWriter);
		packetWriter.writeS(_requestor);
		packetWriter.writeD(_isPartyDuel);

		return true;
	}
}