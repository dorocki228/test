package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.instances.PetInstance;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.OutgoingPackets;

public class PetItemListPacket extends AbstractItemPacket
{
	private ItemInstance[] items;

	public PetItemListPacket(PetInstance cha)
	{
		items = cha.getInventory().getItems();
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingPackets.PET_ITEM_LIST.writeId(packetWriter);
		packetWriter.writeH(items.length);

		for(ItemInstance item : items)
			writeItem(packetWriter, null, item);

		return true;
	}
}