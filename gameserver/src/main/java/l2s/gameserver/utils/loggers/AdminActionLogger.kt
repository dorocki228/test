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
 * TODO move to admin module
 */
object AdminActionLogger {

    private val logger = GoogleLogger.forEnclosingClass()

    private val playerKey = MetadataKey.single("player", Player::class.java)
    private val targetKey = MetadataKey.single("target", GameObject::class.java)
    private val commandKey = MetadataKey.single("command", String::class.java)
    private val successKey = MetadataKey.single("success", Boolean::class.java)

    fun log(
        player: Player,
        target: GameObject?,
        command: String,
        success: Boolean
    ) {
        logger.atInfo()
            .with(playerKey, player)
            .with(targetKey, target)
            .with(commandKey, command)
            .with(successKey, success)
            .log()
    }
}