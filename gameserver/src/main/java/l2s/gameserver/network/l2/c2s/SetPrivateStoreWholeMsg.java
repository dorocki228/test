package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.Config;
import l2s.gameserver.model.Player;
import l2s.gameserver.utils.Util;

public class SetPrivateStoreWholeMsg implements IClientIncomingPacket
{
	private String _storename;

	@Override
	public boolean readImpl(l2s.gameserver.network.l2.GameClient client, l2s.commons.network.PacketReader packet)
	{
		_storename = packet.readS(32);
		return true;
	}

	@Override
	public void run(l2s.gameserver.network.l2.GameClient client)
	{
		Player activeChar = client.getActiveChar();
		if(activeChar == null)
			return;

		if (!Util.isMatchingRegexp(_storename, Config.PRIVATE_STORE_NAME_TEMPLATE)) {
			activeChar.sendMessage("You should use english words in store message.");
			return;
		}

		activeChar.setPackageSellStoreName(_storename);
		activeChar.storePrivateStore();
		activeChar.broadcastPrivateStoreInfo();
	}
}