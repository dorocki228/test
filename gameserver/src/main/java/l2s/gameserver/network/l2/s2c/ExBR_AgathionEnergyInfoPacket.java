package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.items.ItemInfo;
import l2s.gameserver.network.l2.OutgoingExPackets;

import java.util.Collection;

/**
 * @author VISTALL
 */
public class ExBR_AgathionEnergyInfoPacket implements IClientOutgoingPacket
{
	private final int _size;
	private final Collection<ItemInfo> _itemList;

	public ExBR_AgathionEnergyInfoPacket(int size, Collection<ItemInfo> items)
	{
		_itemList = items;
		_size = size;
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingExPackets.EX_BR_AGATHION_ENERGY_INFO.writeId(packetWriter);
		packetWriter.writeD(_size);
		for(ItemInfo item : _itemList)
		{
			packetWriter.writeD(item.getObjectId());
			packetWriter.writeD(item.getItemId());
			packetWriter.writeQ(0x200000);
			// TODO add agathion energy to ItemInfo
			packetWriter.writeD(item.getItem().getAgathionEnergy());//current energy
			packetWriter.writeD(item.getItem().getAgathionMaxEnergy()); //max energy
		}

		return true;
	}
}