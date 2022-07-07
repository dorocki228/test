package l2s.authserver.network.gamecomm.as2gs;

import io.netty.buffer.ByteBuf;
import l2s.authserver.network.gamecomm.SendablePacket;

public class GetAccountInfo extends SendablePacket {
    private final String _name;

    public GetAccountInfo(String name) {
        _name = name;
    }

    @Override
    protected ByteBuf writeImpl(ByteBuf byteBuf) {
        writeString(byteBuf, _name);
        return byteBuf;
    }

    @Override
    protected byte getOpCode() {
        return 4;
    }
}
