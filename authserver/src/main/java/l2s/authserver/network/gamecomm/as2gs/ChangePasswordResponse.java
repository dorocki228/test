package l2s.authserver.network.gamecomm.as2gs;

import io.netty.buffer.ByteBuf;
import l2s.authserver.network.gamecomm.SendablePacket;

public class ChangePasswordResponse extends SendablePacket {
    public String _account;
    public boolean _hasChanged;

    public ChangePasswordResponse(String account, boolean hasChanged) {
        _account = account;
        _hasChanged = hasChanged;
    }

    @Override
    protected ByteBuf writeImpl(ByteBuf byteBuf) {
        writeString(byteBuf, _account);
        byteBuf.writeInt(_hasChanged ? 1 : 0);
        return byteBuf;
    }

    @Override
    protected byte getOpCode() {
        return 6;
    }
}
