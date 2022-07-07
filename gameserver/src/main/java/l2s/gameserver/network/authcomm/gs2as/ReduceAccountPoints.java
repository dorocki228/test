package l2s.gameserver.network.authcomm.gs2as;

import io.netty.buffer.ByteBuf;
import l2s.gameserver.network.authcomm.SendablePacket;

public class ReduceAccountPoints extends SendablePacket {
    private final String account;
    private final int count;

    public ReduceAccountPoints(String account, int count) {
        this.account = account;
        this.count = count;
    }

    @Override
    protected ByteBuf writeImpl(ByteBuf byteBuf) {
        writeString(byteBuf, account);
        byteBuf.writeInt(count);
        return byteBuf;
    }

    @Override
    protected byte getOpCode() {
        return 18;
    }
}
