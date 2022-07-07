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
    @Volatile
    private var restart: Boolean = false

    private val timers : MutableMap<NetClient, MutableSet<Long>> = mutableMapOf()

    override fun start() {
        synchronized(this) {
            val options = netClientOptionsOf(
                    logActivity = true,
                    reconnectAttempts = -1,
                    reconnectInterval = 20_000,
                    reuseAddress = true,
                    soLinger = Duration.ofSeconds(60).toMillis().toInt(),
                    tcpKeepAlive = true,
                    tcpNoDelay = true,
                    usePooledBuffers = true,
                    receiveBufferSize = 16777216,
                    sendBufferSize = 16777216
            )
            client = vertx.createNetClient(options)
            client.connect(SocketAddressImpl(socketAddress)) { res ->
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

                        val timerId = vertx.setTimer(20_000) {
                            restart()
                        }
                        putTimer(client, timerId)
                    }
                    logger.info("Connected to ${socket.remoteAddress()}.")

                    connection.sendPacket(AuthRequest())

                    val timerId = vertx.setTimer(60_000) {
                        if (!connection.isAuthed) {
                            restart()
                        }
                    }
                    putTimer(client, timerId)
                }
            }
        }
    }

    fun putTimer(client : NetClient, timerId : Long) {
        var set = timers[client]
        if(set == null) {
            set = mutableSetOf()
            timers[client] = set
        }
        set.add(timerId)
    }

    override fun stop() {
        synchronized(this) {
            val set = timers.remove(client)
            if(set != null) {
                set.forEach { e ->
                    vertx.cancelTimer(e)
                }
                set.clear()
            }
            client.close()
        }
    }

    fun sendPacket(packet: SendablePacket) {
        if (isShutdown() || !::connection.isInitialized) {
            logger.error("Connection to authserver is not set.")
            return
        }

        val buf = allocator.buffer()
        val byteBuf = packet.write(buf)
        val size = byteBuf.readableBytes()
        val buffer = Buffer.buffer(size + 4)
        buffer.appendInt(size)
        buffer.appendBuffer(Buffer.buffer(byteBuf))

        vertx.eventBus().send(connection.writeHandlerID, buffer)
        buf.release()
        logger.info(
                "${packet.javaClass.simpleName} with size ${byteBuf.readableBytes()}(${buffer.length()})" +
                        " was sent."
        )
    }

    fun isShutdown(): Boolean {
        return shutdown || restart
    }

    fun restart() {
        if (restart) {
            return
        }
        synchronized(this) {
            if (restart) {
                return
            }
            restart = true
            logger.info("Restarting connection to authserver.")
            stop()
            start()
            restart = false
        }
    }

    companion object {

        private val allocator = ByteBufAllocator.DEFAULT

    }

}