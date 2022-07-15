package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.Config;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.ExPutCommissionResultForVariationMake;
import l2s.gameserver.templates.item.ItemGrade;
import l2s.gameserver.templates.item.support.variation.VariationFee;
import l2s.gameserver.utils.VariationUtils;

public class RequestConfirmGemStone implements IClientIncomingPacket
{
	// format: (ch)dddd
	private int _targetItemObjId;
	private int _refinerItemObjId;
	private int _feeItemObjectId;
	private long _feeItemCount;

	@Override
	public boolean readImpl(l2s.gameserver.network.l2.GameClient client, l2s.commons.network.PacketReader packet)
	{
		_targetItemObjId = packet.readD();
		_refinerItemObjId = packet.readD();
		_feeItemObjectId = packet.readD();
		_feeItemCount = packet.readQ();
		return true;
	}

	@Override
	public void run(l2s.gameserver.network.l2.GameClient client)
	{
		if(_feeItemCount <= 0)
			return;

		Player activeChar = client.getActiveChar();
		if(activeChar == null)
			return;

		if(!Config.ALLOW_AUGMENTATION)
		{
			activeChar.sendActionFailed();
			return;
		}

		ItemInstance targetItem = activeChar.getInventory().getItemByObjectId(_targetItemObjId);
		ItemInstance refinerItem = activeChar.getInventory().getItemByObjectId(_refinerItemObjId);
		ItemInstance feeItem = activeChar.getInventory().getItemByObjectId(_feeItemObjectId);

		if(targetItem == null || refinerItem == null || feeItem == null)
		{
			activeChar.sendPacket(SystemMsg.THIS_IS_NOT_A_SUITABLE_ITEM);
			return;
		}

		if(!targetItem.canBeAugmented(activeChar))
		{
			activeChar.sendPacket(SystemMsg.THIS_IS_NOT_A_SUITABLE_ITEM);
			return;
		}

		VariationFee fee = VariationUtils.getVariationFee(targetItem, refinerItem);
		if(fee == null)
		{
			activeChar.sendPacket(SystemMsg.THIS_IS_NOT_A_SUITABLE_ITEM);
			return;
		}

		if(fee.getFeeItemId() != feeItem.getItemId())
		{
			activeChar.sendPacket(SystemMsg.THIS_IS_NOT_A_SUITABLE_ITEM);
			return;
		}

		/*if(_feeItemCount != fee.getFeeItemCount())
		{
			activeChar.sendPacket(SystemMsg.GEMSTONE_QUANTITY_IS_INCORRECT);
			return;
		}*/

		activeChar.sendPacket(new ExPutCommissionResultForVariationMake(_feeItemObjectId, fee.getFeeItemCount()), SystemMsg.PRESS_THE_AUGMENT_BUTTON_TO_BEGIN);
	}
}