package l2s.gameserver.network.authcomm.as2gs;

import io.netty.buffer.ByteBuf;
import l2s.gameserver.GameServer;
import l2s.gameserver.network.authcomm.ReceivablePacket;
import l2s.gameserver.network.authcomm.vertx.AuthServerConnection;

public class LoginServerFail extends ReceivablePacket {
    private static final String[] REASONS = {
            "none", "IP banned", "IP reserved", "wrong hexid", "ID reserved", "no free ID", "not authed",
            "already logged in"
    };
    private final String _reason;
    private boolean _restartConnection = true;

    public LoginServerFail(AuthServerConnection client, ByteBuf byteBuf) {
        super(client, byteBuf);

        int reasonId = byteBuf.readUnsignedByte();
        if (!isReadable())
            _reason = "Authserver registration failed! Reason: " + REASONS[reasonId];
        else {
            _reason = readString(byteBuf);
            _restartConnection = byteBuf.readUnsignedByte() > 0;
        }
    }

    @Override
    protected void runImpl(AuthServerConnection client) {
        LOGGER.warn(_reason);
        if (_restartConnection)
            GameServer.getInstance().getAuthServerCommunication().restart();
    }
}
