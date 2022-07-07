package l2s.gameserver.network.authcomm.gs2as;

import io.netty.buffer.ByteBuf;
import l2s.gameserver.network.authcomm.SendablePacket;

public class LockAccountIP extends SendablePacket {
    private final String _account;
    private final String _IP;
    private final int _time;

    public LockAccountIP(String account, String IP, int time) {
        _account = account;
        _IP = IP;
        _time = time;
    }

    @Override
    protected ByteBuf writeImpl(ByteBuf byteBuf) {
        writeString(byteBuf, _account);
        writeString(byteBuf, _IP);
        byteBuf.writeInt(_time);
        return byteBuf;
    }

    @Override
    protected byte getOpCode() {
        return 11;
    }
}
