package l2s.authserver.network.gamecomm.gs2as

import io.netty.buffer.ByteBuf
import l2s.authserver.database.DatabaseFactory
import l2s.authserver.network.gamecomm.ReceivablePacket
import l2s.authserver.network.gamecomm.vertx.GameServerConnection
import org.slf4j.LoggerFactory

class ChangeAllowedHwid(client: GameServerConnection, byteBuf: ByteBuf) : ReceivablePacket(client, byteBuf) {

    private val account: String = readString(byteBuf)
    private val hwid: String = readString(byteBuf)

    override fun runImpl(client: GameServerConnection) {
        DatabaseFactory.getInstance().connection.use { connection ->
            connection.prepareStatement("UPDATE accounts SET allow_hwid=? WHERE login=?").use { statement ->
                statement.setString(1, hwid)
                statement.setString(2, account)
                statement.execute()
            }
        }
    }

    companion object {
        private val _log = LoggerFactory.getLogger(ChangeAllowedHwid::class.java)
    }

}