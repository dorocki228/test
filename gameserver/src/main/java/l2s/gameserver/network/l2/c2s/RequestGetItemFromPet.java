package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.logging.ItemLogProcess;
import l2s.gameserver.logging.LogService;
import l2s.gameserver.logging.LoggerType;
import l2s.gameserver.logging.message.ItemLogMessage;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.PetInstance;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.items.PcInventory;
import l2s.gameserver.model.items.PetInventory;
import l2s.gameserver.network.l2.components.SystemMsg;

public class RequestGetItemFromPet extends L2GameClientPacket
{
	private int _objectId;
	private long _amount;
	private int _unknown;

	@Override
	protected void readImpl()
	{
		_objectId = readD();
		_amount = readQ();
		_unknown = readD();
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null || _amount < 1)
			return;

		PetInstance pet = activeChar.getPet();
		if(pet == null)
		{
			activeChar.sendActionFailed();
			return;
		}

		if(activeChar.isOutOfControl())
		{
			activeChar.sendActionFailed();
			return;
		}

		if(activeChar.isInStoreMode())
		{
			activeChar.sendPacket(SystemMsg.WHILE_OPERATING_A_PRIVATE_STORE_OR_WORKSHOP_YOU_CANNOT_DISCARD_DESTROY_OR_TRADE_AN_ITEM);
			return;
		}

		if(activeChar.isInTrade() || activeChar.isProcessingRequest())
		{
			activeChar.sendActionFailed();
			return;
		}

		if(activeChar.isFishing())
		{
			activeChar.sendPacket(SystemMsg.YOU_CANNOT_DO_THAT_WHILE_FISHING);
			return;
		}

		PetInventory petInventory = pet.getInventory();
		PcInventory playerInventory = activeChar.getInventory();

		petInventory.writeLock();
		playerInventory.writeLock();
		try
		{
			ItemInstance item = petInventory.getItemByObjectId(_objectId);
			if(item == null || item.getCount() < _amount || item.isEquipped())
			{
				activeChar.sendActionFailed();
				return;
			}

			int slots = 0;
			long weight = item.getTemplate().getWeight() * _amount;
			if(!item.getTemplate().isStackable() || activeChar.getInventory().getItemByItemId(item.getItemId()) == null)
				slots = 1;

			if(!activeChar.getInventory().validateWeight(weight))
			{
				activeChar.sendPacket(SystemMsg.YOU_HAVE_EXCEEDED_THE_WEIGHT_LIMIT);
				return;
			}

			if(!activeChar.getInventory().validateCapacity(slots))
			{
				activeChar.sendPacket(SystemMsg.YOUR_INVENTORY_IS_FULL);
				return;
			}

			item = petInventory.removeItemByObjectId(_objectId, _amount);

			ItemLogMessage message = new ItemLogMessage(activeChar, ItemLogProcess.FromPet, item);
			LogService.getInstance().log(LoggerType.ITEM, message);

			playerInventory.addItem(item);

			pet.sendChanges();
			activeChar.sendChanges();
		}
		finally
		{
			petInventory.writeUnlock();
			playerInventory.writeUnlock();
		}
	}
}
