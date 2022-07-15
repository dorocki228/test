package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.Player;
import l2s.gameserver.tables.GmListTable;
import smartguard.api.ISmartGuardService;
import smartguard.core.properties.GuardProperties;
import smartguard.integration.SmartClient;
import smartguard.spi.SmartGuardSPI;

public class RequestGmList implements IClientIncomingPacket
{
	// SmartGuard
	private byte[] data = null;

	@Override
	public boolean readImpl(l2s.gameserver.network.l2.GameClient client, l2s.commons.network.PacketReader packet)
	{
		// SmartGuard
		if (GuardProperties.ProtectionEnabled)
		{
			if(packet.getReadableBytes() > 2)
			{
				int dataLen = packet.readH();

				if (packet.getReadableBytes() >= dataLen)
				{
					data = packet.readB(dataLen);
				}
			}
		}

		return true;
	}

	@Override
	public void run(l2s.gameserver.network.l2.GameClient client)
	{
		// SmartGuard
		if(GuardProperties.ProtectionEnabled && data != null)
		{
			ISmartGuardService svc = SmartGuardSPI.getSmartGuardService();
			svc.getSmartGuardBus().onClientData(new SmartClient(client), data);
			return;
		}

		Player activeChar = client.getActiveChar();
		if(activeChar != null)
			GmListTable.sendListToPlayer(activeChar);
	}
}