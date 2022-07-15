package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingExPackets;

/**
 * @author Bonux
**/
public class ExCuriousHouseObserveMode implements IClientOutgoingPacket
{
	public static final IClientOutgoingPacket ENTER = new ExCuriousHouseObserveMode(false);
	public static final IClientOutgoingPacket LEAVE = new ExCuriousHouseObserveMode(true);

	private final boolean _leave;

	public ExCuriousHouseObserveMode(boolean leave)
	{
		_leave = leave;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_CURIOUHOUSE_OBSERVE_MODE.writeId(packetWriter);
		packetWriter.writeC(_leave);

		return true;
	}
}
