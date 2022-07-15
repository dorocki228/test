package l2s.authserver.network.gamecomm.gs2as

import io.netty.buffer.ByteBuf
import l2s.authserver.accounts.Account
import l2s.authserver.network.gamecomm.ReceivablePacket
import l2s.authserver.network.gamecomm.vertx.GameServerConnection

class ReduceAccountPoints(client: GameServerConnection, byteBuf: ByteBuf) : ReceivablePacket(client, byteBuf) {

    private val account: String = readString(byteBuf)
    private val count = byteBuf.readInt()

    override fun runImpl(client: GameServerConnection) {
        Account.reducePoints(account, count)
    }

}