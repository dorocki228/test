package l2s.authserver.network.gamecomm.gs2as

import io.netty.buffer.ByteBuf
import l2s.authserver.Config
import l2s.authserver.database.DatabaseFactory
import l2s.authserver.network.gamecomm.ReceivablePacket
import l2s.authserver.network.gamecomm.as2gs.ChangePasswordResponse
import l2s.authserver.network.gamecomm.vertx.GameServerConnection
import l2s.commons.dbutils.DbUtils
import org.slf4j.LoggerFactory
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet

class ChangePassword(client: GameServerConnection, byteBuf: ByteBuf) : ReceivablePacket(client, byteBuf) {

    private val _accname: String = readString(byteBuf)
    private val _oldPass: String = readString(byteBuf)
    private val _newPass: String = readString(byteBuf)
    private val _hwid: String = readString(byteBuf)

    override fun runImpl(client: GameServerConnection) {
        var dbPassword: String? = null
        var con: Connection? = null
        var statement: PreparedStatement? = null
        var rs: ResultSet? = null
        try {
            con = DatabaseFactory.getInstance().connection
            try {
                statement = con.prepareStatement("SELECT * FROM accounts WHERE login = ?")
                statement.setString(1, _accname)
                rs = statement.executeQuery()
                if (rs.next()) dbPassword = rs.getString("password")
            } catch (e: Exception) {
                _log!!.warn("Can't recive old password for account $_accname, exciption :$e")
            } finally {
                DbUtils.closeQuietly(statement, rs)
            }

            //Encode old password and compare it to sended one, send packet to determine changed or not.


            try {
                if (!Config.DEFAULT_CRYPT.compare(_oldPass, dbPassword)) {
                    val cp1 = ChangePasswordResponse(_accname, false)
                    client.sendPacket(cp1)
                } else {
                    statement = con.prepareStatement("UPDATE accounts SET password = ? WHERE login = ?")
                    statement.setString(1, Config.DEFAULT_CRYPT.encrypt(_newPass))
                    statement.setString(2, _accname)
                    val result = statement.executeUpdate()
                    val cp1 = ChangePasswordResponse(_accname, result != 0)
                    client.sendPacket(cp1)
                }
            } catch (e1: Exception) {
                e1.printStackTrace()
            } finally {
                DbUtils.closeQuietly(statement)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            DbUtils.closeQuietly(con)
        }
    }

    companion object {
        private val _log = LoggerFactory.getLogger(ChangePassword::class.java)
    }

}