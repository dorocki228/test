package l2s.gameserver.network.authcomm.gs2as

import io.netty.buffer.ByteBuf
import l2s.gameserver.network.authcomm.SendablePacket

/**
 * @author VISTALL
 * @date 21:07/25.03.2011
 */
class SetAccountInfo(private val _account: String, private val _size: Int, private val _deleteChars: IntArray) :
    SendablePacket() {

    override val opCode: Byte = 0x05

    override fun writeImpl(byteBuf: ByteBuf): ByteBuf {
        writeString(byteBuf, _account)
        byteBuf.writeByte(_size)
        byteBuf.writeInt(_deleteChars.size)
        for (i in _deleteChars) byteBuf.writeInt(i)
        return byteBuf
    }

}