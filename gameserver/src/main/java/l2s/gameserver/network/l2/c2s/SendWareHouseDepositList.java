package l2s.gameserver.network.l2.c2s;

import l2s.commons.math.SafeMath;
import l2s.gameserver.Config;
import l2s.gameserver.logging.ItemLogProcess;
import l2s.gameserver.logging.LogService;
import l2s.gameserver.logging.LoggerType;
import l2s.gameserver.logging.message.ItemLogMessage;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.items.PcInventory;
import l2s.gameserver.model.items.Warehouse;
import l2s.gameserver.network.l2.components.SystemMsg;
import org.apache.commons.lang3.ArrayUtils;

public class SendWareHouseDepositList extends L2GameClientPacket
{
	private static final long WAREHOUSE_FEE = 1;
	private int _count;
	private int[] _items;
	private long[] _itemQ;

	@Override
	protected void readImpl()
	{
		_count = readD();
		if(_count * 12 > _buf.remaining() || _count > 32767 || _count < 1)
		{
			_count = 0;
			return;
		}
		_items = new int[_count];
		_itemQ = new long[_count];
		for(int i = 0; i < _count; ++i)
		{
			_items[i] = readD();
			_itemQ[i] = readQ();
			if(_itemQ[i] < 1L || ArrayUtils.indexOf(_items, _items[i]) < i)
			{
				_count = 0;
				return;
			}
		}
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null || _count == 0)
			return;
		if(!activeChar.getPlayerAccess().UseWarehouse)
		{
			activeChar.sendActionFailed();
			return;
		}
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
		NpcInstance whkeeper = activeChar.getLastNpc();
		if(whkeeper == null || !activeChar.checkInteractionDistance(whkeeper))
		{
			activeChar.sendPacket(SystemMsg.YOU_HAVE_MOVED_TOO_FAR_AWAY_FROM_THE_WAREHOUSE_TO_PERFORM_THAT_ACTION);
			return;
		}
		PcInventory inventory = activeChar.getInventory();
		boolean privatewh = activeChar.getUsingWarehouseType() != Warehouse.WarehouseType.CLAN;
		Warehouse warehouse;
		if(privatewh)
			warehouse = activeChar.getWarehouse();
		else
			warehouse = activeChar.getClan().getWarehouse();
		inventory.writeLock();
		warehouse.writeLock();
		try
		{
			int slotsleft = 0;
            if(privatewh)
				slotsleft = activeChar.getWarehouseLimit() - warehouse.getSize();
			else
				slotsleft = activeChar.getClan().getWhBonus() + Config.WAREHOUSE_SLOTS_CLAN - warehouse.getSize();
			int items = 0;
            long adenaDeposit = 0L;
            for(int i = 0; i < _count; ++i)
			{
				ItemInstance item = inventory.getItemByObjectId(_items[i]);
				if(item == null || item.getCount() < _itemQ[i] || !item.canBeStored(activeChar, privatewh))
				{
					_items[i] = 0;
					_itemQ[i] = 0L;
				}
				else
				{
					if(!item.isStackable() || warehouse.getItemByItemId(item.getItemId()) == null)
					{
						if(slotsleft <= 0)
						{
							_items[i] = 0;
							_itemQ[i] = 0L;
							continue;
						}
						--slotsleft;
					}
					if(item.getItemId() == 57)
						adenaDeposit = _itemQ[i];
					++items;
				}
			}
			if(slotsleft <= 0)
				activeChar.sendPacket(SystemMsg.YOUR_WAREHOUSE_IS_FULL);
			if(items == 0)
			{
				activeChar.sendPacket(SystemMsg.INCORRECT_ITEM_COUNT);
				return;
			}
			long fee = SafeMath.mulAndCheck(items, WAREHOUSE_FEE);
			if(fee + adenaDeposit > activeChar.getAdena())
			{
				activeChar.sendPacket(SystemMsg.YOU_LACK_THE_FUNDS_NEEDED_TO_PAY_FOR_THIS_TRANSACTION);
				return;
			}
			if(!activeChar.reduceAdena(fee, true))
			{
				activeChar.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
				return;
			}
			for(int j = 0; j < _count; ++j)
				if(_items[j] != 0)
				{
					ItemInstance item2 = inventory.removeItemByObjectId(_items[j], _itemQ[j]);

					if (item2 != null) {
						ItemLogProcess process = privatewh
								? ItemLogProcess.WarehouseDeposit
								: ItemLogProcess.ClanWarehouseDeposit;
						ItemLogMessage message = new ItemLogMessage(activeChar, process, item2);
						LogService.getInstance().log(LoggerType.ITEM, message);
					}

					warehouse.addItem(item2);
				}
		}
		catch(ArithmeticException ae)
		{
			activeChar.sendPacket(SystemMsg.YOU_HAVE_EXCEEDED_THE_QUANTITY_THAT_CAN_BE_INPUTTED);
			return;
		}
		finally
		{
			warehouse.writeUnlock();
			inventory.writeUnlock();
		}
		activeChar.sendChanges();
		activeChar.sendPacket(SystemMsg.THE_TRANSACTION_IS_COMPLETE);
	}
}
