package l2s.gameserver.network.authcomm.gs2as

import io.netty.buffer.ByteBuf
import l2s.gameserver.network.authcomm.SendablePacket
import l2s.gameserver.network.l2.GameClient

class PlayerAuthRequest(client: GameClient) : SendablePacket() {
    private val account: String = client.login
    private val playOkID1: Int = client.sessionKey.playOkID1
    private val playOkID2: Int = client.sessionKey.playOkID2
    private val loginOkID1: Int = client.sessionKey.loginOkID1
    private val loginOkID2: Int = client.sessionKey.loginOkID2

    override val opCode: Byte = 0x02

    override fun writeImpl(byteBuf: ByteBuf): ByteBuf {
        writeString(byteBuf, account)
        byteBuf.writeInt(playOkID1)
        byteBuf.writeInt(playOkID2)
        byteBuf.writeInt(loginOkID1)
        byteBuf.writeInt(loginOkID2)
        return byteBuf
    }
}