package l2s.authserver.network.gamecomm.gs2as;

import io.netty.buffer.ByteBuf;
import l2s.authserver.network.gamecomm.ReceivablePacket;
import l2s.authserver.network.gamecomm.vertx.GameServerConnection;

public class OnlineStatus extends ReceivablePacket {
    private final boolean _online;

    public OnlineStatus(GameServerConnection client, ByteBuf byteBuf) {
        super(client, byteBuf);

        _online = byteBuf.readUnsignedByte() == 1;
    }

    @Override
    protected void runImpl(GameServerConnection client) {
        if (!client.isAuthed())
            return;
        client.getGameServerDescription().setOnline(_online);
    }
}
