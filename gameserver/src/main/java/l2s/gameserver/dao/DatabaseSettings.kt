package l2s.gameserver.dao

import l2s.gameserver.database.DatabaseFactory
import org.jetbrains.exposed.sql.Database

/**
 * @author Java-man
 * @since 07.06.2019
 */
object DatabaseSettings {

    val database = Database.connect(DatabaseFactory.getInstance().dataSource)

}