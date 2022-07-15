package l2s.authserver.network.gamecomm.vertx

import io.netty.buffer.ByteBufAllocator
import io.vertx.core.buffer.Buffer
import io.vertx.core.net.NetSocket
import io.vertx.core.net.SocketAddress
import l2s.authserver.network.gamecomm.GameServerDescription
import l2s.authserver.network.gamecomm.SendablePacket
import org.slf4j.LoggerFactory

class GameServerConnection(private val socket: NetSocket) {

    val remoteAddress: SocketAddress = socket.remoteAddress()

    var description = GameServerDescription()

    var isAuthed: Boolean = false

    fun sendPacket(packet: SendablePacket) {
        val byteBuf = packet.write(allocator.buffer())
        val size = byteBuf.readableBytes()
        val buffer = Buffer.buffer(size + 4)
        buffer.appendInt(size)
        buffer.appendBuffer(Buffer.buffer(byteBuf))

        socket.write(buffer)
        logger.debug(
            "${packet.javaClass.simpleName} with size ${byteBuf.readableBytes()}(${buffer.length()})" +
                    " was sent."
        )

        //byteBuf.release()
    }

    fun closeNow() {
        socket.close()
    }

    fun onDisconnection() {
        closeNow()

        if (isAuthed) {
            logger.info("Connection with gameserver IP[$remoteAddress] lost.")
            logger.info("Setting gameserver down.")
            setDown()
        }
    }

    fun setDown() {
        isAuthed = false

        description.setDown()
    }

    companion object {

        private val logger = LoggerFactory.getLogger(GameServerConnection::class.java)

        private val allocator = ByteBufAllocator.DEFAULT

    }
}
