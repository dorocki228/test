package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingExPackets;

/**
 * @author Bonux
**/
public final class ExAutoFishAvailable implements IClientOutgoingPacket
{
	public static final IClientOutgoingPacket REMOVE = new ExAutoFishAvailable(0);
	public static final IClientOutgoingPacket SHOW = new ExAutoFishAvailable(1);
	public static final IClientOutgoingPacket FISHING = new ExAutoFishAvailable(2);

	private final int _type;

	private ExAutoFishAvailable(int type)
	{
		_type = type;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_AUTOFISH_AVAILABLE.writeId(packetWriter);
		packetWriter.writeC(_type);

		return true;
	}
}