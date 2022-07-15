package l2s.authserver.network.gamecomm.gs2as

import io.netty.buffer.ByteBuf
import l2s.authserver.database.DatabaseFactory
import l2s.authserver.network.gamecomm.ReceivablePacket
import l2s.authserver.network.gamecomm.vertx.GameServerConnection
import org.slf4j.LoggerFactory
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.SQLException

/**
 * @author Bonux
 */
class ChangePhoneNumber(client: GameServerConnection, byteBuf: ByteBuf) : ReceivablePacket(client, byteBuf) {

    private val account: String = readString(byteBuf)
    private val phoneNumber: Long = byteBuf.readLong()

    override fun runImpl(client: GameServerConnection) {
        var con: Connection? = null
        var statement: PreparedStatement? = null
        try {
            con = DatabaseFactory.getInstance().connection
            statement = con.prepareStatement("UPDATE accounts SET phone_nubmer=? WHERE login=?")
            statement.setLong(1, phoneNumber)
            statement.setString(2, account)
            statement.execute()
            statement.close()
        } catch (e: SQLException) {
            _log!!.warn("ChangePhoneNumber: Could not write data. Reason: $e")
        } finally {
            try {
                con?.close()
            } catch (e: SQLException) {
                e.printStackTrace()
            }
        }
    }

    companion object {
        private val _log = LoggerFactory.getLogger(ChangePhoneNumber::class.java)
    }

}