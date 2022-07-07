package l2s.gameserver.network.authcomm.gs2as;

import io.netty.buffer.ByteBuf;
import l2s.gameserver.network.authcomm.SendablePacket;

import java.util.List;

public class SetAccountInfo extends SendablePacket {
    private final String _account;
    private final int _size;
    private final List<Integer> _deleteChars;

    public SetAccountInfo(String account, int size, List<Integer> deleteChars) {
        _account = account;
        _size = size;
        _deleteChars = deleteChars;
    }

    @Override
    protected ByteBuf writeImpl(ByteBuf byteBuf) {
        writeString(byteBuf, _account);
        byteBuf.writeByte(_size);
        byteBuf.writeInt(_deleteChars.size());
        _deleteChars.forEach(byteBuf::writeInt);
        return byteBuf;
    }

    @Override
    protected byte getOpCode() {
        return 5;
    }
}
