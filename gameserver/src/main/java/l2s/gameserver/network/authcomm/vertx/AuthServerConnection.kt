package l2s.gameserver.network.authcomm.vertx

import io.netty.buffer.ByteBufAllocator
import io.vertx.core.buffer.Buffer
import io.vertx.core.net.NetSocket
import l2s.gameserver.network.authcomm.SendablePacket
import org.slf4j.LoggerFactory

class AuthServerConnection(private val socket: NetSocket) {

    val writeHandlerID: String
        get() = socket.writeHandlerID()
    var isAuthed: Boolean = false

    fun sendPacket(packet: SendablePacket) {
        var buf = allocator.buffer()
        val byteBuf = packet.write(buf)
        val size = byteBuf.readableBytes()
        val buffer = Buffer.buffer(size + 4)
        buffer.appendInt(size)
        buffer.appendBuffer(Buffer.buffer(byteBuf))
        socket.write(buffer)
        logger.info(
            "${packet.javaClass.simpleName} with size ${byteBuf.readableBytes()}(${buffer.length()})" +
                    " was sent."
        )
        buf.release()
    }

    fun closeNow() {
        socket.close()
    }

    fun onDisconnection() {
        closeNow()

        if (isAuthed) {
            logger.info("Connection with authserver IP[${socket.remoteAddress()}] lost.")
            setDown()
        }
    }

    fun setDown() {
        isAuthed = false
    }

    companion object {

        private val logger = LoggerFactory.getLogger(AuthServerConnection::class.java)

        private val allocator = ByteBufAllocator.DEFAULT

    }
}
