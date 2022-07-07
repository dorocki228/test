package l2s.authserver.network.gamecomm.as2gs;

import io.netty.buffer.ByteBuf;
import l2s.authserver.network.gamecomm.SendablePacket;

public class LoginServerFail extends SendablePacket {
    private final String _reason;
    private final boolean _restartConnection;

    public LoginServerFail(String reason, boolean restartConnection) {
        _reason = reason;
        _restartConnection = restartConnection;
    }

    @Override
    protected ByteBuf writeImpl(ByteBuf byteBuf) {
        byteBuf.writeByte(0);
        writeString(byteBuf, _reason);
        byteBuf.writeByte(_restartConnection ? 1 : 0);
        return byteBuf;
    }

    @Override
    protected byte getOpCode() {
        return 1;
    }
}
