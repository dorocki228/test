package l2s.authserver.network.gamecomm.gs2as

import io.netty.buffer.ByteBuf
import l2s.authserver.accounts.SessionManager
import l2s.authserver.network.gamecomm.GameServerDescription
import l2s.authserver.network.gamecomm.ReceivablePacket
import l2s.authserver.network.gamecomm.vertx.GameServerConnection
import org.apache.commons.lang3.ArrayUtils

/**
 * @author VISTALL
 * @date 20:52/25.03.2011
 */
class SetAccountInfo(client: GameServerConnection, byteBuf: ByteBuf) : ReceivablePacket(client, byteBuf) {

    private val _account: String? = readString(byteBuf)
    private val _size = byteBuf.readUnsignedByte()
    private val _deleteChars: IntArray

    init {
        val size: Int = byteBuf.readInt()
        _deleteChars = if (size > 7 || size <= 0)
            ArrayUtils.EMPTY_INT_ARRAY
        else {
            IntArray(size) {
                byteBuf.readInt()
            }
        }
    }

    override fun runImpl(client: GameServerConnection) {
        val gs: GameServerDescription = client.description
        if (client.isAuthed) {
            val session =
                SessionManager.getInstance().getSessionByName(_account) ?: return
            for (host in gs.hosts)
                session.account.addAccountInfo(host.id, _size.toInt(), _deleteChars)
        }
    }
}