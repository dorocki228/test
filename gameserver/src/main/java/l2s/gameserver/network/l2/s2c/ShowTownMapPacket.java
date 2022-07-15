package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingPackets;

public class ShowTownMapPacket implements IClientOutgoingPacket
{
	/**
	 * Format: csdd
	 */

	String _texture;
	int _x;
	int _y;

	public ShowTownMapPacket(String texture, int x, int y)
	{
		_texture = texture;
		_x = x;
		_y = y;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingPackets.SHOW_TOWN_MAP.writeId(packetWriter);
		packetWriter.writeS(_texture);
		packetWriter.writeD(_x);
		packetWriter.writeD(_y);

		return true;
	}
}