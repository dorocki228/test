package l2s.gameserver.network.authcomm.gs2as

import io.netty.buffer.ByteBuf
import l2s.commons.net.HostInfo
import l2s.gameserver.Config
import l2s.gameserver.GameServer
import l2s.gameserver.config.xml.holder.HostsConfigHolder
import l2s.gameserver.network.authcomm.SendablePacket

class AuthRequest : SendablePacket() {

    override val opCode: Byte = 0x00

    override fun writeImpl(byteBuf: ByteBuf): ByteBuf {
        byteBuf.writeInt(GameServer.AUTH_SERVER_PROTOCOL)
        byteBuf.writeInt(Config.AUTH_SERVER_SERVER_TYPE)
        byteBuf.writeInt(Config.AUTH_SERVER_AGE_LIMIT)
        byteBuf.writeByte(if (Config.AUTH_SERVER_GM_ONLY) 0x01 else 0x00)
        byteBuf.writeByte(if (Config.AUTH_SERVER_BRACKETS) 0x01 else 0x00)
        byteBuf.writeByte(if (Config.AUTH_SERVER_IS_PVP) 0x01 else 0x00)
        byteBuf.writeInt(GameServer.getInstance().onlineLimit)
        val hosts: Array<HostInfo> =
            HostsConfigHolder.getInstance().gameServerHosts
        byteBuf.writeByte(hosts.size)
        for (host in hosts) {
            byteBuf.writeByte(host.id)
            writeString(byteBuf, host.address)
            byteBuf.writeShort(host.port)
            writeString(byteBuf, host.key)
            byteBuf.writeByte(host.subnets.size)
            for ((key, value) in host.subnets) {
                writeString(byteBuf, value)
                val address = key.address
                byteBuf.writeInt(address.size)
                byteBuf.writeBytes(address)
                val mask = key.mask
                byteBuf.writeInt(mask.size)
                byteBuf.writeBytes(mask)
            }
        }

        return byteBuf
    }

}