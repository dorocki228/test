package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingPackets;

public class GameGuardQuery implements IClientOutgoingPacket
{
	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingPackets.GAME_GUARD_QUERY.writeId(packetWriter);
		packetWriter.writeD(0x00); // ? - Меняется при каждом перезаходе.
		packetWriter.writeD(0x00); // ? - Меняется при каждом перезаходе.
		packetWriter.writeD(0x00); // ? - Меняется при каждом перезаходе.
		packetWriter.writeD(0x00); // ? - Меняется при каждом перезаходе.

		return true;
	}
}