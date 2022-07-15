package l2s.gameserver.network.l2.c2s;

import l2s.commons.ban.BanBindType;
import l2s.gameserver.GameServer;
import l2s.gameserver.Shutdown;
import l2s.gameserver.instancemanager.AuthBanManager;
import l2s.gameserver.instancemanager.GameBanManager;
import l2s.gameserver.network.authcomm.AuthServerClientService;
import l2s.gameserver.network.authcomm.SessionKey;
import l2s.gameserver.network.authcomm.gs2as.PlayerAuthRequest;
import l2s.gameserver.network.l2.GameClient;
import l2s.gameserver.network.l2.s2c.LoginFail;
import l2s.gameserver.network.l2.s2c.ServerClose;
import l2s.gameserver.network.l2.s2c.ServerCloseSocketPacket;
import l2s.gameserver.utils.Language;
import org.strixplatform.StrixPlatform;
import org.strixplatform.logging.Log;
import smartguard.api.ISmartGuardService;
import smartguard.api.integration.SessionData;
import smartguard.core.properties.GuardProperties;
import smartguard.integration.SmartClient;
import smartguard.spi.SmartGuardSPI;

/**
 * cSddddd
 * cSdddddQ
 * loginName + keys must match what the loginserver used.
 */
public class AuthLogin implements IClientIncomingPacket
{
	private String _loginName;
	private int _playKey1;
	private int _playKey2;
	private int _loginKey1;
	private int _loginKey2;
	private int _lang;

	// SmartGuard
	private byte[] data = null;

	@Override
	public boolean readImpl(l2s.gameserver.network.l2.GameClient client, l2s.commons.network.PacketReader packet)
	{
		_loginName = packet.readS(32).toLowerCase();
		_playKey2 = packet.readD();
		_playKey1 = packet.readD();
		_loginKey1 = packet.readD();
		_loginKey2 = packet.readD();
		_lang = packet.readD();
		packet.readD(); // UNK

		// SmartGuard
		if (GuardProperties.ProtectionEnabled)
		{
			packet.readQ(); // unk2

			if(packet.getReadableBytes() > 2) {
				int dataLen = packet.readH();
				if(packet.getReadableBytes() >= dataLen) {
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
		if(GuardProperties.ProtectionEnabled)
		{
			if(data != null)
			{
				ASmartClient smrtclient = new ASmartClient(client, _loginName);
				smrtclient.setSessionData(new SessionData(_playKey2, _playKey1, _loginKey1, _loginKey2));

				ISmartGuardService svc = SmartGuardSPI.getSmartGuardService();
				if (!svc.getSmartGuardBus().checkAuthLogin(smrtclient, data)) {
					smrtclient.closeLater();
					return;
				}
			} else {
				client.close(ServerCloseSocketPacket.STATIC);
				return;
			}
		}

		SessionKey key = new SessionKey(_loginKey1, _loginKey2, _playKey1, _playKey2);
		client.setSessionId(key);
		client.setLoginName(_loginName);
		client.setLanguage(Language.getLanguage(_lang));

		if(Shutdown.getInstance().getMode() != Shutdown.NONE && Shutdown.getInstance().getSeconds() <= 15)
			client.closeNow();
		else
		{
			if(GameServer.getInstance().getAuthServerCommunication().isShutdown())
			{
				client.close(LoginFail.SYSTEM_ERROR_LOGIN_LATER);
				return;
			}
			if(GameBanManager.getInstance().isBanned(BanBindType.LOGIN, client.getLogin()))
			{
				client.close(LoginFail.ACCESS_FAILED_TRY_LATER);
				return;
			}
			if(GameBanManager.getInstance().isBanned(BanBindType.IP, client.getIpAddr()))
			{
				client.close(LoginFail.ACCESS_FAILED_TRY_LATER);
				return;
			}
			if(AuthBanManager.getInstance().isBanned(BanBindType.HWID, client.getHWID()) || GameBanManager.getInstance().isBanned(BanBindType.HWID, client.getHWID()))
			{
				client.close(LoginFail.ACCESS_FAILED_TRY_LATER);
				return;
			}

			GameClient oldClient = AuthServerClientService.INSTANCE.addWaitingClient(client);
			if(oldClient != null)
				oldClient.close(ServerCloseSocketPacket.STATIC);

			GameServer.getInstance().getAuthServerCommunication().sendPacket(new PlayerAuthRequest(client));

			// TODO[K] - Strix section start
			if(StrixPlatform.getInstance().isPlatformEnabled())
			{
				if(client.getStrixClientData() != null)
				{
					client.getStrixClientData().setClientAccount(_loginName);
					if(StrixPlatform.getInstance().isAuthLogEnabled())
					{
						Log.auth("Account: [" + _loginName + "] HWID: [" + client.getStrixClientData().getClientHWID() + "] SessionID: [" + client.getStrixClientData().getSessionId() + "] entered to Game Server");
					}
				}
				else
				{
					client.close(ServerCloseSocketPacket.STATIC);
					return;
				}
			}
			// TODO[K] - Strix section end
		}
	}

	// SmartGuard
	private static class ASmartClient extends SmartClient
	{
		private final String _accountName;

		ASmartClient(GameClient client, String accountName)
		{
			super(client);
			_accountName = accountName;
		}

		@Override
		public String getAccountName()
		{
			return _accountName;
		}
	}

}