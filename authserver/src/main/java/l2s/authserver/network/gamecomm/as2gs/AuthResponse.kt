package l2s.authserver.network.gamecomm.as2gs

import io.netty.buffer.ByteBuf
import l2s.authserver.Config
import l2s.authserver.network.gamecomm.GameServerDescription
import l2s.authserver.network.gamecomm.SendablePacket
import l2s.commons.net.HostInfo

/**
 * @reworked by Bonux
 */
class AuthResponse(gs: GameServerDescription) : SendablePacket() {

    private val hosts: Collection<HostInfo> = gs.hosts

    override val opCode: Byte = 0x00

    override fun writeImpl(byteBuf: ByteBuf): ByteBuf {
        byteBuf.writeByte(0x00) // ServerId

        writeString(byteBuf, "") // ServerName

        byteBuf.writeByte(hosts.size)
        for (host in hosts) {
            byteBuf.writeByte(host.id)
            val name = Config.SERVER_NAMES[host.id]
            if (name != null)
                writeString(byteBuf, name)
            else
                logger.atSevere().log("Cant find server name for id %d", host.id)
        }

        return byteBuf
    }

}