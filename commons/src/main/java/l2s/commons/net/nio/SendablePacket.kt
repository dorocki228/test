package l2s.commons.net.nio

import java.nio.charset.StandardCharsets

abstract class SendablePacket<T> : AbstractPacket<T>() {

    protected fun writeC(data: Int) {
        byteBuffer.put(data.toByte())
    }

    protected fun writeF(value: Double) {
        byteBuffer.putDouble(value)
    }

    protected fun writeCutF(value: Double) {
        byteBuffer.putFloat(value.toFloat())
    }

    protected fun writeH(value: Int) {
        byteBuffer.putShort(value.toShort())
    }

    protected fun writeD(value: Int) {
        byteBuffer.putInt(value)
    }

    protected fun writeQ(value: Long) {
        byteBuffer.putLong(value)
    }

    protected fun writeB(data: ByteArray) {
        byteBuffer.put(data)
    }

    protected fun writeS(charSequence: CharSequence?) {
        if (charSequence.isNullOrBlank()) {
            byteBuffer.putChar('\u0000')
            return
        }

        writeStringWithCharset(charSequence)
        byteBuffer.putChar('\u0000')
    }

    protected fun writeSizedString(charSequence: CharSequence?) {
        if (charSequence.isNullOrBlank()) {
            writeH(0)
            return
        }

        writeH(charSequence.length)
        writeStringWithCharset(charSequence)
    }

    private inline fun writeStringWithCharset(text: CharSequence?) {
        writeB(text.toString().toByteArray(StandardCharsets.UTF_16LE))
    }

    protected abstract fun write(): Boolean

}
