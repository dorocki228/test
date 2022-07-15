package l2s.gameserver.network.l2.c2s;

import l2s.commons.dao.JdbcEntityState;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.base.AttributeType;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.ExChangeAttributeFail;
import l2s.gameserver.network.l2.s2c.ExChangeAttributeOk;
import l2s.gameserver.network.l2.s2c.InventoryUpdatePacket;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;

/**
 * @author Bonux
 */
public class RequestChangeAttributeItem implements IClientIncomingPacket
{
	public int _consumeItemId;
	public int _itemObjId;
	public int _newElementId;

	@Override
	public boolean readImpl(l2s.gameserver.network.l2.GameClient client, l2s.commons.network.PacketReader packet)
	{
		_consumeItemId = packet.readD(); //Change Attribute Crystall ID
		_itemObjId = packet.readD(); //Item for Change ObjId
		_newElementId = packet.readD(); //AttributeType
		return true;
	}

	@Override
	public void run(l2s.gameserver.network.l2.GameClient client)
	{
		Player activeChar = client.getActiveChar();
		if(activeChar == null)
			return;

		if(activeChar.isActionsDisabled())
		{
			activeChar.sendActionFailed();
			return;
		}

		if(activeChar.getPrivateStoreType() != Player.STORE_PRIVATE_NONE)
		{
			activeChar.sendPacket(SystemMsg.YOU_CAN_NOT_CHANGE_THE_ATTRIBUTE_WHILE_OPERATING_A_PRIVATE_STORE_OR_PRIVATE_WORKSHOP);
			activeChar.sendPacket(ExChangeAttributeFail.STATIC);
			return;
		}

		if(activeChar.isInTrade())
		{
			activeChar.sendActionFailed();
			return;
		}

		ItemInstance item = activeChar.getInventory().getItemByObjectId(_itemObjId);
		if(item == null || !item.isWeapon())
		{
			activeChar.sendPacket(SystemMsg.UNABLE_TO_CHANCE_THE_ATTRIBUTE);
			activeChar.sendPacket(ExChangeAttributeFail.STATIC);
			return;
		}

		if(!activeChar.getInventory().destroyItemByItemId(_consumeItemId, 1L))
		{
			activeChar.sendActionFailed();
			return;
		}

		AttributeType oldAttributeType = item.getAttackElement();
		int elementVal = item.getAttributeElementValue(oldAttributeType, false);
		item.setAttributeElement(oldAttributeType, 0);

		AttributeType newAttributeType = AttributeType.getElementById(_newElementId);
		item.setAttributeElement(newAttributeType, item.getAttributeElementValue(newAttributeType, false) + elementVal);

		item.setJdbcState(JdbcEntityState.UPDATED);
		item.update();

		activeChar.getInventory().refreshEquip(item);

		SystemMessagePacket msg = new SystemMessagePacket(SystemMsg.IN_THE_ITEM_S1_ATTRIBUTE_S2_SUCCESSFULLY_CHANGED_TO_S3);
		msg.addName(item);
		msg.addElementName(oldAttributeType);
		msg.addElementName(newAttributeType);
		activeChar.sendPacket(msg);
		activeChar.sendPacket(new InventoryUpdatePacket().addModifiedItem(activeChar, item));
		activeChar.sendPacket(ExChangeAttributeOk.STATIC);
		//activeChar.updateStats();
	}
}
