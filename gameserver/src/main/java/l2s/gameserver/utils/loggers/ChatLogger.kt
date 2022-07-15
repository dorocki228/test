package l2s.gameserver.utils.loggers

import com.google.common.flogger.GoogleLogger
import l2s.gameserver.network.l2.components.ChatType
import com.google.common.flogger.MetadataKey
import l2s.gameserver.model.Player

/**
 * @author Java-man
 * @since 26.08.2019
 *
 * TODO move to chat module
 * TODO migrate target to Player type
 */
object ChatLogger {

    private val logger = GoogleLogger.forEnclosingClass()

    private val typeKey = MetadataKey.single("type", ChatType::class.java)
    private val playerKey = MetadataKey.single("player", Player::class.java)
    private val targetKey = MetadataKey.single("target", String::class.java)
    private val textKey = MetadataKey.single("text", String::class.java)

    fun log(
        type: ChatType,
        player: Player,
        target: String?,
        text: String
    ) {
        logger.atInfo()
            .with(typeKey, type)
            .with(playerKey, player)
            .with(targetKey, target)
            .with(textKey, text)
            .log()
    }
}