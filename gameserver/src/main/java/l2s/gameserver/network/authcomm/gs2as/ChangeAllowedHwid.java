package l2s.gameserver.network.authcomm.gs2as;

import io.netty.buffer.ByteBuf;
import l2s.gameserver.network.authcomm.SendablePacket;
import l2s.gameserver.network.l2.components.hwid.HwidHolder;

public class ChangeAllowedHwid extends SendablePacket {
    private final String account;
    private final String hwid;

    public ChangeAllowedHwid(String account, HwidHolder hwid) {
        this.account = account;
        this.hwid = hwid.asString();
    }

    public ChangeAllowedHwid(String account, String hwid) {
        this.account = account;
        this.hwid = hwid;
    }

    @Override
    protected ByteBuf writeImpl(ByteBuf byteBuf) {
        writeString(byteBuf, account);
        writeString(byteBuf, hwid);
        return byteBuf;
    }

    @Override
    protected byte getOpCode() {
        return 9;
    }
}
