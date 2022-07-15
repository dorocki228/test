package l2s.gameserver.network.authcomm

import io.netty.buffer.ByteBuf
import l2s.gameserver.network.authcomm.as2gs.*
import l2s.gameserver.network.authcomm.vertx.AuthServerConnection
import org.slf4j.LoggerFactory

object PacketHandler {

    private val logger = LoggerFactory.getLogger(PacketHandler::class.java)

    fun handlePacket(communication: AuthServerConnection, buf: ByteBuf): ReceivablePacket? {
        val id = buf.readUnsignedByte().toInt()

        val packet: ReceivablePacket? = when (id) {
            0x00 -> AuthResponse(communication, buf)
            0x01 -> LoginServerFail(communication, buf)
            0x02 -> PlayerAuthResponse(communication, buf)
            0x03 -> KickPlayer(communication, buf)
            0x04 -> GetAccountInfo(communication, buf)
            0x06 -> ChangePasswordResponse(communication, buf)
            0x07 -> CheckBans(communication, buf)
            else -> null
        }

        if (packet == null) {
            logger.error("Received unknown packet: " + Integer.toHexString(id))
        }

        return packet
    }

}