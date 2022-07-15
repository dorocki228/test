package l2s.authserver.network.gamecomm.gs2as

import io.netty.buffer.ByteBuf
import l2s.authserver.AuthBanManager
import l2s.authserver.network.gamecomm.ReceivablePacket
import l2s.authserver.network.gamecomm.vertx.GameServerConnection
import l2s.commons.ban.BanBindType

/**
 * @author Bonux (Head Developer L2-scripts.com)
 * 10.04.2019
 * Developed for L2-Scripts.com
 */
class UnbanRequest(client: GameServerConnection, byteBuf: ByteBuf) : ReceivablePacket(client, byteBuf) {

    private val bindType: BanBindType
    private val bindValue: String

    init {
        try {
            bindType = BanBindType.VALUES[byteBuf.readUnsignedByte().toInt()]
        } catch (e: Exception) {
            throw IllegalArgumentException("Can't find ban type", e)
        }
        bindValue = readString(byteBuf)
    }

    override fun runImpl(client: GameServerConnection) {
        AuthBanManager.getInstance().removeBan(bindType, bindValue)
    }

}