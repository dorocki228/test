package l2s.authserver.network.gamecomm.as2gs;

import io.netty.buffer.ByteBuf;
import l2s.authserver.network.gamecomm.SendablePacket;

public class KickPlayer extends SendablePacket {
    private final String account;

    public KickPlayer(String login) {
        account = login;
    }

    @Override
    protected ByteBuf writeImpl(ByteBuf byteBuf) {
        writeString(byteBuf, account);
        return byteBuf;
    }

    @Override
    protected byte getOpCode() {
        return 3;
    }
}
