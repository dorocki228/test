package l2s.gameserver.network.l2.s2c;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.items.TradeItem;
import l2s.gameserver.model.items.Warehouse.ItemClassComparator;
import l2s.gameserver.network.l2.OutgoingPackets;
import l2s.gameserver.templates.item.ItemTemplate;

public class PrivateStoreBuyManageList extends AbstractItemPacket
{
	private final int _type;
	private final int _buyerId;
	private final long _adena;
	private final List<TradeItem> _buyList0;
	private final List<TradeItem> _buyList;

	/**
	 * Окно управления личным магазином покупки
	 * @param buyer
	 */
	public PrivateStoreBuyManageList(int type, Player buyer)
	{
		_type = type;
		_buyerId = buyer.getObjectId();
		_adena = buyer.getAdena();
		_buyList0 = buyer.getBuyList();
		_buyList = new ArrayList<TradeItem>();

		ItemInstance[] items = buyer.getInventory().getItems();
		Arrays.sort(items, ItemClassComparator.getInstance());
		TradeItem bi;
		for(ItemInstance item : items)
			if(item.canBePrivateStore(buyer) && item.getItemId() != ItemTemplate.ITEM_ID_ADENA)
			{
				_buyList.add(bi = new TradeItem(buyer, item, item.getTemplate().isBlocked(buyer, item)));
				bi.setObjectId(0);
			}
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingPackets.PRIVATE_STORE_BUY_MANAGE_LIST.writeId(packetWriter);
		packetWriter.writeC(_type);
		if(_type == 1)
		{
			packetWriter.writeD(_buyerId);
			packetWriter.writeQ(_adena);
			packetWriter.writeD(_buyList0.size());//count for any items already added for sell
			for(TradeItem bi : _buyList0)
			{
				writeItem(packetWriter, bi);
				packetWriter.writeQ(bi.getOwnersPrice());
				packetWriter.writeQ(bi.getStorePrice());
				packetWriter.writeQ(bi.getCount());
			}
			packetWriter.writeD(_buyList.size());
		}
		else if(_type == 2)
		{
			packetWriter.writeD(_buyList.size());
			packetWriter.writeD(_buyList.size());//for potential sells
			for(TradeItem bi : _buyList)
			{
				writeItem(packetWriter, bi);
				packetWriter.writeQ(bi.getStorePrice());
			}
		}

		return true;
	}
}