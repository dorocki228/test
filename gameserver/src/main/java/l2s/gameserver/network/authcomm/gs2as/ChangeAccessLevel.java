package l2s.gameserver.network.authcomm.gs2as;

import io.netty.buffer.ByteBuf;
import l2s.gameserver.network.authcomm.SendablePacket;

public class ChangeAccessLevel extends SendablePacket {
    private final String account;
    private final int level;
    private final int banExpire;
    private final String reason;

    public ChangeAccessLevel(String account, int level, int banExpire, String reason) {
        this.account = account;
        this.level = level;
        this.banExpire = banExpire;
        this.reason = reason;
    }

    @Override
    protected ByteBuf writeImpl(ByteBuf byteBuf) {
        writeString(byteBuf, account);
        byteBuf.writeInt(level);
        byteBuf.writeInt(banExpire);
        writeString(byteBuf, reason);
        return byteBuf;
    }

    @Override
    protected byte getOpCode() {
        return 0x11;
    }
}
