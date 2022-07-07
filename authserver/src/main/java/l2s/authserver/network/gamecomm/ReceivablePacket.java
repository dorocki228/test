package l2s.authserver.network.gamecomm;

import io.netty.buffer.ByteBuf;
import l2s.authserver.network.gamecomm.vertx.GameServerConnection;

public abstract class ReceivablePacket extends l2s.commons.net.netty.ReceivablePacket<GameServerConnection> {
    public ReceivablePacket(GameServerConnection client, ByteBuf byteBuf) {
        super(client, byteBuf);
    }
}
