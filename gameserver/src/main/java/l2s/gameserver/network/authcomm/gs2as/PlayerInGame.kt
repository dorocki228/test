package l2s.gameserver.network.authcomm.gs2as

import io.netty.buffer.ByteBuf
import l2s.gameserver.network.authcomm.SendablePacket

class PlayerInGame(private val account: String) : SendablePacket() {

    override val opCode: Byte = 0x03

    override fun writeImpl(byteBuf: ByteBuf): ByteBuf {
        writeString(byteBuf, account)
        return byteBuf
    }

}