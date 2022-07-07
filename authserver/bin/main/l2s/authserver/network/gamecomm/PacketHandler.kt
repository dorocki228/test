package l2s.authserver.network.gamecomm

import io.netty.buffer.ByteBuf
import io.vertx.core.buffer.Buffer
import l2s.authserver.network.gamecomm.gs2as.*
import l2s.authserver.network.gamecomm.vertx.GameServerConnection
import org.slf4j.LoggerFactory

object PacketHandler {

    private val logger = LoggerFactory.getLogger(PacketHandler::class.java)

    fun handlePacket(gameServerConnection: GameServerConnection, buf: ByteBuf): ReceivablePacket? {
        val id = buf.readUnsignedByte().toInt()
        val packet: ReceivablePacket? = if (!gameServerConnection.isAuthed)
            when (id) {
                0 -> AuthRequest(gameServerConnection, buf)
                else -> null
            }
        else
            when (id) {
                1 -> OnlineStatus(gameServerConnection, buf)
                2 -> PlayerAuthRequest(gameServerConnection, buf)
                3 -> PlayerInGame(gameServerConnection, buf)
                4 -> PlayerLogout(gameServerConnection, buf)
                5 -> SetAccountInfo(gameServerConnection, buf)
                7 -> ChangeAllowedIp(gameServerConnection, buf)
                8 -> ChangePassword(gameServerConnection, buf)
                9 -> ChangeAllowedHwid(gameServerConnection, buf)
                11 -> LockAccountIP(gameServerConnection, buf)
                16 -> BonusRequest(gameServerConnection, buf)
                17 -> ChangeAccessLevel(gameServerConnection, buf)
                18 -> ReduceAccountPoints(gameServerConnection, buf)
                19 -> AccountStatisticsRequest(gameServerConnection, buf)
                else -> null
            }

        if (packet == null) {
            logger.error("Received unknown packet: " + Integer.toHexString(id))
        }

        return packet
    }

}
