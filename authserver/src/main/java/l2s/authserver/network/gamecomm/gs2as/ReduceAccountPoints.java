package l2s.authserver.network.gamecomm.gs2as;

import io.netty.buffer.ByteBuf;
import l2s.authserver.accounts.Account;
import l2s.authserver.network.gamecomm.ReceivablePacket;
import l2s.authserver.network.gamecomm.vertx.GameServerConnection;

public class ReduceAccountPoints extends ReceivablePacket {
    private final String account;
    private final int count;

    public ReduceAccountPoints(GameServerConnection client, ByteBuf byteBuf) {
        super(client, byteBuf);

        account = readString(byteBuf);
        count = byteBuf.readInt();
    }

    @Override
    protected void runImpl(GameServerConnection client) {
        Account.reducePoints(account, count);
    }
}
