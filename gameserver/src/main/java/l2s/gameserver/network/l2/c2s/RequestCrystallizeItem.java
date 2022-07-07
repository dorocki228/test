package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.logging.ItemLogProcess;
import l2s.gameserver.logging.LogService;
import l2s.gameserver.logging.LoggerType;
import l2s.gameserver.logging.message.ItemLogMessage;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.utils.ItemFunctions;

public class RequestCrystallizeItem extends L2GameClientPacket
{
	private int _objectId;
	private long _count;

	@Override
	protected void readImpl()
	{
		_objectId = readD();
		_count = readQ();
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;
		ItemInstance item = activeChar.getInventory().getItemByObjectId(_objectId);
		if(item == null)
		{
			activeChar.sendActionFailed();
			return;
		}
		if(!item.canBeCrystallized(activeChar))
		{
			activeChar.sendPacket(SystemMsg.THIS_ITEM_CANNOT_BE_CRYSTALLIZED);
			return;
		}

		ItemLogMessage message = new ItemLogMessage(activeChar, ItemLogProcess.Crystalize, item);
		LogService.getInstance().log(LoggerType.ITEM, message);

		if(!activeChar.getInventory().destroyItemByObjectId(_objectId, _count))
		{
			activeChar.sendActionFailed();
			return;
		}
		activeChar.sendPacket(SystemMsg.THE_ITEM_HAS_BEEN_SUCCESSFULLY_CRYSTALLIZED);
/*		int crystalId = item.getGrade().getCrystalId();
		int crystalCount = item.getCrystalCountOnCrystallize();
		if(crystalId > 0 && crystalCount > 0)
			ItemFunctions.addItem(activeChar, crystalId, crystalCount, true);*/
		activeChar.sendChanges();
	}
}
