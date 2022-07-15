package l2s.authserver.network.gamecomm

import com.google.common.flogger.FluentLogger
import l2s.authserver.database.DatabaseFactory
import l2s.commons.dbutils.DbUtils
import l2s.commons.net.HostInfo
import java.sql.Connection
import java.sql.PreparedStatement
import java.util.concurrent.ConcurrentHashMap

/**
 * @reworked by Bonux
 */
class GameServerDescription {

    private val hostMap: MutableMap<Int, HostInfo> = HashMap()
    var serverType = 0
    var ageLimit = 0
    var protocol = 0
    var isOnline = false
    var isPvp = false
    var isShowingBrackets = false
    var isGmOnly = false
    var maxPlayers = 0
    private var _accounts: MutableSet<String> = ConcurrentHashMap.newKeySet()

    constructor() {
    }

    constructor(id: Int, ip: String?, port: Int, key: String?) {
        addHost(HostInfo(id, ip, port, key))
    }

    fun addHost(host: HostInfo) {
        hostMap.put(host.id, host)
    }

    fun removeHost(id: Int): HostInfo? {
        return hostMap.remove(id)
    }

    fun getHost(id: Int): HostInfo? {
        return hostMap.get(id)
    }

    val hosts: Collection<HostInfo>
        get() = hostMap.values

    val online: Int
        get() = _accounts.size

    val accounts: Set<String>
        get() = _accounts

    fun addAccount(account: String) {
        _accounts.add(account)
    }

    fun removeAccount(account: String) {
        _accounts.remove(account)
    }

    fun setDown() {
        isOnline = false
        _accounts.clear()
    }

    fun store(): Boolean {
        var con: Connection? = null
        var statement: PreparedStatement? = null
        try {
            con = DatabaseFactory.getInstance().connection
            for (host in hosts) {
                statement =
                    con.prepareStatement("REPLACE INTO gameservers (`id`, `ip`, `port`, `age_limit`, `pvp`, `max_players`, `type`, `brackets`, `key`) VALUES(?,?,?,?,?,?,?,?,?)")
                var i = 0
                statement.setInt(++i, host.id)
                statement.setString(++i, host.address)
                statement.setShort(++i, host.port.toShort())
                statement.setByte(++i, ageLimit.toByte())
                statement.setByte(++i, (if (isPvp) 1 else 0).toByte())
                statement.setShort(++i, maxPlayers.toShort())
                statement.setInt(++i, serverType)
                statement.setByte(++i, (if (isShowingBrackets) 1 else 0).toByte())
                statement.setString(++i, host.key)
                statement.execute()
                DbUtils.closeQuietly(statement)
            }
        } catch (e: Exception) {
            _log.atSevere().withStackTrace(com.google.common.flogger.StackSize.FULL).withCause(e).log("Error while store gameserver.")
            return false
        } finally {
            DbUtils.closeQuietly(con, statement)
        }
        return true
    }

    companion object {

        private val _log = FluentLogger.forEnclosingClass()

    }

}