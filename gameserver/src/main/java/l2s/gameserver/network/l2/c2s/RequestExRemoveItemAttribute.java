package l2s.gameserver.network.l2.c2s;

import l2s.commons.dao.JdbcEntityState;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.base.AttributeType;
import l2s.gameserver.model.items.ItemAttributes;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.items.PcInventory;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.ActionFailPacket;
import l2s.gameserver.network.l2.s2c.ExBaseAttributeCancelResult;
import l2s.gameserver.network.l2.s2c.ExShowBaseAttributeCancelWindow;
import l2s.gameserver.network.l2.s2c.InventoryUpdatePacket;

/**
 * @author SYS
 */
public class RequestExRemoveItemAttribute implements IClientIncomingPacket
{
	// Format: chd
	private int _objectId;
	private int _attributeId;

	@Override
	public boolean readImpl(l2s.gameserver.network.l2.GameClient client, l2s.commons.network.PacketReader packet)
	{
		_objectId = packet.readD();
		_attributeId = packet.readD();
		return true;
	}

	@Override
	public void run(l2s.gameserver.network.l2.GameClient client)
	{
		Player activeChar = client.getActiveChar();
		if(activeChar == null)
			return;

		if(activeChar.isActionsDisabled() || activeChar.isInStoreMode() || activeChar.isInTrade())
		{
			activeChar.sendActionFailed();
			return;
		}

		PcInventory inventory = activeChar.getInventory();
		ItemInstance itemToUnnchant = inventory.getItemByObjectId(_objectId);

		if(itemToUnnchant == null)
		{
			activeChar.sendActionFailed();
			return;
		}

		ItemAttributes set = itemToUnnchant.getAttributes();
		AttributeType attributeType = AttributeType.getElementById(_attributeId);

		if(attributeType == AttributeType.NONE || set.getValue(attributeType) <= 0)
		{
			activeChar.sendPacket(new ExBaseAttributeCancelResult(false, itemToUnnchant, attributeType), ActionFailPacket.STATIC);
			return;
		}

		// проверка делается клиентом, если зашло в эту проверку знач чит
		if(!activeChar.reduceAdena(ExShowBaseAttributeCancelWindow.getAttributeRemovePrice(itemToUnnchant), true))
		{
			activeChar.sendPacket(new ExBaseAttributeCancelResult(false, itemToUnnchant, attributeType), SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_ADENA, ActionFailPacket.STATIC);
			return;
		}

		itemToUnnchant.setAttributeElement(attributeType, 0);
		itemToUnnchant.setJdbcState(JdbcEntityState.UPDATED);
		itemToUnnchant.update();

		activeChar.getInventory().refreshEquip(itemToUnnchant);

		activeChar.sendPacket(new InventoryUpdatePacket().addModifiedItem(activeChar, itemToUnnchant));
		activeChar.sendPacket(new ExBaseAttributeCancelResult(true, itemToUnnchant, attributeType));

		//activeChar.updateStats();
	}
}