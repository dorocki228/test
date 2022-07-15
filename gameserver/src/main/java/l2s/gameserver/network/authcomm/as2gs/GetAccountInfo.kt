package l2s.gameserver.network.authcomm.as2gs

import io.netty.buffer.ByteBuf
import l2s.commons.dbutils.DbUtils
import l2s.gameserver.Config
import l2s.gameserver.database.DatabaseFactory
import l2s.gameserver.network.authcomm.ReceivablePacket
import l2s.gameserver.network.authcomm.gs2as.SetAccountInfo
import l2s.gameserver.network.authcomm.vertx.AuthServerConnection
import org.napile.primitive.Containers
import org.napile.primitive.lists.IntList
import org.napile.primitive.lists.impl.ArrayIntList
import org.slf4j.LoggerFactory
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet

/**
 * @author VISTALL
 * @date 21:05/25.03.2011
 */
class GetAccountInfo(client: AuthServerConnection, byteBuf: ByteBuf) : ReceivablePacket(client, byteBuf) {

    private val _account: String = readString(byteBuf)

    override fun runImpl(client: AuthServerConnection) {
        var playerSize = 0
        var deleteChars: IntList? = Containers.EMPTY_INT_LIST
        var con: Connection? = null
        var statement: PreparedStatement? = null
        var rset: ResultSet? = null
        try {
            con = DatabaseFactory.getInstance().connection
            statement = con.prepareStatement("SELECT deletetime FROM characters WHERE account_name=?")
            statement.setString(1, _account)
            rset = statement.executeQuery()
            while (rset.next()) {
                playerSize++
                val d = rset.getInt("deletetime")
                if (d > 0) {
                    if (deleteChars!!.isEmpty) deleteChars = ArrayIntList(3)
                    deleteChars.add(d + Config.CHARACTER_DELETE_AFTER_HOURS * 60 * 60)
                }
            }
        } catch (e: Exception) {
            _log!!.error("GetAccountInfo:runImpl():$e", e)
        } finally {
            DbUtils.closeQuietly(con, statement, rset)
        }

        client.sendPacket(SetAccountInfo(_account, playerSize, deleteChars!!.toArray()))
    }

    companion object {
        private val _log = LoggerFactory.getLogger(GetAccountInfo::class.java)
    }
}