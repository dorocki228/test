package l2s.gameserver.network.authcomm.gs2as;

import io.netty.buffer.ByteBuf;
import l2s.gameserver.Config;
import l2s.gameserver.GameServer;
import l2s.gameserver.config.templates.HostInfo;
import l2s.gameserver.config.xml.holder.HostsConfigHolder;
import l2s.gameserver.network.authcomm.SendablePacket;

public class AuthRequest extends SendablePacket {
    @Override
    protected ByteBuf writeImpl(ByteBuf byteBuf) {
        byteBuf.writeInt(2);
        byteBuf.writeByte(Config.REQUEST_ID);
        byteBuf.writeByte(0);
        byteBuf.writeInt(Config.AUTH_SERVER_SERVER_TYPE);
        byteBuf.writeInt(Config.AUTH_SERVER_AGE_LIMIT);
        byteBuf.writeByte(Config.AUTH_SERVER_GM_ONLY ? 1 : 0);
        byteBuf.writeByte(Config.AUTH_SERVER_BRACKETS ? 1 : 0);
        byteBuf.writeByte(Config.AUTH_SERVER_IS_PVP ? 1 : 0);
        writeString(byteBuf, Config.EXTERNAL_HOSTNAME);
        writeString(byteBuf, Config.INTERNAL_HOSTNAME);
        byteBuf.writeShort(1);
        byteBuf.writeShort(Config.PORT_GAME);
        byteBuf.writeInt(GameServer.getInstance().getOnlineLimit());
        HostInfo[] hosts = HostsConfigHolder.getInstance().getGameServerHosts();
        byteBuf.writeByte(hosts.length);
        for (HostInfo host : hosts) {
            byteBuf.writeByte(host.getId());
            writeString(byteBuf, host.getIP());
            writeString(byteBuf, host.getInnerIP());
            byteBuf.writeShort(host.getPort());
            writeString(byteBuf, host.getKey());
        }
        return byteBuf;
    }

    @Override
    protected byte getOpCode() {
        return 0;
    }
}
