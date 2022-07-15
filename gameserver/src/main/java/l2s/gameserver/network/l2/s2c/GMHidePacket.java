package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingPackets;

public class GMHidePacket implements IClientOutgoingPacket
{
	private final int obj_id;

	public GMHidePacket(int id)
	{
		obj_id = id; //TODO хз чей id должен посылатся, нужно эксперементировать
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingPackets.GM_HIDE.writeId(packetWriter);
		packetWriter.writeD(obj_id);

		return true;
	}
}