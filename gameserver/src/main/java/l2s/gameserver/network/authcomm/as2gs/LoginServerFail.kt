package l2s.gameserver.network.authcomm.as2gs

import io.netty.buffer.ByteBuf
import l2s.gameserver.GameServer
import l2s.gameserver.network.authcomm.ReceivablePacket
import l2s.gameserver.network.authcomm.vertx.AuthServerConnection
import org.slf4j.LoggerFactory

class LoginServerFail(client: AuthServerConnection, byteBuf: ByteBuf) : ReceivablePacket(client, byteBuf) {

    private val _reason: String
    private val _restartConnection: Boolean

    init {
        val reasonId: Int = byteBuf.readUnsignedByte().toInt()
        if (!isReadable) {
            _reason =
                "Authserver registration failed! Reason: " + REASONS[reasonId]
            _restartConnection = true
        } else {
            _reason = readString(byteBuf)
            _restartConnection = byteBuf.readUnsignedByte() > 0
        }
    }

    override fun runImpl(client: AuthServerConnection) {
        _log.warn(_reason)
        if (_restartConnection)
            GameServer.getInstance().getAuthServerCommunication().restart()
    }

    companion object {

        private val _log = LoggerFactory.getLogger(LoginServerFail::class.java)

        private val REASONS = arrayOf(
            "none",
            "IP banned",
            "IP reserved",
            "wrong hexid",
            "ID reserved",
            "no free ID",
            "not authed",
            "already logged in"
        )

    }

}