package l2s.gameserver.network.l2.c2s;

import l2s.commons.util.Rnd;
import l2s.gameserver.data.xml.holder.SynthesisDataHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.items.PcInventory;
import l2s.gameserver.network.l2.s2c.ExEnchantFail;
import l2s.gameserver.network.l2.s2c.ExEnchantSucess;
import l2s.gameserver.templates.item.support.SynthesisData;
import l2s.gameserver.utils.ItemFunctions;

public class RequestNewEnchantTry extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		if(activeChar.isActionsDisabled())
		{
			activeChar.sendPacket(ExEnchantFail.STATIC);
			return;
		}

		if(activeChar.isInStoreMode())
		{
			activeChar.sendPacket(ExEnchantFail.STATIC);
			return;
		}

		if(activeChar.isInTrade())
		{
			activeChar.sendPacket(ExEnchantFail.STATIC);
			return;
		}

		if(activeChar.isFishing())
		{
			activeChar.sendPacket(ExEnchantFail.STATIC);
			return;
		}

		ItemInstance item1 = activeChar.getSynthesisItem1();
		if(item1 == null)
		{
			activeChar.sendPacket(ExEnchantFail.STATIC);
			return;
		}

		ItemInstance item2 = activeChar.getSynthesisItem2();
		if(item2 == null)
		{
			activeChar.sendPacket(ExEnchantFail.STATIC);
			return;
		}

		if(item1 == item2)
		{
			activeChar.sendPacket(ExEnchantFail.STATIC);
			return;
		}

		SynthesisData data = null;
		for(SynthesisData d : SynthesisDataHolder.getInstance().getDatas())
		{
			if(item1.getItemId() == d.getSlotone() && item2.getItemId() == d.getSlottwo())
			{
				data = d;
				break;
			}

			if(item1.getItemId() != d.getSlottwo() || item2.getItemId() != d.getSlotone())
				continue;

			data = d;
			break;
		}

		if(data == null)
		{
			activeChar.sendPacket(ExEnchantFail.STATIC);
			return;
		}

		PcInventory inventory = activeChar.getInventory();
		inventory.writeLock();
		try
		{
			if(inventory.getItemByObjectId(item1.getObjectId()) == null)
			{
				activeChar.sendPacket(ExEnchantFail.STATIC);
				return;
			}

			if(inventory.getItemByObjectId(item2.getObjectId()) == null)
			{
				activeChar.sendPacket(ExEnchantFail.STATIC);
				return;
			}

			ItemFunctions.deleteItem(activeChar, item1, 1, true);
			ItemFunctions.deleteItem(activeChar, item2, 1, true);

			if(Rnd.chance(data.getChance()))
			{
				ItemFunctions.addItem(activeChar, data.getSuccessId(), 1, true);
				activeChar.sendPacket(new ExEnchantSucess(data.getSuccessId()));
			}
			else
			{
				ItemFunctions.addItem(activeChar, data.getFailId(), data.getFailCount(), true);
				activeChar.sendPacket(new ExEnchantFail(data.getFailId(), 1));
			}
		}
		finally
		{
			inventory.writeUnlock();
		}
	}
}
