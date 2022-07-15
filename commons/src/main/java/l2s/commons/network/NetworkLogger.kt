package l2s.commons.network

import com.google.common.flogger.GoogleLogger
import com.google.common.flogger.MetadataKey
import java.awt.SystemColor.text

/**
 * @author Java-man
 * @since 14.09.2019
 */
object NetworkLogger {

    val logger = GoogleLogger.forEnclosingClass()

    val clientKey = MetadataKey.single("client", ChannelInboundHandler::class.java)

}