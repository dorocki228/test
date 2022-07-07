package l2s.gameserver.network.authcomm.gs2as;

import io.netty.buffer.ByteBuf;
import l2s.gameserver.network.authcomm.SendablePacket;
import l2s.gameserver.network.l2.GameClient;

public class PlayerAuthRequest extends SendablePacket {
    private final String account;
    private final int playOkID1;
    private final int playOkID2;
    private final int loginOkID1;
    private final int loginOkID2;

    public PlayerAuthRequest(GameClient client) {
        account = client.getLogin();
        playOkID1 = client.getSessionKey().playOkID1;
        playOkID2 = client.getSessionKey().playOkID2;
        loginOkID1 = client.getSessionKey().loginOkID1;
        loginOkID2 = client.getSessionKey().loginOkID2;
    }

    @Override
    protected ByteBuf writeImpl(ByteBuf byteBuf) {
        writeString(byteBuf, account);
        byteBuf.writeInt(playOkID1);
        byteBuf.writeInt(playOkID2);
        byteBuf.writeInt(loginOkID1);
        byteBuf.writeInt(loginOkID2);
        return byteBuf;
    }

    @Override
    protected byte getOpCode() {
        return 2;
    }
}
