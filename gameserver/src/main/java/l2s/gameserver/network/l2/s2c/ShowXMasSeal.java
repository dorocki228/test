package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingPackets;

public class ShowXMasSeal implements IClientOutgoingPacket
{
	private int _item;

	public ShowXMasSeal(int item)
	{
		_item = item;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingPackets.SHOW_XMAS_SEAL.writeId(packetWriter);
		packetWriter.writeD(_item);

		return true;
	}
}