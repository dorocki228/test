package l2s.gameserver.network.l2.c2s;

import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import l2s.gameserver.GameServer;
import l2s.gameserver.Shutdown;
import l2s.gameserver.logging.LogService;
import l2s.gameserver.logging.LoggerType;
import l2s.gameserver.network.authcomm.AuthServerClientService;
import l2s.gameserver.network.authcomm.SessionKey;
import l2s.gameserver.network.authcomm.gs2as.PlayerAuthRequest;
import l2s.gameserver.network.authcomm.vertx.AuthServerCommunication;
import l2s.gameserver.network.l2.GameClient;
import l2s.gameserver.network.l2.components.hwid.DefaultHwidHolder;
import l2s.gameserver.network.l2.components.hwid.EmptyHwidHolder;
import l2s.gameserver.network.l2.s2c.LoginResultPacket;
import l2s.gameserver.network.l2.s2c.ServerCloseSocketPacket;
import l2s.gameserver.utils.Language;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.message.SimpleMessage;

import java.nio.BufferUnderflowException;

public class AuthLogin extends L2GameClientPacket
{
	private static final int HWID_LENGTH = 32;

	private String _loginName;
	private int _playKey1;
	private int _playKey2;
	private int _loginKey1;
	private int _loginKey2;
	private int _lang;

	private byte[] hwid = ArrayUtils.EMPTY_BYTE_ARRAY;

	private String hexDump;

	@Override
	protected void readImpl()
	{
		hexDump = ByteBufUtil.prettyHexDump(Unpooled.wrappedBuffer(getByteBuffer()));
		SimpleMessage message = new SimpleMessage(getClient() + " AuthLogin hexdump:\n" + hexDump);
		LogService.getInstance().log(LoggerType.DEBUG, message);

		_loginName = readS(32).toLowerCase();
		_playKey2 = readD();
		_playKey1 = readD();
		_loginKey1 = readD();
		_loginKey2 = readD();
		_lang = readD();
		readQ();
		readD();

        if (!GameServer.DEVELOP && getAvaliableBytes() >= HWID_LENGTH) {
            try {
				hwid = new byte[HWID_LENGTH];
				readB(hwid, getAvaliableBytes() - HWID_LENGTH, HWID_LENGTH);
            } catch (BufferUnderflowException ex) {
                getClient().closeNow(true);
                return;
            }
        }
    }

	@Override
	protected void runImpl()
	{
		if (!GameServer.DEVELOP && hwid.length == 0) {
			SimpleMessage message = new SimpleMessage(getClient() + " have no hwid. AuthLogin hexdump:\n" + hexDump);
			LogService.getInstance().log(LoggerType.DEBUG, message);
			getClient().closeNow(true);
			return;
		}

		if(_client == null)
			return;

		GameClient client = getClient();
		SessionKey key = new SessionKey(_loginKey1, _loginKey2, _playKey1, _playKey2);
		client.setSessionId(key);
		client.setLoginName(_loginName);
		client.setLanguage(Language.getLanguage(_lang));
		if (GameServer.DEVELOP) {
			client.setHwidHolder(new EmptyHwidHolder(_loginName));
		} else {
			client.setHwidHolder(new DefaultHwidHolder(hwid));
		}

		if(Shutdown.getInstance().getMode() != -1 && Shutdown.getInstance().getSeconds() <= 15)
			client.closeNow(false);
		else
		{
			AuthServerCommunication authServerCommunication = GameServer.getInstance().getAuthServerCommunication();
			if(authServerCommunication.isShutdown())
			{
				client.close(new LoginResultPacket(LoginResultPacket.SYSTEM_ERROR_LOGIN_LATER));
				return;
			}
			GameClient oldClient = AuthServerClientService.INSTANCE.addWaitingClient(client);
			if(oldClient != null) {
				oldClient.close(ServerCloseSocketPacket.STATIC);
			}
			authServerCommunication.sendPacket(new PlayerAuthRequest(client));
		}
	}
}
