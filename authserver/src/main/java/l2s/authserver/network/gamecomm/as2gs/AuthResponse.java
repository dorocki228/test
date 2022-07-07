package l2s.authserver.network.gamecomm.as2gs;

import io.netty.buffer.ByteBuf;
import l2s.authserver.Config;
import l2s.authserver.network.gamecomm.GameServerDescription;
import l2s.authserver.network.gamecomm.GameServerDescription.HostInfo;
import l2s.authserver.network.gamecomm.SendablePacket;

public class AuthResponse extends SendablePacket {
    private final HostInfo[] _hosts;

    public AuthResponse(GameServerDescription gs) {
        _hosts = gs.getHosts();
    }

    @Override
    protected ByteBuf writeImpl(ByteBuf byteBuf) {
        byteBuf.writeByte(0);
        writeString(byteBuf, "");
        byteBuf.writeByte(_hosts.length);
        for (HostInfo host : _hosts) {
            byteBuf.writeByte(host.getId());
            writeString(byteBuf, Config.SERVER_NAMES.get(host.getId()));
        }
        return byteBuf;
    }

    @Override
    protected byte getOpCode() {
        return 0;
    }
}
