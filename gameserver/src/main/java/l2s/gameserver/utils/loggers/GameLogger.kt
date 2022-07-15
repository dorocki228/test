package l2s.gameserver.utils.loggers

import com.google.common.flogger.GoogleLogger
import l2s.gameserver.network.l2.components.ChatType
import com.google.common.flogger.MetadataKey
import l2s.gameserver.model.GameObject
import l2s.gameserver.model.Player
import java.awt.SystemColor.text

/**
 * @author Java-man
 * @since 16.09.2019
 */
object GameLogger {

    val logger = GoogleLogger.forEnclosingClass()

    val categoryKey = MetadataKey.single("category", String::class.java)
    val playerKey = MetadataKey.single("player", Player::class.java)

}