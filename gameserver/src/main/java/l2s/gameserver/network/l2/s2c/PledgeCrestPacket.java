package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.Config;
import l2s.gameserver.network.l2.OutgoingPackets;

public class PledgeCrestPacket implements IClientOutgoingPacket
{
	private int _crestId;
	private int _crestSize;
	private byte[] _data;

	public PledgeCrestPacket(int crestId, byte[] data)
	{
		_crestId = crestId;
		_data = data;
		_crestSize = _data.length;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingPackets.PLEDGE_CREST.writeId(packetWriter);
		packetWriter.writeD(Config.REQUEST_ID);
		packetWriter.writeD(_crestId);
		packetWriter.writeD(_crestSize);
		packetWriter.writeB(_data);

		return true;
	}
}