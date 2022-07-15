package l2s.authserver.network.gamecomm.gs2as

import io.netty.buffer.ByteBuf
import l2s.authserver.AuthServer
import l2s.authserver.GameServerManager
import l2s.authserver.network.gamecomm.ReceivablePacket
import l2s.authserver.network.gamecomm.as2gs.AuthResponse
import l2s.authserver.network.gamecomm.as2gs.LoginServerFail
import l2s.authserver.network.gamecomm.vertx.GameServerConnection
import l2s.commons.net.HostInfo
import org.slf4j.Logger
import org.slf4j.LoggerFactory


/**
 * @reworked by Bonux
 */
class AuthRequest(client: GameServerConnection, byteBuf: ByteBuf) : ReceivablePacket(client, byteBuf) {

    private val _protocolVersion: Int
    private val _hosts: Array<HostInfo>
    private val _serverType: Int
    private val _ageLimit: Int
    private val _gmOnly: Boolean
    private val _brackets: Boolean
    private val _pvp: Boolean
    private val _maxOnline: Int

    init {
        _protocolVersion = byteBuf.readInt()
        require(_protocolVersion == AuthServer.AUTH_SERVER_PROTOCOL) {
            "Auth server protocol version is wrong: $_protocolVersion."
        }
        _serverType = byteBuf.readInt()
        _ageLimit = byteBuf.readInt()
        _gmOnly = byteBuf.readUnsignedByte() == 1.toShort()
        _brackets = byteBuf.readUnsignedByte() == 1.toShort()
        _pvp = byteBuf.readUnsignedByte() == 1.toShort()
        _maxOnline = byteBuf.readInt()
        val hostsCount: Int = byteBuf.readUnsignedByte().toInt()
        _hosts = Array(hostsCount) {
            val id: Int = byteBuf.readUnsignedByte().toInt()
            val address: String = readString(byteBuf)
            val port: Int = byteBuf.readUnsignedShort()
            val key: String = readString(byteBuf)
            val maskCount: Int = byteBuf.readUnsignedByte().toInt()
            val host = HostInfo(id, address, port, key)
            for (m in 0 until maskCount) {
                val subAddress: String = readString(byteBuf)
                val subnetAddress = ByteArray(byteBuf.readInt())
                byteBuf.readBytes(subnetAddress)
                val subnetMask = ByteArray(byteBuf.readInt())
                byteBuf.readBytes(subnetMask)
                host.addSubnet(subAddress, subnetAddress, subnetMask)
            }

            host
        }
    }

    override fun runImpl(client: GameServerConnection) {
        if (_protocolVersion != AuthServer.AUTH_SERVER_PROTOCOL) {
            logger.warn("Authserver and gameserver have different versions! Please update your servers.")
            client.sendPacket(
                LoginServerFail(
                    "Authserver and gameserver have different versions! Please update your servers.",
                    false
                )
            )
            return
        }

        logger.info("Trying to register gameserver: IP[" + client.remoteAddress + "]")
        for (host in _hosts) {
            val registerResult =
                GameServerManager.getInstance().registerGameServer(host, client)
            if (registerResult == GameServerManager.SUCCESS_GS_REGISTER)
                client.description.addHost(host)
            else {
                when (registerResult) {
                    GameServerManager.FAIL_GS_REGISTER_DIFF_KEYS -> {
                        client.sendPacket(
                            LoginServerFail(
                                "Gameserver registration on ID[" + host.id.toString() + "] failed. Registered different keys!",
                                false
                            )
                        )
                        client.sendPacket(
                            LoginServerFail(
                                "Set the same keys in authserver and gameserver, and restart them!",
                                false
                            )
                        )
                    }
                    GameServerManager.FAIL_GS_REGISTER_ID_ALREADY_USE -> {
                        client.sendPacket(
                            LoginServerFail(
                                "Gameserver registration on ID[" + host.id.toString() + "] failed. ID[" + host.id.toString() + "] is already in use!",
                                false
                            )
                        )
                        client.sendPacket(
                            LoginServerFail(
                                "Free ID[" + host.id.toString() + "] or change to another ID, and restart your authserver or gameserver!",
                                false
                            )
                        )
                    }
                    GameServerManager.FAIL_GS_REGISTER_ERROR -> {
                        client.sendPacket(
                            LoginServerFail(
                                "Gameserver registration on ID[" + host.id.toString() + "] failed. You have some errors!",
                                false
                            )
                        )
                        client.sendPacket(LoginServerFail("To solve the problem, contact the developer!", false))
                    }
                }
            }
        }

        val description = client.description
        if (description.hosts.isNotEmpty()) {
            description.protocol = _protocolVersion
            description.serverType = _serverType
            description.ageLimit = _ageLimit
            description.isGmOnly = _gmOnly
            description.isShowingBrackets = _brackets
            description.isPvp = _pvp
            description.maxPlayers = _maxOnline
            description.store()
            client.isAuthed = true
        } else {
            client.sendPacket(LoginServerFail("Gameserver registration failed. All ID's is already in use!", true))
            logger.info("Gameserver registration failed.")
            return
        }
        logger.info("Gameserver registration successful.")
        client.sendPacket(AuthResponse(description))
    }

    companion object {

        private val logger: Logger = LoggerFactory.getLogger(AuthRequest::class.java)

    }

}