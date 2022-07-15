package l2s.authserver.network.gamecomm.as2gs

import io.netty.buffer.ByteBuf
import l2s.authserver.network.gamecomm.SendablePacket

class ChangePasswordResponse(private val account: String, private val hasChanged: Boolean) : SendablePacket() {

    override val opCode: Byte = 0x06

    override fun writeImpl(byteBuf: ByteBuf): ByteBuf {
        writeString(byteBuf, account)
        byteBuf.writeInt(if (hasChanged) 1 else 0)
        return byteBuf
    }

}