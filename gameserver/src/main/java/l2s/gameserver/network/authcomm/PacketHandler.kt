package l2s.gameserver.network.authcomm

import io.netty.buffer.ByteBuf
import io.vertx.core.buffer.Buffer
import l2s.gameserver.network.authcomm.as2gs.*
import l2s.gameserver.network.authcomm.vertx.AuthServerConnection
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object PacketHandler {

    private val logger = LoggerFactory.getLogger(PacketHandler::class.java)

    fun handlePacket(communication: AuthServerConnection, buf: ByteBuf): ReceivablePacket? {
        val id = buf.readUnsignedByte().toInt()
        val packet: ReceivablePacket? = when (id) {
            0 -> AuthResponse(communication, buf)
            1 -> LoginServerFail(communication, buf)
            2 -> PlayerAuthResponse(communication, buf)
            3 -> KickPlayer(communication, buf)
            4 -> GetAccountInfo(communication, buf)
            6 -> ChangePasswordResponse(communication, buf)
            7 -> AccountStatisticsResponse(communication, buf)
            8 -> AccountStatisticsResponseFinish(communication, buf)
            else -> null
        }

        if (packet == null) {
            logger.error("Received unknown packet: " + Integer.toHexString(id))
        }

        return packet
    }
}
