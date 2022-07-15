package l2s.gameserver.network.authcomm.vertx

import io.netty.buffer.ByteBufAllocator
import io.vertx.core.AbstractVerticle
import io.vertx.core.Future
import io.vertx.core.buffer.Buffer
import io.vertx.core.net.NetClient
import io.vertx.core.net.impl.SocketAddressImpl
import io.vertx.kotlin.core.net.netClientOptionsOf
import l2s.commons.net.vertx.LengthPrefixedStream
import l2s.gameserver.network.authcomm.PacketHandler
import l2s.gameserver.network.authcomm.SendablePacket
import l2s.gameserver.network.authcomm.gs2as.AuthRequest
import org.slf4j.LoggerFactory
import java.net.InetAddress
import java.net.InetSocketAddress
import java.time.Duration

/**
 * @author Java-man
 * @since 15.06.2019
 */
class AuthServerCommunication(address: InetAddress?, tcpPort: Int) : AbstractVerticle() {

    private val logger = LoggerFactory.getLogger(AuthServerCommunication::class.java)

    private val socketAddress: InetSocketAddress =
        address?.let { InetSocketAddress(it, tcpPort) } ?: InetSocketAddress(tcpPort)
    private lateinit var client: NetClient
    private lateinit var connection: AuthServerConnection

    private var shutdown: Boolean = false
    private var restart: Boolean = false

    override fun start() {
        val options = netClientOptionsOf(
            logActivity = true,
            reconnectAttempts = 100,
            reconnectInterval = 30_000,
            reuseAddress = true,
            soLinger = Duration.ofSeconds(60).toMillis().toInt(),
            tcpKeepAlive = true,
            tcpNoDelay = true,
            usePooledBuffers = true
        )
        client = vertx.createNetClient(options)
        client.connect(SocketAddressImpl(socketAddress.port, socketAddress.hostName)) { res ->
            if (res.succeeded()) {
                val socket = res.result()
                connection = AuthServerConnection(socket)
                val frameParser = LengthPrefixedStream(socket)
                frameParser.handler { buffer ->
                    val bytes = buffer.bytes
                    val buf = allocator.buffer(bytes.size)
                    buf.writeBytes(bytes)
                    val packet = PacketHandler.handlePacket(connection, buf) ?: return@handler
                    packet.run()
                    logger.debug("Received $packet packet with ${buffer.length()} size.")

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

                    vertx.setTimer(20_000) {
                        restart()
                    }
                }
                logger.info("Connected to ${socket.remoteAddress()}.")

                connection.sendPacket(AuthRequest())

                /*vertx.setTimer(60_000) {
                    if (!connection.isAuthed) {
                        restart()
                    }
                }*/
            } else {
                logger.info("Failed to connect: ${res.cause().message}.")

                vertx.setTimer(20_000) {
                    restart()
                }
            }
        }
    }

    override fun stop() {
        client.close()
    }

    fun sendPacket(packet: SendablePacket) {
        if (isShutdown() || !::connection.isInitialized) {
            logger.error("Connection to authserver is not set.")
            return
        }

        val byteBuf = packet.write(allocator.buffer())
        val size = byteBuf.readableBytes()
        val buffer = Buffer.buffer(size + 4)
        buffer.appendInt(size)
        buffer.appendBuffer(Buffer.buffer(byteBuf))

        vertx.eventBus().send(connection.writeHandlerID, buffer)
        logger.debug(
            "${packet.javaClass.simpleName} with size ${byteBuf.readableBytes()}(${buffer.length()})" +
                    " was sent."
        )

        //byteBuf.release()
    }

    fun isShutdown(): Boolean {
        return shutdown || restart
    }

    fun restart() {
        restart = true
        logger.info("Restarting connection to authserver.")
        stop(Future.future {
            start()
            restart = false
        })
    }

    companion object {

        private val allocator = ByteBufAllocator.DEFAULT

    }

}