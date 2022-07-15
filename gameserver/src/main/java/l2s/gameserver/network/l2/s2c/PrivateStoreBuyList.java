package l2s.gameserver.network.l2.s2c;

import java.util.ArrayList;
import java.util.List;

import l2s.gameserver.idfactory.IdFactory;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.items.TradeItem;

import l2s.gameserver.network.l2.OutgoingPackets;
import org.napile.primitive.sets.IntSet;
import org.napile.primitive.sets.impl.HashIntSet;

public class PrivateStoreBuyList extends AbstractItemPacket
{
	private final int _buyerId;
	private final long _adena;
	private List<TradeItem> _sellList;

	/**
	 * Список вещей в личном магазине покупки, показываемый продающему
	 * @param seller
	 * @param buyer
	 */
	public PrivateStoreBuyList(Player seller, Player buyer)
	{
		_adena = seller.getAdena();
		_buyerId = buyer.getObjectId();
		_sellList = new ArrayList<TradeItem>();

		final ItemInstance[] items = seller.getInventory().getItems();
		final IntSet addedItems = new HashIntSet();
		for(TradeItem bi : buyer.getBuyList())
		{
			TradeItem si = null;

			for(ItemInstance item : items)
			{
				// TODO: Нужно ли представлять итемы по приоритету? (Заточка, Атрибут и т.д.)
				if(item.getItemId() == bi.getItemId() && item.canBePrivateStore(seller) && !addedItems.contains(item.getObjectId()))
				{
					if((!item.isArmor() && !item.isAccessory() && !item.isWeapon()) || item.getEnchantLevel() == bi.getEnchantLevel())
					{
						si = new TradeItem(null, item);
						si.setOwnersPrice(bi.getOwnersPrice());
						si.setCount(bi.getCount());
						si.setCurrentValue(Math.min(bi.getCount(), item.getCount()));
						addedItems.add(item.getObjectId());
						break;
					}
				}
			}

			if(si == null)
			{
				si = new TradeItem();
				si.setItemId(bi.getItemId());
				si.setOwnersPrice(bi.getOwnersPrice());
				si.setCount(bi.getCount());
				si.setEnchantLevel(bi.getEnchantLevel());
				si.setCurrentValue(0);
			}
			_sellList.add(si);
		}
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingPackets.PRIVATE_STORE_BUY_LIST.writeId(packetWriter);
		packetWriter.writeD(_buyerId);
		packetWriter.writeQ(_adena);
		packetWriter.writeD(70);
		packetWriter.writeD(_sellList.size());
		for(TradeItem si : _sellList)
		{
			writeItem(packetWriter, si, si.getCurrentValue());
			packetWriter.writeD(si.getObjectId());
			packetWriter.writeQ(si.getOwnersPrice());
			packetWriter.writeQ(si.getStorePrice());
			packetWriter.writeQ(si.getCount()); // maximum possible tradecount
		}

		return true;
	}
}