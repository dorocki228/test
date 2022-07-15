package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingExPackets;

public class ExAskModifyPartyLooting implements IClientOutgoingPacket
{
	private String _requestor;
	private int _mode;

	public ExAskModifyPartyLooting(String name, int mode)
	{
		_requestor = name;
		_mode = mode;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_ASK_MODIFY_PARTY_LOOTING.writeId(packetWriter);
		packetWriter.writeS(_requestor);
		packetWriter.writeD(_mode);

		return true;
	}
}
