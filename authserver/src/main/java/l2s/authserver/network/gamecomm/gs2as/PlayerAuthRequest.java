package l2s.authserver.network.gamecomm.gs2as;

import io.netty.buffer.ByteBuf;
import l2s.authserver.accounts.SessionManager;
import l2s.authserver.accounts.SessionManager.Session;
import l2s.authserver.network.gamecomm.ReceivablePacket;
import l2s.authserver.network.gamecomm.as2gs.PlayerAuthResponse;
import l2s.authserver.network.gamecomm.vertx.GameServerConnection;
import l2s.authserver.network.l2.SessionKey;

public class PlayerAuthRequest extends ReceivablePacket {
    private final String account;
    private final int playOkId1;
    private final int playOkId2;
    private final int loginOkId1;
    private final int loginOkId2;

    public PlayerAuthRequest(GameServerConnection client, ByteBuf byteBuf) {
        super(client, byteBuf);

        account = readString(byteBuf);
        playOkId1 = byteBuf.readInt();
        playOkId2 = byteBuf.readInt();
        loginOkId1 = byteBuf.readInt();
        loginOkId2 = byteBuf.readInt();
    }

    @Override
    protected void runImpl(GameServerConnection client) {
        SessionKey skey = new SessionKey(loginOkId1, loginOkId2, playOkId1, playOkId2);
        Session session = SessionManager.getInstance().closeSession(skey);
        if (session == null || !session.getAccount().getLogin().equals(account)) {
            client.sendPacket(new PlayerAuthResponse(account));
            return;
        }
        client.sendPacket(new PlayerAuthResponse(session, session.getSessionKey().equals(skey)));
    }
}
