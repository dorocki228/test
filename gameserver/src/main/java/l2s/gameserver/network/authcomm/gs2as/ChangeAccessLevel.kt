package l2s.gameserver.network.authcomm.gs2as

import io.netty.buffer.ByteBuf
import l2s.gameserver.network.authcomm.SendablePacket

class ChangeAccessLevel(private val account: String, private val level: Int, private val banExpire: Int) :
    SendablePacket() {

    override val opCode: Byte = 0x11

    override fun writeImpl(byteBuf: ByteBuf): ByteBuf {
        writeString(byteBuf, account)
        byteBuf.writeInt(level)
        byteBuf.writeInt(banExpire)

        return byteBuf
    }

}