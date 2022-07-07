package l2s.authserver.network.gamecomm.gs2as;

import io.netty.buffer.ByteBuf;
import l2s.authserver.accounts.Account;
import l2s.authserver.network.gamecomm.ReceivablePacket;
import l2s.authserver.network.gamecomm.vertx.GameServerConnection;

public class BonusRequest extends ReceivablePacket {
    private final String account;
    private final int bonus;
    private final int bonusExpire;

    public BonusRequest(GameServerConnection client, ByteBuf byteBuf) {
        super(client, byteBuf);

        account = readString(byteBuf);
        bonus = byteBuf.readInt();
        bonusExpire = byteBuf.readInt();
    }

    @Override
    protected void runImpl(GameServerConnection client) {
        Account acc = new Account(account);
        acc.restore();
        acc.setBonus(bonus);
        acc.setBonusExpire(bonusExpire);
        acc.update();
    }
}
