package l2s.authserver.network.gamecomm.gs2as;

import io.netty.buffer.ByteBuf;
import l2s.authserver.accounts.SessionManager;
import l2s.authserver.accounts.SessionManager.Session;
import l2s.authserver.network.gamecomm.GameServerDescription.HostInfo;
import l2s.authserver.network.gamecomm.ReceivablePacket;
import l2s.authserver.network.gamecomm.vertx.GameServerConnection;
import org.apache.commons.lang3.ArrayUtils;

public class SetAccountInfo extends ReceivablePacket {
    private final String _account;
    private final int _size;
    private final int[] _deleteChars;

    public SetAccountInfo(GameServerConnection client, ByteBuf byteBuf) {
        super(client, byteBuf);

        _account = readString(byteBuf);
        _size = byteBuf.readUnsignedByte();
        int size = byteBuf.readInt();
        if (size > 7 || size <= 0)
            _deleteChars = ArrayUtils.EMPTY_INT_ARRAY;
        else {
            _deleteChars = new int[size];
            for (int i = 0; i < _deleteChars.length; ++i)
                _deleteChars[i] = byteBuf.readInt();
        }
    }

    @Override
    protected void runImpl(GameServerConnection client) {
        if (client.isAuthed()) {
            Session session = SessionManager.getInstance().getSessionByName(_account);
            if (session == null)
                return;
            for (HostInfo host : client.getGameServerDescription().getHosts())
                session.getAccount().addAccountInfo(host.getId(), _size, _deleteChars);
        }
    }
}
