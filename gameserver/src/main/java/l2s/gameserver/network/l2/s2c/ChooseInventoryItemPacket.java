package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.network.l2.OutgoingPackets;

public class ChooseInventoryItemPacket implements IClientOutgoingPacket
{
	private int ItemID;

	public ChooseInventoryItemPacket(int id)
	{
		ItemID = id;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingPackets.CHOOSE_INVENTORY_ITEM.writeId(packetWriter);
		packetWriter.writeD(ItemID);
		return true;
	}
}