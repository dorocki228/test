package l2s.authserver.network.gamecomm.vertx

import io.netty.buffer.ByteBufAllocator
import io.vertx.core.buffer.Buffer
import io.vertx.core.net.NetSocket
import io.vertx.core.net.SocketAddress
import io.vertx.kotlin.core.net.writeAwait
import l2s.authserver.network.gamecomm.GameServerDescription
import l2s.authserver.network.gamecomm.SendablePacket
import org.slf4j.LoggerFactory


class GameServerConnection(private val socket: NetSocket) {

    val remoteAddress: SocketAddress = socket.remoteAddress()

    val gameServerDescription = GameServerDescription(remoteAddress.toString())

    var isAuthed: Boolean = false

    fun sendPacket(packet: SendablePacket) {
        val buf = allocator.buffer()
        val byteBuf = packet.write(buf)
        val size = byteBuf.readableBytes()
        val buffer = Buffer.buffer(size + 4)
        buffer.appendInt(size)
        buffer.appendBuffer(Buffer.buffer(byteBuf))

        socket.write(buffer)
        buf.release()
        logger.info(
            "${packet.javaClass.simpleName} with size ${byteBuf.readableBytes()}(${buffer.length()})" +
                    " was sent."
        )
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

        gameServerDescription.setDown()
    }

    companion object {

        private val logger = LoggerFactory.getLogger(GameServerConnection::class.java)

        private val allocator = ByteBufAllocator.DEFAULT

    }
}
