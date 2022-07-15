package l2s.authserver.network.gamecomm.gs2as

import io.netty.buffer.ByteBuf
import l2s.authserver.database.DatabaseFactory
import l2s.authserver.network.gamecomm.ReceivablePacket
import l2s.authserver.network.gamecomm.vertx.GameServerConnection

class ChangeAllowedIp(client: GameServerConnection, byteBuf: ByteBuf) : ReceivablePacket(client, byteBuf) {

    private val account: String = readString(byteBuf)
    private val ip: String = readString(byteBuf)

    override fun runImpl(client: GameServerConnection) {
        DatabaseFactory.getInstance().connection.use { connection ->
            connection.prepareStatement("UPDATE accounts SET allow_ip=? WHERE login=?").use { statement ->
                statement.setString(1, ip)
                statement.setString(2, account)
                statement.execute()
            }
        }
    }

}