package l2s.authserver.network.gamecomm.gs2as

import io.netty.buffer.ByteBuf
import l2s.authserver.network.gamecomm.GameServerDescription
import l2s.authserver.network.gamecomm.ReceivablePacket
import l2s.authserver.network.gamecomm.vertx.GameServerConnection

class PlayerInGame(client: GameServerConnection, byteBuf: ByteBuf) : ReceivablePacket(client, byteBuf) {

    private val account: String = readString(byteBuf)

    override fun runImpl(client: GameServerConnection) {
        val gs: GameServerDescription = client.description
        if (client.isAuthed)
            gs.addAccount(account)
    }

}