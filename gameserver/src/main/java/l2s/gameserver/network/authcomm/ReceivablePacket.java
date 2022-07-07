package l2s.gameserver.network.authcomm;

import io.netty.buffer.ByteBuf;
import l2s.gameserver.network.authcomm.vertx.AuthServerConnection;

public abstract class ReceivablePacket extends l2s.commons.net.netty.ReceivablePacket<AuthServerConnection> {
    public ReceivablePacket(AuthServerConnection client, ByteBuf byteBuf) {
        super(client, byteBuf);
    }
}
