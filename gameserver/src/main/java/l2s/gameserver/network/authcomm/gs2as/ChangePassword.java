package l2s.gameserver.network.authcomm.gs2as;

import io.netty.buffer.ByteBuf;
import l2s.gameserver.network.authcomm.SendablePacket;

public class ChangePassword extends SendablePacket {
    private final String _account;
    private final String _oldPass;
    private final String _newPass;
    private final String _hwid;

    public ChangePassword(String account, String oldPass, String newPass, String hwid) {
        _account = account;
        _oldPass = oldPass;
        _newPass = newPass;
        _hwid = hwid;
    }

    @Override
    protected ByteBuf writeImpl(ByteBuf byteBuf) {
        writeString(byteBuf, _account);
        writeString(byteBuf, _oldPass);
        writeString(byteBuf, _newPass);
        writeString(byteBuf, _hwid);
        return byteBuf;
    }

    @Override
    protected byte getOpCode() {
        return 8;
    }
}
