package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.Config;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.s2c.ExChooseCostumeItem;

/**
 * @author Bonux (Head Developer L2-scripts.com)
 * 24.05.2019
 * Developed for L2-Scripts.com
 **/
public class RequestExCostumeUseItem implements IClientIncomingPacket {
	private int itemObjectId;

	@Override
	public boolean readImpl(l2s.gameserver.network.l2.GameClient client, l2s.commons.network.PacketReader packet) {
		itemObjectId = packet.readD();
		return true;
	}

	@Override
	public void run(l2s.gameserver.network.l2.GameClient client) {
		Player activeChar = client.getActiveChar();
		if (activeChar == null)
			return;

		if (Config.EX_COSTUME_DISABLE) {
			activeChar.sendActionFailed();
			return;
		}

		activeChar.setActive();

		ItemInstance item = activeChar.getInventory().getItemByObjectId(itemObjectId);
		if (item == null) {
			activeChar.sendActionFailed();
			return;
		}

		activeChar.sendPacket(new ExChooseCostumeItem(item.getItemId())); // TODO: Нужен ли он здесь?
		activeChar.useItem(item, false, true);
	}
}
