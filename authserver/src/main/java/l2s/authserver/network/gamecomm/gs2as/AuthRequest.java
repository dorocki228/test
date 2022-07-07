package l2s.authserver.network.gamecomm.gs2as;

import io.netty.buffer.ByteBuf;
import l2s.authserver.GameServerManager;
import l2s.authserver.network.gamecomm.GameServerDescription;
import l2s.authserver.network.gamecomm.GameServerDescription.HostInfo;
import l2s.authserver.network.gamecomm.ReceivablePacket;
import l2s.authserver.network.gamecomm.as2gs.AuthResponse;
import l2s.authserver.network.gamecomm.as2gs.LoginServerFail;
import l2s.authserver.network.gamecomm.vertx.GameServerConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class AuthRequest extends ReceivablePacket {
    private static final Logger _log = LoggerFactory.getLogger(AuthRequest.class);
    private final int _protocolVersion;
    private final List<HostInfo> _hosts;
    private final int _serverType;
    private final int _ageLimit;
    private final boolean _gmOnly;
    private final boolean _brackets;
    private final boolean _pvp;
    private final int _maxOnline;

    public AuthRequest(GameServerConnection client, ByteBuf byteBuf) {
        super(client, byteBuf);

        _protocolVersion = byteBuf.readInt();
        byteBuf.readUnsignedByte();
        byteBuf.readUnsignedByte();
        _serverType = byteBuf.readInt();
        _ageLimit = byteBuf.readInt();
        _gmOnly = byteBuf.readUnsignedByte() == 1;
        _brackets = byteBuf.readUnsignedByte() == 1;
        _pvp = byteBuf.readUnsignedByte() == 1;
        readString(byteBuf);
        readString(byteBuf);
        for (int ports = byteBuf.readUnsignedShort(), i = 0; i < ports; ++i)
            byteBuf.readUnsignedShort();
        _maxOnline = byteBuf.readInt();
        int hostsCount = byteBuf.readUnsignedByte();
        _hosts = new ArrayList<>(hostsCount);
        for (int j = 0; j < hostsCount; ++j) {
            int id = byteBuf.readUnsignedByte();
            String ip = readString(byteBuf);
            String innerIP = readString(byteBuf);
            int port = byteBuf.readUnsignedShort();
            String key = readString(byteBuf);
            _hosts.add(new HostInfo(id, ip, innerIP, port, key));
        }
    }

    @Override
    protected void runImpl(GameServerConnection client) {
        _log.info("Trying to register gameserver: IP[" + client.getRemoteAddress() + "]");
        GameServerDescription gs = client.getGameServerDescription();
        for (HostInfo host : _hosts)
            if (GameServerManager.getInstance().registerGameServer(host.getId(), client))
                gs.addHost(host);
            else {
                client.sendPacket(new LoginServerFail("Gameserver registration on ID[" + host.getId() + "] failed. ID[" + host.getId() + "] is already in use!", false));
                client.sendPacket(new LoginServerFail("Free ID[" + host.getId() + "] or change to another ID, and restart your authserver or gameserver!", false));
            }
        if (gs.getHosts().length > 0) {
            gs.setProtocol(_protocolVersion);
            gs.setServerType(_serverType);
            gs.setAgeLimit(_ageLimit);
            gs.setGmOnly(_gmOnly);
            gs.setShowingBrackets(_brackets);
            gs.setPvp(_pvp);
            gs.setMaxPlayers(_maxOnline);
            client.setAuthed(true);
            _log.info("Gameserver registration successful.");
            client.sendPacket(new AuthResponse(gs));
            return;
        }
        client.sendPacket(new LoginServerFail("Gameserver registration failed. All ID's is already in use!", true));
        _log.info("Gameserver registration failed.");
    }

}
