package l2s.gameserver.network.l2.s2c;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ItemInfo;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.items.Warehouse;
import l2s.gameserver.network.l2.OutgoingPackets;

/**
 * @author VISTALL
 * @date 20:46/16.05.2011
 */
public class PackageSendableListPacket extends AbstractItemPacket
{
	private final int _type;
	private int _targetObjectId;
	private long _adena;
	private List<ItemInfo> _itemList;

	public PackageSendableListPacket(int type, int objectId, Player player)
	{
		_type = type;
		_adena = player.getAdena();
		_targetObjectId = objectId;

		ItemInstance[] items = player.getInventory().getItems();
		Arrays.sort(items, Warehouse.ItemClassComparator.getInstance());
		_itemList = new ArrayList<ItemInfo>(items.length);
		for(ItemInstance item : items)
			if(item.getTemplate().isFreightable())
				_itemList.add(new ItemInfo(player, item, item.getTemplate().isBlocked(player, item)));
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingPackets.PACKAGE_SENDABLE_LIST.writeId(packetWriter);
		packetWriter.writeC(_type);
		if(_type == 1)
		{
			packetWriter.writeD(_targetObjectId);
			packetWriter.writeQ(_adena);
			packetWriter.writeD(_itemList.size());
		}
		else if(_type == 2)
		{
			packetWriter.writeD(_itemList.size());
			packetWriter.writeD(_itemList.size());
			for(ItemInfo item : _itemList)
			{
				writeItem(packetWriter, item);
				packetWriter.writeD(item.getObjectId());
			}
		}

		return true;
	}
}
