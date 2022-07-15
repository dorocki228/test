package l2s.gameserver.network.authcomm.as2gs

import io.netty.buffer.ByteBuf
import l2s.gameserver.network.authcomm.AuthServerClientService
import l2s.gameserver.network.authcomm.ReceivablePacket
import l2s.gameserver.network.authcomm.gs2as.OnlineStatus
import l2s.gameserver.network.authcomm.gs2as.PlayerInGame
import l2s.gameserver.network.authcomm.vertx.AuthServerConnection
import org.slf4j.LoggerFactory
import java.util.*

/**
 * @reworked by Bonux
 */
class AuthResponse(client: AuthServerConnection, byteBuf: ByteBuf) : ReceivablePacket(client, byteBuf) {

    private class ServerInfo(val id: Int, val name: String)

    private val _servers: List<ServerInfo>

    init {
        val serverId: Int = byteBuf.readUnsignedByte().toInt()
        val serverName: String = readString(byteBuf)
        if (!isReadable) {
            _servers = listOf(ServerInfo(serverId, serverName))
        } else {
            val serversCount: Int = byteBuf.readUnsignedByte().toInt()
            _servers = ArrayList(serversCount)
            for (i in 0 until serversCount)
                _servers.add(ServerInfo(byteBuf.readUnsignedByte().toInt(), readString(byteBuf)))
        }
    }

    override fun runImpl(client: AuthServerConnection) {
        for (info in _servers) _log.info("Registered on authserver as " + info.id.toString() + " [" + info.name + "]")
        client.sendPacket(OnlineStatus(true))
        val accounts: Array<String> = AuthServerClientService.getAccounts()
        for (account in accounts)
            client.sendPacket(PlayerInGame(account))
    }

    companion object {
        private val _log = LoggerFactory.getLogger(AuthResponse::class.java)
    }
}