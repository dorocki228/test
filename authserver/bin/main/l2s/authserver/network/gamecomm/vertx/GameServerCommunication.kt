package l2s.authserver.network.gamecomm.vertx

import io.netty.buffer.ByteBufAllocator
import io.vertx.core.AbstractVerticle
import io.vertx.core.Future
import io.vertx.core.http.ClientAuth
import io.vertx.core.net.NetServer
import io.vertx.kotlin.core.net.netServerOptionsOf
import l2s.authserver.network.gamecomm.PacketHandler
import l2s.commons.net.vertx.LengthPrefixedStream
import org.slf4j.LoggerFactory
import java.net.InetAddress
import java.net.InetSocketAddress
import java.time.Duration

/**
 * @author Java-man
 * @since 15.06.2019
 */
class GameServerCommunication(address: InetAddress?, tcpPort: Int) : AbstractVerticle() {

    private val logger = LoggerFactory.getLogger(GameServerCommunication::class.java)

    private val socketAddress: InetSocketAddress =
        address?.let { InetSocketAddress(it, tcpPort) } ?: InetSocketAddress(tcpPort)
    private lateinit var server: NetServer

    private var restart: Boolean = false

    override fun start() {
        val options = netServerOptionsOf(
            clientAuth = ClientAuth.REQUIRED,
            clientAuthRequired = true,
            host = socketAddress.hostName,
            port = socketAddress.port,
            logActivity = true,
            reuseAddress = true,
            soLinger = Duration.ofSeconds(60).toMillis().toInt(),
            tcpKeepAlive = true,
            tcpNoDelay = true,
            usePooledBuffers = true,
            receiveBufferSize = 16777216,
            sendBufferSize = 16777216
        )
        server = vertx.createNetServer(options)
        server.connectHandler { socket ->
            val connection = GameServerConnection(socket)
            val frameParser = LengthPrefixedStream(socket)
            frameParser.handler { buffer ->
                val bytes = buffer.bytes
                val buf = allocator.buffer(bytes.size)
                buf.writeBytes(bytes)
                val packet = PacketHandler.handlePacket(connection, buf) ?: return@handler
                packet.run()
                buf.release()
                logger.info("Received $packet packet with ${buffer.length()} size.")

                if (socket.writeQueueFull()) {
                    socket.pause()
                    socket.drainHandler { done ->
                        socket.resume()
                        logger.info("Socket was paused.")
                    }
                    logger.info("Socket was paused.")
                }
            }
            socket.closeHandler {
                connection.onDisconnection()
                logger.info("The socket has been closed.")
            }
        }
        server.listen { res ->
            if (res.succeeded()) {
                logger.info("Listening for gameservers on {}.", socketAddress)
            } else {
                logger.info("Failed to bind.")
            }
        }
    }

    override fun stop() {
        server.close { res ->
            if (res.succeeded()) {
                logger.info("Server is now closed.")
            } else {
                logger.info("Close failed.")
            }
        }
    }

    fun restart() {
        restart = true

        stop(Future.future {
            start()
            restart = false
        })
    }

    companion object {

        private val allocator = ByteBufAllocator.DEFAULT

    }

}