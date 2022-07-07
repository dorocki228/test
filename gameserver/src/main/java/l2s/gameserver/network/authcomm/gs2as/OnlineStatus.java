package l2s.gameserver.network.authcomm.gs2as;

import io.netty.buffer.ByteBuf;
import l2s.gameserver.network.authcomm.SendablePacket;

public class OnlineStatus extends SendablePacket {
    private final boolean _online;

    public OnlineStatus(boolean online) {
        _online = online;
    }

    @Override
    protected ByteBuf writeImpl(ByteBuf byteBuf) {
        byteBuf.writeByte(_online ? 1 : 0);
        return byteBuf;
    }

    @Override
    protected byte getOpCode() {
        return 1;
    }
}
