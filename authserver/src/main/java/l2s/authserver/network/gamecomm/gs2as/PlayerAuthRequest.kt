package l2s.authserver.network.gamecomm.gs2as

import io.netty.buffer.ByteBuf
import l2s.authserver.accounts.SessionManager
import l2s.authserver.network.gamecomm.ReceivablePacket
import l2s.authserver.network.gamecomm.as2gs.PlayerAuthResponse
import l2s.authserver.network.gamecomm.vertx.GameServerConnection
import l2s.authserver.network.l2.SessionKey

class PlayerAuthRequest(client: GameServerConnection, byteBuf: ByteBuf) : ReceivablePacket(client, byteBuf) {

    private val account: String = readString(byteBuf)
    private val playOkId1 = byteBuf.readInt()
    private val playOkId2 = byteBuf.readInt()
    private val loginOkId1 = byteBuf.readInt()
    private val loginOkId2 = byteBuf.readInt()

    override fun runImpl(client: GameServerConnection) {
        val skey = SessionKey(loginOkId1, loginOkId2, playOkId1, playOkId2)
        val session = SessionManager.getInstance().closeSession(skey)
        if (session == null || session.account.login != account) {
            client.sendPacket(PlayerAuthResponse(account))
            return
        }
        client.sendPacket(PlayerAuthResponse(session, session.sessionKey == skey))
    }

}