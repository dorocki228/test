package l2s.gameserver.network.authcomm.gs2as;

import io.netty.buffer.ByteBuf;
import l2s.gameserver.network.authcomm.SendablePacket;

public class BonusRequest extends SendablePacket {
    private final String account;
    private final int bonus;
    private final int bonusExpire;

    public BonusRequest(String account, int bonus, int bonusExpire) {
        this.account = account;
        this.bonus = bonus;
        this.bonusExpire = bonusExpire;
    }

    @Override
    protected ByteBuf writeImpl(ByteBuf byteBuf) {
        writeString(byteBuf, account);
        byteBuf.writeInt(bonus);
        byteBuf.writeInt(bonusExpire);
        return byteBuf;
    }

    @Override
    protected byte getOpCode() {
        return 16;
    }
}
