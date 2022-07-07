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
import l2s.gameserver.model.items.PcFreight;
import l2s.gameserver.model.items.PcInventory;
import l2s.gameserver.network.l2.components.SystemMsg;
import org.apache.commons.lang3.ArrayUtils;

public class RequestPackageSend extends L2GameClientPacket
{
	private static final long _FREIGHT_FEE = 1000L;
	private int _objectId;
	private int _count;
	private int[] _items;
	private long[] _itemQ;

	@Override
	protected void readImpl()
	{
		_objectId = readD();
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
	protected void runImpl() throws Exception
	{
		Player player = getClient().getActiveChar();
		if(player == null || _count == 0)
			return;
		if(!player.getPlayerAccess().UseWarehouse)
		{
			player.sendActionFailed();
			return;
		}
		if(player.isActionsDisabled())
		{
			player.sendActionFailed();
			return;
		}
		if(player.isInStoreMode())
		{
			player.sendPacket(SystemMsg.WHILE_OPERATING_A_PRIVATE_STORE_OR_WORKSHOP_YOU_CANNOT_DISCARD_DESTROY_OR_TRADE_AN_ITEM);
			return;
		}
		if(player.isInTrade())
		{
			player.sendActionFailed();
			return;
		}
		NpcInstance whkeeper = player.getLastNpc();
		if(whkeeper == null || !player.checkInteractionDistance(whkeeper))
			return;
		if(!player.getAccountChars().containsKey(_objectId))
			return;
		PcInventory inventory = player.getInventory();
		PcFreight freight = new PcFreight(_objectId);
		freight.restore();
		inventory.writeLock();
		freight.writeLock();
		try
		{
            int slotsleft = Config.FREIGHT_SLOTS - freight.getSize();
            int items = 0;
            long adenaDeposit = 0L;
            for(int i = 0; i < _count; ++i)
			{
				ItemInstance item = inventory.getItemByObjectId(_items[i]);
				if(item == null || item.getCount() < _itemQ[i] || !item.canBeFreighted(player))
				{
					_items[i] = 0;
					_itemQ[i] = 0L;
				}
				else
				{
					if(!item.isStackable() || freight.getItemByItemId(item.getItemId()) == null)
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
				player.sendPacket(SystemMsg.YOU_HAVE_EXCEEDED_THE_QUANTITY_THAT_CAN_BE_INPUTTED);
			if(items == 0)
			{
				player.sendPacket(SystemMsg.INCORRECT_ITEM_COUNT);
				return;
			}
			long fee = SafeMath.mulAndCheck(items, 1000L);
			if(fee + adenaDeposit > player.getAdena())
			{
				player.sendPacket(SystemMsg.YOU_LACK_THE_FUNDS_NEEDED_TO_PAY_FOR_THIS_TRANSACTION);
				return;
			}
			if(!player.reduceAdena(fee, true))
			{
				player.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
				return;
			}
			for(int j = 0; j < _count; ++j)
				if(_items[j] != 0)
				{
					ItemInstance item2 = inventory.removeItemByObjectId(_items[j], _itemQ[j]);

					ItemLogMessage message = new ItemLogMessage(player, ItemLogProcess.FreightDeposit, item2);
					LogService.getInstance().log(LoggerType.ITEM, message);

					freight.addItem(item2);
				}
		}
		catch(ArithmeticException ae)
		{
			player.sendPacket(SystemMsg.YOU_HAVE_EXCEEDED_THE_QUANTITY_THAT_CAN_BE_INPUTTED);
			return;
		}
		finally
		{
			freight.writeUnlock();
			inventory.writeUnlock();
		}
		player.sendChanges();
		player.sendPacket(SystemMsg.THE_TRANSACTION_IS_COMPLETE);
	}
}
