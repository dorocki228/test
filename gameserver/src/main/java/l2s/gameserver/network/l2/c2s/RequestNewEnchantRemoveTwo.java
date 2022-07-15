package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.s2c.ExEnchantTwoRemoveFail;
import l2s.gameserver.network.l2.s2c.ExEnchantTwoRemoveOK;

/**
 * @author Bonux
**/
public class RequestNewEnchantRemoveTwo implements IClientIncomingPacket
{
	private int _item2ObjectId;

	@Override
	public boolean readImpl(l2s.gameserver.network.l2.GameClient client, l2s.commons.network.PacketReader packet)
	{
		_item2ObjectId = packet.readD();
		return true;
	}

	@Override
	public void run(l2s.gameserver.network.l2.GameClient client)
	{
		final Player activeChar = client.getActiveChar();
		if(activeChar == null)
			return;

		final ItemInstance item2 = activeChar.getInventory().getItemByObjectId(_item2ObjectId);
		if(item2 == null)
		{
			activeChar.sendPacket(ExEnchantTwoRemoveFail.STATIC);
			return;
		}

		if(activeChar.getSynthesisItem2() != item2)
		{
			activeChar.sendPacket(ExEnchantTwoRemoveFail.STATIC);
			return;
		}

		activeChar.setSynthesisItem2(null);
		activeChar.sendPacket(ExEnchantTwoRemoveOK.STATIC);
	}
}