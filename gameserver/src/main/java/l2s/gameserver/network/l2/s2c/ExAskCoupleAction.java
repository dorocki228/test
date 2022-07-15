package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingExPackets;

public class ExAskCoupleAction implements IClientOutgoingPacket
{
	private int _objectId, _socialId;

	public ExAskCoupleAction(int objectId, int socialId)
	{
		_objectId = objectId;
		_socialId = socialId;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_ASK_COUPLE_ACTION.writeId(packetWriter);
		packetWriter.writeD(_socialId);
		packetWriter.writeD(_objectId);

		return true;
	}
}
