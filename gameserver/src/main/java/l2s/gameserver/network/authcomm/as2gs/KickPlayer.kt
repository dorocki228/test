package l2s.gameserver.network.authcomm.as2gs

import io.netty.buffer.ByteBuf
import l2s.gameserver.network.authcomm.AuthServerClientService
import l2s.gameserver.network.authcomm.ReceivablePacket
import l2s.gameserver.network.authcomm.vertx.AuthServerConnection
import l2s.gameserver.network.l2.GameClient
import l2s.gameserver.network.l2.components.SystemMsg
import l2s.gameserver.network.l2.s2c.ServerCloseSocketPacket

class KickPlayer(client: AuthServerConnection, byteBuf: ByteBuf) : ReceivablePacket(client, byteBuf) {

    private val account: String = readString(byteBuf)

    override fun runImpl(client: AuthServerConnection) {
        var client: GameClient? =
            AuthServerClientService.removeWaitingClient(account)
        if (client == null) client = AuthServerClientService.removeAuthedClient(account)
        if (client == null) return
        val activeChar = client.activeChar
        if (activeChar != null) {
            //FIXME [G1ta0] сообщение чаще всего не показывается, т.к. при закрытии соединения очередь на отправку очищается
            activeChar.sendPacket(SystemMsg.ANOTHER_PERSON_HAS_LOGGED_IN_WITH_THE_SAME_ACCOUNT)
            activeChar.kick()
        } else {
            client.close(ServerCloseSocketPacket.STATIC)
        }
    }
}