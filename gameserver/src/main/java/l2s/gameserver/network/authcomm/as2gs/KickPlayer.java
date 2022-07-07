package l2s.gameserver.network.authcomm.as2gs;

import io.netty.buffer.ByteBuf;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.authcomm.AuthServerClientService;
import l2s.gameserver.network.authcomm.ReceivablePacket;
import l2s.gameserver.network.authcomm.vertx.AuthServerConnection;
import l2s.gameserver.network.l2.GameClient;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.ServerCloseSocketPacket;

public class KickPlayer extends ReceivablePacket {
    private final String account;

    public KickPlayer(AuthServerConnection client, ByteBuf byteBuf) {
        super(client, byteBuf);

        account = readString(byteBuf);
    }

    @Override
    protected void runImpl(AuthServerConnection client) {
        GameClient gameClient = AuthServerClientService.INSTANCE.removeWaitingClient(account);
        if (gameClient == null)
            gameClient = AuthServerClientService.INSTANCE.removeAuthedClient(account);
        if (gameClient == null)
            return;
        Player activeChar = gameClient.getActiveChar();
        if (activeChar != null) {
            activeChar.sendPacket(SystemMsg.ANOTHER_PERSON_HAS_LOGGED_IN_WITH_THE_SAME_ACCOUNT);
            activeChar.kick();
        } else
            gameClient.close(ServerCloseSocketPacket.STATIC);
    }
}
