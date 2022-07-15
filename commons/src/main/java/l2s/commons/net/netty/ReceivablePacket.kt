package l2s.commons.net.netty

import com.google.common.flogger.FluentLogger
import io.netty.buffer.ByteBuf
import java.nio.charset.StandardCharsets

/**
 * @author Java-man
 */
abstract class ReceivablePacket<T>(private val client: T, private val byteBuf: ByteBuf) : Runnable {

    val isReadable: Boolean
        get() = byteBuf.isReadable

    protected fun readString(byteBuf: ByteBuf): String {
        val length = byteBuf.readShort()
        return byteBuf.readCharSequence(length.toInt(), StandardCharsets.UTF_8).toString()
    }

    override fun run() {
        try {
            runImpl(client)
        } catch (e: Exception) {
            LOGGER.atSevere().withStackTrace(com.google.common.flogger.StackSize.FULL).withCause(e).log("Can't read packet.")
        }
    }

    protected abstract fun runImpl(client: T)

    companion object {

        private val LOGGER = FluentLogger.forEnclosingClass()

    }

}