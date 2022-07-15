package l2s.authserver.network.gamecomm.gs2as

import io.netty.buffer.ByteBuf
import l2s.authserver.accounts.Account
import l2s.authserver.network.gamecomm.ReceivablePacket
import l2s.authserver.network.gamecomm.vertx.GameServerConnection

class BonusRequest(client: GameServerConnection, byteBuf: ByteBuf) : ReceivablePacket(client, byteBuf) {

    private val account: String = readString(byteBuf)
    private val bonus = byteBuf.readInt()
    private val bonusExpire = byteBuf.readInt()

    override fun runImpl(client: GameServerConnection) {
        val acc = Account(account)
        acc.restore()
        acc.bonus = bonus
        acc.bonusExpire = bonusExpire
        acc.update()
    }

}