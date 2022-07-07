package l2s.gameserver.network.authcomm.gs2as;

import io.netty.buffer.ByteBuf;
import l2s.gameserver.network.authcomm.SendablePacket;

public class PlayerInGame extends SendablePacket {
    private final String account;

    public PlayerInGame(String account) {
        this.account = account;
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
