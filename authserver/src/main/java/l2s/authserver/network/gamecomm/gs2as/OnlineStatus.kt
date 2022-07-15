package l2s.authserver.network.gamecomm.gs2as

import io.netty.buffer.ByteBuf
import l2s.authserver.network.gamecomm.GameServerDescription
import l2s.authserver.network.gamecomm.ReceivablePacket
import l2s.authserver.network.gamecomm.vertx.GameServerConnection

/**
 * @author VISTALL
 * @date 21:40/28.06.2011
 */
class OnlineStatus(client: GameServerConnection, byteBuf: ByteBuf) : ReceivablePacket(client, byteBuf) {

    private val _online = byteBuf.readUnsignedByte() == 1.toShort()

    override fun runImpl(client: GameServerConnection) {
        val gameServerDescription: GameServerDescription = client.description
        if (client.isAuthed) {
            gameServerDescription.isOnline = _online
        }
    }

}