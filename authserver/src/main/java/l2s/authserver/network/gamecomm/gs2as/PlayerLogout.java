package l2s.authserver.network.gamecomm.gs2as;

import io.netty.buffer.ByteBuf;
import l2s.authserver.network.gamecomm.ReceivablePacket;
import l2s.authserver.network.gamecomm.vertx.GameServerConnection;

public class PlayerLogout extends ReceivablePacket {
    private final String account;

    public PlayerLogout(GameServerConnection client, ByteBuf byteBuf) {
        super(client, byteBuf);

        account = readString(byteBuf);
    }

    @Override
    protected void runImpl(GameServerConnection client) {
        if (client.isAuthed())
            client.getGameServerDescription().removeAccount(account);
    }
}
