package l2s.gameserver.network.authcomm.gs2as

import io.netty.buffer.ByteBuf
import l2s.gameserver.network.authcomm.SendablePacket

class ReduceAccountPoints(private val account: String, private val count: Int) : SendablePacket() {

    override val opCode: Byte = 0x12

    override fun writeImpl(byteBuf: ByteBuf): ByteBuf {
        writeString(byteBuf, account)
        byteBuf.writeInt(count)
        return byteBuf
    }

}