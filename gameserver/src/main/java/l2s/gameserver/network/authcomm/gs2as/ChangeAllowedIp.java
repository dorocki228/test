package l2s.gameserver.network.authcomm.gs2as;

import io.netty.buffer.ByteBuf;
import l2s.gameserver.network.authcomm.SendablePacket;

public class ChangeAllowedIp extends SendablePacket {
    private final String account;
    private final String ip;

    public ChangeAllowedIp(String account, String ip) {
        this.account = account;
        this.ip = ip;
    }

    @Override
    protected ByteBuf writeImpl(ByteBuf byteBuf) {
        writeString(byteBuf, account);
        writeString(byteBuf, ip);
        return byteBuf;
    }

    @Override
    protected byte getOpCode() {
        return 7;
    }
}
