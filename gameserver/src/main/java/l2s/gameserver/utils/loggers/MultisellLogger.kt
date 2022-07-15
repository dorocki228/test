package l2s.gameserver.utils.loggers

import com.google.common.flogger.GoogleLogger
import l2s.gameserver.network.l2.components.ChatType
import com.google.common.flogger.MetadataKey
import l2s.gameserver.model.GameObject
import l2s.gameserver.model.Player
import java.awt.SystemColor.text

/**
 * @author Java-man
 * @since 26.08.2019
 *
 * TODO move to multisell module
 */
object MultisellLogger {

    private val logger = GoogleLogger.forEnclosingClass()

    private val playerKey = MetadataKey.single("player", Player::class.java)
    private val itemIdKey = MetadataKey.single("itemId", Int::class.java)
    private val itemCountKey = MetadataKey.single("itemCount", Long::class.java)
    private val totalKey = MetadataKey.single("total", Long::class.java)

    fun log(
        player: Player,
        itemId: Int,
        itemCount: Long,
        total: Long
    ) {
        logger.atInfo()
            .with(playerKey, player)
            .with(itemIdKey, itemId)
            .with(itemCountKey, itemCount)
            .with(totalKey, total)
            .log()
    }
}