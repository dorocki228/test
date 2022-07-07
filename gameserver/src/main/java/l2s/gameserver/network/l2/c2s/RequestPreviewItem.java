package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.Config;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.data.xml.holder.BuyListHolder;
import l2s.gameserver.data.xml.holder.ItemHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.items.Inventory;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.ShopPreviewInfoPacket;
import l2s.gameserver.network.l2.s2c.ShopPreviewListPacket;
import l2s.gameserver.templates.item.ItemTemplate;
import l2s.gameserver.templates.npc.BuyListTemplate;
import l2s.gameserver.utils.NpcUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class RequestPreviewItem extends L2GameClientPacket
{
	private static final Logger _log;
	private int _unknow;
	private int _listId;
	private int _count;
	private int[] _items;

	@Override
	protected void readImpl()
	{
		_unknow = readD();
		_listId = readD();
		_count = readD();
		if(_count * 4 > _buf.remaining() || _count > 32767 || _count < 1)
		{
			_count = 0;
			return;
		}
		_items = new int[_count];
		for(int i = 0; i < _count; ++i)
			_items[i] = readD();
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null || _count == 0)
			return;
		if(activeChar.isActionsDisabled())
		{
			activeChar.sendActionFailed();
			return;
		}
		if(activeChar.isInStoreMode())
		{
			activeChar.sendPacket(SystemMsg.WHILE_OPERATING_A_PRIVATE_STORE_OR_WORKSHOP_YOU_CANNOT_DISCARD_DESTROY_OR_TRADE_AN_ITEM);
			return;
		}
		if(activeChar.isInTrade())
		{
			activeChar.sendActionFailed();
			return;
		}
		if(!Config.ALT_GAME_KARMA_PLAYER_CAN_SHOP && activeChar.isPK() && !activeChar.isGM())
		{
			activeChar.sendActionFailed();
			return;
		}
		BuyListTemplate list = null;
		NpcInstance merchant = NpcUtils.canPassPacket(activeChar, this);
		if(merchant != null)
			list = merchant.getBuyList(_listId);
		if(activeChar.isGM() && (merchant == null || list == null || merchant.getNpcId() != list.getNpcId()))
			list = BuyListHolder.getInstance().getBuyList(_listId);
		if(list == null)
		{
			activeChar.sendActionFailed();
			return;
		}
		int slots = 0;
        Map<Integer, Integer> itemList = new HashMap<>();
		try
		{
            long totalPrice = 0L;
            for(int i = 0; i < _count; ++i)
			{
				int itemId = _items[i];
				if(list.getItemByItemId(itemId) == null)
				{
					activeChar.sendActionFailed();
					return;
				}
				ItemTemplate template = ItemHolder.getInstance().getTemplate(itemId);
				if(template != null)
					if(template.isEquipable())
					{
						int paperdoll = Inventory.getPaperdollIndex(template.getBodyPart());
						if(paperdoll >= 0)
						{
							if(itemList.containsKey(paperdoll))
							{
								activeChar.sendPacket(SystemMsg.YOU_CAN_NOT_TRY_THOSE_ITEMS_ON_AT_THE_SAME_TIME);
								return;
							}
							itemList.put(paperdoll, itemId);
							totalPrice += ShopPreviewListPacket.getWearPrice(template);
						}
					}
			}
			if(!activeChar.reduceAdena(totalPrice))
			{
				activeChar.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
				return;
			}
		}
		catch(ArithmeticException ae)
		{
			activeChar.sendPacket(SystemMsg.YOU_HAVE_EXCEEDED_THE_QUANTITY_THAT_CAN_BE_INPUTTED);
			return;
		}
		if(!itemList.isEmpty())
		{
			activeChar.sendPacket(new ShopPreviewInfoPacket(itemList));
			ThreadPoolManager.getInstance().schedule(new RemoveWearItemsTask(activeChar), Config.WEAR_DELAY * 1000);
		}
	}

	static
	{
		_log = LoggerFactory.getLogger(RequestPreviewItem.class);
	}

	private static class RemoveWearItemsTask implements Runnable
	{
		private final Player _activeChar;

		public RemoveWearItemsTask(Player activeChar)
		{
			_activeChar = activeChar;
		}

		@Override
		public void run()
		{
			_activeChar.sendPacket(SystemMsg.YOU_ARE_NO_LONGER_TRYING_ON_EQUIPMENT_);
			_activeChar.sendUserInfo(true);
		}
	}
}
