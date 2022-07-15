package l2s.gameserver.network.authcomm.gs2as

import io.netty.buffer.ByteBuf
import l2s.gameserver.network.authcomm.SendablePacket

class OnlineStatus(private val _online: Boolean) : SendablePacket() {

    override val opCode: Byte = 0x01

    override fun writeImpl(byteBuf: ByteBuf): ByteBuf {
        byteBuf.writeByte(if (_online) 1 else 0)
        return byteBuf
    }

}