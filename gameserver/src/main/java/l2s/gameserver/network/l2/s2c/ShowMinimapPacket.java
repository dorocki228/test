package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.OutgoingPackets;

public class ShowMinimapPacket implements IClientOutgoingPacket
{
	private int _mapId;

	public ShowMinimapPacket(Player player, int mapId)
	{
		_mapId = mapId;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingPackets.SHOW_MINIMAP.writeId(packetWriter);
		packetWriter.writeD(_mapId);
		packetWriter.writeC(0x00);

		return true;
	}
}