package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.Config;
import l2s.gameserver.network.l2.OutgoingPackets;

public class AllianceCrestPacket implements IClientOutgoingPacket
{
	private int _crestId;
	private byte[] _data;

	public AllianceCrestPacket(int crestId, byte[] data)
	{
		_crestId = crestId;
		_data = data;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingPackets.ALLIANCE_CREST.writeId(packetWriter);

		packetWriter.writeD(Config.REQUEST_ID);
		packetWriter.writeD(_crestId);
		packetWriter.writeD(_data.length);
		packetWriter.writeB(_data);
		return true;
	}
}