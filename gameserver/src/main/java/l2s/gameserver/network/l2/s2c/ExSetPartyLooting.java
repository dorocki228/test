package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingExPackets;

public class ExSetPartyLooting implements IClientOutgoingPacket
{
	private int _result;
	private int _mode;

	public ExSetPartyLooting(int result, int mode)
	{
		_result = result;
		_mode = mode;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_SET_PARTY_LOOTING.writeId(packetWriter);
		packetWriter.writeD(_result);
		packetWriter.writeD(_mode);

		return true;
	}
}
