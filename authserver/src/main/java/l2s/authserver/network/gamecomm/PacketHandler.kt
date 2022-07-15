package l2s.authserver.network.gamecomm

import com.google.common.flogger.FluentLogger
import io.netty.buffer.ByteBuf
import l2s.authserver.network.gamecomm.gs2as.*
import l2s.authserver.network.gamecomm.vertx.GameServerConnection

object PacketHandler {

    private val logger = FluentLogger.forEnclosingClass()

    fun handlePacket(gameServerConnection: GameServerConnection, buf: ByteBuf): ReceivablePacket? {
        val id = buf.readUnsignedByte().toInt()
        val packet: ReceivablePacket? =
            if (!gameServerConnection.isAuthed) {
                when (id) {
                    0x00 -> AuthRequest(gameServerConnection, buf)
                    else -> null
                }
            } else {
                when (id) {
                    0x01 -> OnlineStatus(gameServerConnection, buf)
                    0x02 -> PlayerAuthRequest(gameServerConnection, buf)
                    0x03 -> PlayerInGame(gameServerConnection, buf)
                    0x04 -> PlayerLogout(gameServerConnection, buf)
                    0x05 -> SetAccountInfo(gameServerConnection, buf)
                    0x07 -> ChangeAllowedIp(gameServerConnection, buf)
                    0x08 -> ChangePassword(gameServerConnection, buf)
                    0x09 -> ChangeAllowedHwid(gameServerConnection, buf)
                    0x10 -> BonusRequest(gameServerConnection, buf)
                    0x11 -> ChangeAccessLevel(gameServerConnection, buf)
                    0x12 -> ReduceAccountPoints(gameServerConnection, buf)
                    0x13 -> BanRequest(gameServerConnection, buf)
                    0x14 -> UnbanRequest(gameServerConnection, buf)
                    0x0b -> LockAccountIP(gameServerConnection, buf)
                    0x0C -> ChangePhoneNumber(gameServerConnection, buf)
                    else -> null
                }
            }

        if (packet == null) {
            logger.atSevere().log("Received unknown packet: %s", Integer.toHexString(id))
        }

        return packet
    }

}