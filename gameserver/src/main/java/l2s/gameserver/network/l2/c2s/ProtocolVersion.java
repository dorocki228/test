package l2s.gameserver.network.l2.c2s;

import com.google.common.flogger.FluentLogger;
import l2s.gameserver.Config;
import l2s.gameserver.network.l2.s2c.SendStatus;
import l2s.gameserver.network.l2.s2c.ServerCloseSocketPacket;
import org.strixplatform.StrixPlatform;
import org.strixplatform.managers.ClientGameSessionManager;
import org.strixplatform.managers.ClientProtocolDataManager;
import org.strixplatform.utils.StrixClientData;
import smartguard.core.properties.GuardProperties;
import smartguard.packet.VersionCheckPacket;

import static com.google.common.flogger.LazyArgs.lazy;

/**
 * packet type id 0x0E
 * format:	cdbd
 */
public class ProtocolVersion implements IClientIncomingPacket
{
	private static final FluentLogger _log = FluentLogger.forEnclosingClass();

	private int _version;

	// SmartGuard
	private boolean hasExtraData = false;

	//TODO[K] - Guard section start
	private byte[] data;
	private int dataChecksum;
	//TODO[K] - Guard section end

	@Override
	public boolean readImpl(l2s.gameserver.network.l2.GameClient client, l2s.commons.network.PacketReader packet)
	{
		_version = packet.readD();
		if(!StrixPlatform.getInstance().isPlatformEnabled()) {
			packet.readD(); // account id
			packet.readB(256); // rsa modulus
			packet.readD(); // unk
		}

		// SmartGuard
		if (GuardProperties.ProtectionEnabled)
		{
			int dataLen = packet.readH();
			if (packet.getReadableBytes() >= dataLen) {
				hasExtraData = true;
			}
		}

		//TODO[K] - Guard section start
		if(StrixPlatform.getInstance().isPlatformEnabled())
		{
			try
			{
				if(packet.getReadableBytes() >= StrixPlatform.getInstance().getProtocolVersionDataSize())
				{
					data = packet.readB(StrixPlatform.getInstance().getClientDataSize());
					dataChecksum = packet.readD();
				}
			}
			catch(final Exception e)
			{
				_log.atFine().log("Client [IP=%s] used unprotected client. Disconnect...", client.getIpAddr());
				client.close(new VersionCheckPacket(null));
				return false;
			}
		}
		//TODO[K] - Guard section end

		return true;
	}

	@Override
	public void run(l2s.gameserver.network.l2.GameClient client)
	{
		if(_version == -2)
		{
			client.closeNow();
			return;
		}
		else if(_version == -3)
		{
			_log.atInfo().log( "Status request from IP : %s", lazy(() -> client.getIpAddr()) );
			client.close(new SendStatus());
			return;
		}
		else if(!Config.AVAILABLE_PROTOCOL_REVISIONS.contains(_version))
		{
			_log.atWarning().log( "Unknown protocol revision : %s, client : %s", _version, client );
			// SmartGuard
			if(GuardProperties.ProtectionEnabled)
				client.close(new VersionCheckPacket(null));
			else
				client.close(new l2s.gameserver.network.l2.s2c.VersionCheckPacket(null));
			return;
		}

		//TODO[K] - Strix section start
		if (StrixPlatform.getInstance().isPlatformEnabled()) {
			if(data == null)
			{
				_log.atFine().log("Client [IP=%s] used unprotected client. Disconnect...", client.getIpAddr());
				client.close(new l2s.gameserver.network.l2.s2c.VersionCheckPacket(null));
				return;
			}
			else
			{
				final StrixClientData clientData = ClientProtocolDataManager.getInstance().getDecodedData(data, dataChecksum);
				if(clientData != null)
				{
					if(!ClientGameSessionManager.getInstance().checkServerResponse(clientData))
					{
						client.close(new l2s.gameserver.network.l2.s2c.VersionCheckPacket(null, clientData));
						return;
					}
					client.setStrixClientData(clientData);
					client.setRevision(_version);
					client.sendPacket(new l2s.gameserver.network.l2.s2c.VersionCheckPacket(client.enableCrypt()));
					return;
				}
				_log.atFine().log("Decode client data failed. See Strix-Platform log file. Disconected client " + client.getIpAddr());
				client.close(new l2s.gameserver.network.l2.s2c.VersionCheckPacket(null));
			}
			return;
		}
		//TODO[K] - Strix section end

		// SmartGuard
		// если защита включена, производим дополнительные проверки
		if(GuardProperties.ProtectionEnabled && !hasExtraData)
		{
			// если нету доп. данных - попытка входа без защиты
			client.close(ServerCloseSocketPacket.STATIC);
			return;
		}

		client.setRevision(_version);
		// SmartGuard
		if(GuardProperties.ProtectionEnabled)
			client.sendPacket(new VersionCheckPacket(client.enableCrypt()));
		else
			client.sendPacket(new l2s.gameserver.network.l2.s2c.VersionCheckPacket(client.enableCrypt()));
	}
}