package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.GameTimeController;
import l2s.gameserver.network.l2.OutgoingPackets;

public class ClientSetTimePacket implements IClientOutgoingPacket
{
	public static final IClientOutgoingPacket STATIC = new ClientSetTimePacket();

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingPackets.CLIENT_SET_TIME.writeId(packetWriter);
		packetWriter.writeD(GameTimeController.getInstance().getGameTime()); // time in client minutes
		packetWriter.writeD(6); //constant to match the server time( this determines the speed of the client clock)
		return true;
	}
}