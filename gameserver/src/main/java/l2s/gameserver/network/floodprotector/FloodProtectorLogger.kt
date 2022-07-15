package l2s.gameserver.network.floodprotector

import com.google.common.flogger.GoogleLogger
import com.google.common.flogger.MetadataKey
import l2s.gameserver.network.l2.GameClient
import java.awt.SystemColor.text

/**
 * @author Java-man
 * @since 14.09.2019
 */
object FloodProtectorLogger {

    val logger = GoogleLogger.forEnclosingClass()

    val clientKey = MetadataKey.single("client", GameClient::class.java)

}