package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.base.ShotType;
import l2s.gameserver.network.l2.OutgoingExPackets;

public class ExAutoSoulShot implements IClientOutgoingPacket
{
	private final int _itemId;
	private final int _slotId;
	private final int _type;

	public ExAutoSoulShot(int itemId, int slotId, ShotType type)
	{
		_itemId = itemId;
		_slotId = slotId;
		_type = type.ordinal();
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_AUTO_SOULSHOT.writeId(packetWriter);
		packetWriter.writeD(_itemId);
		packetWriter.writeD(_slotId);
		packetWriter.writeD(_type);

		return true;
	}
}