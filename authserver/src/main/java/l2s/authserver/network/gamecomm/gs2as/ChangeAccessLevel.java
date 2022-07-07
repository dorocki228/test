package l2s.authserver.network.gamecomm.gs2as;

import io.netty.buffer.ByteBuf;
import l2s.authserver.accounts.Account;
import l2s.authserver.network.gamecomm.ReceivablePacket;
import l2s.authserver.network.gamecomm.vertx.GameServerConnection;

public class ChangeAccessLevel extends ReceivablePacket {
    private final String account;
    private final int level;
    private final int banExpire;

    public ChangeAccessLevel(GameServerConnection client, ByteBuf byteBuf) {
        super(client, byteBuf);

        account = readString(byteBuf);
        level = byteBuf.readInt();
        banExpire = byteBuf.readInt();
    }

    @Override
    protected void runImpl(GameServerConnection client) {
        Account acc = new Account(account);
        acc.restore();
        acc.setAccessLevel(level);
        acc.setBanExpire(banExpire);
        acc.update();
    }

}
