package l2s.gameserver.network.authcomm.as2gs;

import io.netty.buffer.ByteBuf;
import l2s.gameserver.network.authcomm.AuthServerClientService;
import l2s.gameserver.network.authcomm.ReceivablePacket;
import l2s.gameserver.network.authcomm.gs2as.OnlineStatus;
import l2s.gameserver.network.authcomm.gs2as.PlayerInGame;
import l2s.gameserver.network.authcomm.vertx.AuthServerConnection;

import java.util.ArrayList;
import java.util.List;

public class AuthResponse extends ReceivablePacket {
    private final List<ServerInfo> _servers;

    public AuthResponse(AuthServerConnection client, ByteBuf byteBuf) {
        super(client, byteBuf);

        int serverId = byteBuf.readUnsignedByte();
        String serverName = readString(byteBuf);
        if (!isReadable()) {
            _servers = new ArrayList<>(1);
            _servers.add(new ServerInfo(serverId, serverName));
        } else {
            int serversCount = byteBuf.readUnsignedByte();
            _servers = new ArrayList<>(serversCount);
            for (int i = 0; i < serversCount; ++i)
                _servers.add(new ServerInfo(byteBuf.readUnsignedByte(), readString(byteBuf)));
        }
    }

    @Override
    protected void runImpl(AuthServerConnection client) {
        client.setAuthed(true);

        for (ServerInfo info : _servers)
            LOGGER.info("Registered on authserver as {} [{}]", info.getId(), info.getName());
        client.sendPacket(new OnlineStatus(true));

        String[] accounts = AuthServerClientService.INSTANCE.getAccounts();
        for (String account : accounts)
            client.sendPacket(new PlayerInGame(account));
    }

    private static class ServerInfo {
        private final int _id;
        private final String _name;

        public ServerInfo(int id, String name) {
            _id = id;
            _name = name;
        }

        public int getId() {
            return _id;
        }

        public String getName() {
            return _name;
        }
    }
}
