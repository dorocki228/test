package l2s.commons.net.vertx

import io.vertx.core.Handler
import io.vertx.core.buffer.Buffer
import io.vertx.core.parsetools.RecordParser
import io.vertx.core.streams.ReadStream

class LengthPrefixedStream(stream: ReadStream<Buffer>) : ReadStream<Buffer> {

    private val recordParser: RecordParser = RecordParser.newFixed(4, stream)
    private var expectedToken = FrameToken.SIZE

    override fun exceptionHandler(handler: Handler<Throwable>): ReadStream<Buffer> {
        recordParser.exceptionHandler(handler)
        return this
    }

    override fun handler(handler: Handler<Buffer>?): ReadStream<Buffer> {
        if (handler == null) {
            recordParser.handler(null)
            return this
        }

        recordParser.handler { buffer: Buffer ->
            when (expectedToken) {
                FrameToken.SIZE -> {
                    val frameSize = buffer.getInt(0)
                    recordParser.fixedSizeMode(frameSize)
                    expectedToken = FrameToken.PAYLOAD
                }
                FrameToken.PAYLOAD -> {
                    recordParser.fixedSizeMode(4)
                    expectedToken = FrameToken.SIZE
                    handler.handle(buffer)
                }
            }
        }

        return this
    }

    override fun pause(): ReadStream<Buffer> {
        recordParser.pause()
        return this
    }

    override fun resume(): ReadStream<Buffer> {
        recordParser.resume()
        return this
    }

    override fun fetch(amount: Long): ReadStream<Buffer> {
        recordParser.fetch(amount)
        return this
    }

    override fun endHandler(endHandler: Handler<Void>?): ReadStream<Buffer> {
        recordParser.endHandler(endHandler)
        return this
    }

}