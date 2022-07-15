package l2s.gameserver.utils.loggers

import l2s.gameserver.database.DatabaseFactory
import l2s.gameserver.model.GameObjectsStorage
import l2s.gameserver.model.Player
import java.sql.ResultSet

data class PlayerInfo(
    private val accountName: String,
    private val id: Int,
    private val name: String,
    private val lastIp: String,
    private val lastHwid: String
) {

    constructor(player: Player): this(
        player.accountName,
        player.objectId,
        player.name,
        player.ip,
        player.hwid
    )

    constructor(resultSet: ResultSet): this(
        resultSet.getString("account_name"),
        resultSet.getInt("obj_Id"),
        resultSet.getString("char_name"),
        resultSet.getString("last_ip"),
        resultSet.getString("last_hwid")
    )

    companion object {

        fun findByObjectId(id: Int): PlayerInfo? {
            val player = GameObjectsStorage.getPlayer(id)
            if (player != null && player.isOnline && !player.isInOfflineMode) {
                return PlayerInfo(player)
            }

            DatabaseFactory.getInstance().connection.use { connection ->
                connection.prepareStatement("SELECT account_name, obj_Id, char_name, last_ip, last_hwid FROM characters WHERE obj_Id=? LIMIT 1")
                    .use { statement ->
                        statement.setInt(1, id)
                        statement.executeQuery().use { resultSet ->
                            return if (resultSet.next()) {
                                PlayerInfo(resultSet)
                            } else {
                                null
                            }
                        }
                    }
            }
        }

        fun findByName(name: String): PlayerInfo? {
            val player = GameObjectsStorage.getPlayer(name)
            if (player != null && player.isOnline && !player.isInOfflineMode) {
                return PlayerInfo(player)
            }

            DatabaseFactory.getInstance().connection.use { connection ->
                connection.prepareStatement("SELECT account_name, obj_Id, char_name, last_ip, last_hwid FROM characters WHERE char_name=? LIMIT 1")
                    .use { statement ->
                        statement.setString(1, name)
                        statement.executeQuery().use { resultSet ->
                            return if (resultSet.next()) {
                                PlayerInfo(resultSet)
                            } else {
                                null
                            }
                        }
                    }
            }
        }

    }

}