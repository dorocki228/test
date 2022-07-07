package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.Config;
import l2s.gameserver.network.l2.GameClient;
import l2s.gameserver.network.l2.s2c.SendStatus;
import l2s.gameserver.network.l2.s2c.ServerCloseSocketPacket;
import l2s.gameserver.network.l2.s2c.VersionCheckPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProtocolVersion extends L2GameClientPacket
{
	private static final Logger _log = LoggerFactory.getLogger(ProtocolVersion.class);

    private static final short BasePacketSize = 4 + 256; //cdbd

	private int _version;

	@Override
	protected void readImpl()
	{
		_version = readD();
		readD();
    }

	@Override
	protected void runImpl()
	{
		GameClient client = getClient();

		if(_version == -2)
		{
			client.closeNow(false);
			return;
		}
		if(_version == -3)
		{
			_log.info("Status request from IP : " + client.getIpAddr());
			client.close(new SendStatus());
			return;
		}
		if(!Config.AVAILABLE_PROTOCOL_REVISIONS.contains(_version))
		{
			_log.warn("Unknown protocol revision : " + _version + ", client : " + client);
            client.close(ServerCloseSocketPacket.STATIC);
			return;
		}

		client.setRevision(_version);
		sendPacket(new VersionCheckPacket(client.enableCrypt()));
	}
}
