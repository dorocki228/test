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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SendWareHouseWithDrawList extends L2GameClientPacket
{
	private static final Logger _log;
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
				break;
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
		Warehouse warehouse = null;
		ItemLogProcess logProcess = null;
		if(activeChar.getUsingWarehouseType() == Warehouse.WarehouseType.PRIVATE)
		{
			warehouse = activeChar.getWarehouse();
			logProcess = ItemLogProcess.WarehouseWithdraw;
		}
		else if(activeChar.getUsingWarehouseType() == Warehouse.WarehouseType.CLAN)
		{
			logProcess = ItemLogProcess.ClanWarehouseWithdraw;
			boolean canWithdrawCWH = false;
			if(activeChar.getClan() != null && (activeChar.getClanPrivileges() & 0x8) == 0x8 && (Config.ALT_ALLOW_OTHERS_WITHDRAW_FROM_CLAN_WAREHOUSE || activeChar.isClanLeader() || activeChar.getVarBoolean("canWhWithdraw")))
				canWithdrawCWH = true;
			if(!canWithdrawCWH)
				return;
			warehouse = activeChar.getClan().getWarehouse();
		}
		else
		{
			if(activeChar.getUsingWarehouseType() != Warehouse.WarehouseType.FREIGHT)
			{
				_log.warn("Error retrieving a warehouse object for char " + activeChar.getName() + " - using warehouse type: " + activeChar.getUsingWarehouseType());
				return;
			}
			warehouse = activeChar.getFreight();
			logProcess = ItemLogProcess.FreightWithdraw;
		}
		PcInventory inventory = activeChar.getInventory();
		inventory.writeLock();
		warehouse.writeLock();
		try
		{
			long weight = 0L;
			int slots = 0;
			for(int i = 0; i < _count; ++i)
			{
				ItemInstance item = warehouse.getItemByObjectId(_items[i]);
				if(item == null || item.getCount() < _itemQ[i])
				{
					activeChar.sendPacket(SystemMsg.INCORRECT_ITEM_COUNT);
					return;
				}
				weight = SafeMath.addAndCheck(weight, SafeMath.mulAndCheck(item.getTemplate().getWeight(), _itemQ[i]));
				if(!item.isStackable() || inventory.getItemByItemId(item.getItemId()) == null)
					++slots;
			}
			if(!activeChar.getInventory().validateCapacity(slots))
			{
				activeChar.sendPacket(SystemMsg.YOUR_INVENTORY_IS_FULL);
				return;
			}
			if(!activeChar.getInventory().validateWeight(weight))
			{
				activeChar.sendPacket(SystemMsg.YOU_HAVE_EXCEEDED_THE_WEIGHT_LIMIT);
				return;
			}
			for(int i = 0; i < _count; ++i)
			{
				ItemInstance item = warehouse.removeItemByObjectId(_items[i], _itemQ[i]);

				ItemLogMessage message = new ItemLogMessage(activeChar, logProcess, item);
				LogService.getInstance().log(LoggerType.ITEM, message);

				activeChar.getInventory().addItem(item);
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

	static
	{
		_log = LoggerFactory.getLogger(SendWareHouseWithDrawList.class);
	}
}
