package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.Config;
import l2s.gameserver.model.Player;
import l2s.gameserver.utils.Util;

public class RequestRecipeShopMessageSet implements IClientIncomingPacket
{
	// format: cS
	private String _name;

	@Override
	public boolean readImpl(l2s.gameserver.network.l2.GameClient client, l2s.commons.network.PacketReader packet)
	{
		_name = packet.readS(16);
		return true;
	}

	@Override
	public void run(l2s.gameserver.network.l2.GameClient client)
	{
		Player activeChar = client.getActiveChar();
		if(activeChar == null)
			return;

		if (!Util.isMatchingRegexp(_name, Config.PRIVATE_STORE_NAME_TEMPLATE)) {
			activeChar.sendMessage("You should use english words in store message.");
			return;
		}

		activeChar.setManufactureName(_name);
		activeChar.storePrivateStore();
		activeChar.broadcastPrivateStoreInfo();
	}
}