package l2s.gameserver.network.l2.s2c;

import java.util.*;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.items.TradeItem;
import l2s.gameserver.network.l2.OutgoingPackets;
import l2s.gameserver.templates.item.ItemTemplate;

public class PrivateStoreManageList extends AbstractItemPacket
{
	private final int _type;
	private final int _sellerId;
	private final long _adena;
	private final boolean _package;
	private final List<TradeItem> _sellList;
	private final Map<Integer, TradeItem> _sellList0;

	/**
	 * Окно управления личным магазином продажи
	 * @param seller
	 */
	public PrivateStoreManageList(int type, Player seller, boolean pkg)
	{
		_type = type;
		_sellerId = seller.getObjectId();
		_adena = seller.getAdena();
		_package = pkg;
		_sellList0 = seller.getSellList(_package);
		_sellList = new ArrayList<>();

		// Проверяем список вещей в инвентаре, если вещь остутствует - убираем из списка продажи
		Iterator<Map.Entry<Integer, TradeItem>> iterator = _sellList0.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<Integer, TradeItem> entry = iterator.next();

			TradeItem si = entry.getValue();
			if(si.getCount() <= 0)
			{
				iterator.remove();
				continue;
			}

			ItemInstance item = seller.getInventory().getItemByObjectId(si.getObjectId());
			if(item == null)
				//вещь недоступна, пробуем найти такую же по itemId
				item = seller.getInventory().getItemByItemId(si.getItemId());

			if(item == null || !item.canBePrivateStore(seller) || item.getItemId() == ItemTemplate.ITEM_ID_ADENA)
			{
				iterator.remove();
				continue;
			}

			//корректируем количество
			si.setCount(Math.min(item.getCount(), si.getCount()));
		}

		ItemInstance[] items = seller.getInventory().getItems();
		// Проверяем список вещей в инвентаре, если вещь остутствует в списке продажи, добавляем в список доступных для продажи
		loop: for(ItemInstance item : items)
			if(item.canBePrivateStore(seller) && item.getItemId() != ItemTemplate.ITEM_ID_ADENA)
			{
				for(TradeItem si : _sellList0.values())
					if(si.getObjectId() == item.getObjectId())
					{
						if(si.getCount() == item.getCount())
							continue loop;
						// Показывает остаток вещей для продажи
						TradeItem ti = new TradeItem(seller, item, item.getTemplate().isBlocked(seller, item));
						ti.setCount(item.getCount() - si.getCount());
						_sellList.add(ti);
						continue loop;
					}
				_sellList.add(new TradeItem(seller, item, item.getTemplate().isBlocked(seller, item)));
			}
	}

	@Override
	public boolean write(l2s.commons.network.PacketWriter packetWriter)
	{
		OutgoingPackets.PRIVATE_STORE_MANAGE_LIST.writeId(packetWriter);
		packetWriter.writeC(_type);
		if(_type == 1)
		{
			packetWriter.writeD(_sellerId);
			packetWriter.writeD(_package ? 1 : 0);
			packetWriter.writeQ(_adena);
			packetWriter.writeD(_sellList0.size());
			//Список вещей уже поставленых на продажу
			for(TradeItem si : _sellList0.values())
			{
				writeItem(packetWriter, si);
				packetWriter.writeQ(si.getOwnersPrice());
				packetWriter.writeQ(si.getStorePrice());
			}
			packetWriter.writeD(_sellList.size());
		}
		else if(_type == 2)
		{
			//Список имеющихся вещей
			packetWriter.writeD(_sellList.size());
			packetWriter.writeD(_sellList.size());
			for(TradeItem si : _sellList)
			{
				writeItem(packetWriter, si);
				packetWriter.writeQ(si.getStorePrice());
			}
		}

		return true;
	}
}