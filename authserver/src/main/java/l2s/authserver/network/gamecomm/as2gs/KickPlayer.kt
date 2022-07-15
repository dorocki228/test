package l2s.authserver.network.gamecomm.as2gs

import io.netty.buffer.ByteBuf
import l2s.authserver.network.gamecomm.SendablePacket

class KickPlayer(private val account: String) : SendablePacket() {

    override val opCode: Byte = 0x03

    override fun writeImpl(byteBuf: ByteBuf): ByteBuf {
        writeString(byteBuf, account)
        return byteBuf
    }

}