package l2s.authserver.network.gamecomm.gs2as

import io.netty.buffer.ByteBuf
import l2s.authserver.database.DatabaseFactory
import l2s.authserver.network.gamecomm.ReceivablePacket
import l2s.authserver.network.gamecomm.vertx.GameServerConnection
import l2s.commons.dbutils.DbUtils
import org.slf4j.LoggerFactory
import java.sql.Connection
import java.sql.PreparedStatement

/**
 * @Author: SYS
 * @Date: 10/4/2007
 */
class LockAccountIP(client: GameServerConnection, byteBuf: ByteBuf) : ReceivablePacket(client, byteBuf) {

    private val _accname: String = readString(byteBuf)
    private val _IP: String = readString(byteBuf)
    private val _time = byteBuf.readInt()

    override fun runImpl(client: GameServerConnection) {
        var con: Connection? = null
        var statement: PreparedStatement? = null
        try {
            con = DatabaseFactory.getInstance().connection
            statement = con.prepareStatement("UPDATE accounts SET allow_ip = ?, lock_expire = ? WHERE login = ?")
            statement.setString(1, _IP)
            statement.setInt(2, _time)
            statement.setString(3, _accname)
            statement.executeUpdate()
            DbUtils.closeQuietly(statement)
        } catch (e: Exception) {
            _log!!.error("Failed to lock/unlock account: " + e.message)
        } finally {
            DbUtils.closeQuietly(con)
        }
    }

    companion object {
        private val _log = LoggerFactory.getLogger(LockAccountIP::class.java)
    }

}