package l2s.authserver.network.gamecomm.as2gs;

import io.netty.buffer.ByteBuf;
import l2s.authserver.accounts.Account;
import l2s.authserver.accounts.SessionManager;
import l2s.authserver.network.gamecomm.SendablePacket;
import l2s.authserver.network.l2.SessionKey;

public class PlayerAuthResponse extends SendablePacket {
    private final String login;
    private final boolean authed;
    private int playOkID1;
    private int playOkID2;
    private int loginOkID1;
    private int loginOkID2;
    private int bonus;
    private int bonusExpire;
    private int points;
    private String hwid;

    public PlayerAuthResponse(SessionManager.Session session, boolean authed) {
        Account account = session.getAccount();
        login = account.getLogin();
        this.authed = authed;
        if (authed) {
            SessionKey skey = session.getSessionKey();
            playOkID1 = skey.playOkID1;
            playOkID2 = skey.playOkID2;
            loginOkID1 = skey.loginOkID1;
            loginOkID2 = skey.loginOkID2;
            bonus = account.getBonus();
            bonusExpire = account.getBonusExpire();
            points = account.getPoints();
            hwid = account.getAllowedHwid();
        }
    }

    public PlayerAuthResponse(String account) {
        login = account;
        authed = false;
    }

    @Override
    protected ByteBuf writeImpl(ByteBuf byteBuf) {
        writeString(byteBuf, login);
        byteBuf.writeByte(authed ? 1 : 0);
        if (authed) {
            byteBuf.writeInt(playOkID1);
            byteBuf.writeInt(playOkID2);
            byteBuf.writeInt(loginOkID1);
            byteBuf.writeInt(loginOkID2);
            byteBuf.writeInt(bonus);
            byteBuf.writeInt(bonusExpire);
            byteBuf.writeInt(points);
            writeString(byteBuf, hwid);
        }
        return byteBuf;
    }

    @Override
    protected byte getOpCode() {
        return 2;
    }
}
