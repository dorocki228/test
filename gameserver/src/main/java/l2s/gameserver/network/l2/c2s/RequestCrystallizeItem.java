package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.utils.ItemFunctions;
import l2s.gameserver.utils.loggers.ItemLogger;

public class RequestCrystallizeItem implements IClientIncomingPacket
{
	private int _objectId;
	private long _count;

	@Override
	public boolean readImpl(l2s.gameserver.network.l2.GameClient client, l2s.commons.network.PacketReader packet)
	{
		_objectId = packet.readD();
		_count = packet.readQ();
		return true;
	}

	@Override
	public void run(l2s.gameserver.network.l2.GameClient client)
	{
		Player activeChar = client.getActiveChar();

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
			// На всякий пожарный..
			activeChar.sendPacket(SystemMsg.THIS_ITEM_CANNOT_BE_CRYSTALLIZED);
			return;
		}

		// Check if the char can crystallize items and return if false;
		if (activeChar.getStat().getCrystallizeGrade().ordinal() < item.getTemplate().getGrade().ordinal())	{
			activeChar.sendPacket(SystemMsg.YOU_MAY_NOT_CRYSTALLIZE_THIS_ITEM);
			activeChar.sendActionFailed();
			return;
		}

		ItemLogger.INSTANCE.log(ItemLogger.ItemProcess.Crystalize, activeChar, item);

		if(!activeChar.getInventory().destroyItemByObjectId(_objectId, _count))
		{
			activeChar.sendActionFailed();
			return;
		}

		activeChar.sendPacket(SystemMsg.THE_ITEM_HAS_BEEN_SUCCESSFULLY_CRYSTALLIZED);

		int crystalId = item.getGrade().getCrystalId();
		int crystalCount = item.getCrystalCountOnCrystallize();
		if(crystalId > 0 && crystalCount > 0)
			ItemFunctions.addItem(activeChar, crystalId, crystalCount, true);

		activeChar.sendChanges();
	}
}