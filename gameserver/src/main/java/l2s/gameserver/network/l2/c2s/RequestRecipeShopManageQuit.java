package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.Player;

public class RequestRecipeShopManageQuit implements IClientIncomingPacket
{
	@Override
	public boolean readImpl(l2s.gameserver.network.l2.GameClient client, l2s.commons.network.PacketReader packet)
	{
		return true;
	}

	@Override
	public void run(l2s.gameserver.network.l2.GameClient client)
	{
		Player activeChar = client.getActiveChar();
		if(activeChar == null)
			return;

		if(!activeChar.isInStoreMode() || activeChar.getPrivateStoreType() != Player.STORE_PRIVATE_MANUFACTURE)
		{
			activeChar.sendActionFailed();
			return;
		}

		/*TODO[Ertheia]: Fix this.
		activeChar.setPrivateStoreType(Player.STORE_PRIVATE_NONE);
		activeChar.storePrivateStore();
		activeChar.standUp();
		activeChar.broadcastCharInfo();*/
	}
}