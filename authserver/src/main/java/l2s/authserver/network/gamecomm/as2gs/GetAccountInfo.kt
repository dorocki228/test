package l2s.authserver.network.gamecomm.as2gs

import io.netty.buffer.ByteBuf
import l2s.authserver.network.gamecomm.SendablePacket

/**
 * @author VISTALL
 * @date 20:50/25.03.2011
 */
class GetAccountInfo(private val _name: String) : SendablePacket() {

    override val opCode: Byte = 0x04

    override fun writeImpl(byteBuf: ByteBuf): ByteBuf {
        writeString(byteBuf, _name)
        return byteBuf
    }

}