package l2s.commons.net.netty

import com.google.common.flogger.FluentLogger
import io.netty.buffer.ByteBuf
import java.nio.charset.StandardCharsets

/**
 * @author Java-man
 */
abstract class SendablePacket {

    protected fun writeString(byteBuf: ByteBuf, charSequence: CharSequence) {
        byteBuf.writeShort(charSequence.length)
        byteBuf.writeCharSequence(charSequence, StandardCharsets.UTF_8)
    }

    fun write(byteBuf: ByteBuf): ByteBuf {
        try {
            byteBuf.writeByte(opCode.toInt())
            return writeImpl(byteBuf)
        } catch (e: RuntimeException) {
            logger.atSevere().withStackTrace(com.google.common.flogger.StackSize.FULL).withCause(e).log("Can't write buffer.")
            throw RuntimeException("Can't write buffer.", e)
        }
    }

    protected abstract val opCode: Byte

    protected abstract fun writeImpl(byteBuf: ByteBuf): ByteBuf

    companion object {

        @JvmStatic
        protected val logger = FluentLogger.forEnclosingClass()

    }

}