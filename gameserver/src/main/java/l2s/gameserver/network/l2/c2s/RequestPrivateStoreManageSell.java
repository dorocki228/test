package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.Player;

public class RequestPrivateStoreManageSell implements IClientIncomingPacket
{
	@Override
	public boolean readImpl(l2s.gameserver.network.l2.GameClient client, l2s.commons.network.PacketReader packet)
	{
		// TODO: implement me properly
		// readInt();
		// readLong();
		return true;
	}

	@Override
	public void run(l2s.gameserver.network.l2.GameClient client)
	{
		Player activeChar = client.getActiveChar();
		if(activeChar == null)
			return;

		activeChar.setActive();

		// Player shouldn't be able to set stores if he/she is alike dead (dead or fake death)
		if (activeChar.isAlikeDead() || activeChar.isInOlympiadMode()) {
			activeChar.sendActionFailed();
			return;
		}
	}
}