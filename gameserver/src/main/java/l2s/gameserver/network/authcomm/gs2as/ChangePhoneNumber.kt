package l2s.gameserver.network.authcomm.gs2as

import io.netty.buffer.ByteBuf
import l2s.gameserver.network.authcomm.SendablePacket

/**
 * @author Bonux
 */
class ChangePhoneNumber(private val _account: String, private val _phoneNumber: Long) : SendablePacket() {

    override val opCode: Byte = 0x0C

    override fun writeImpl(byteBuf: ByteBuf): ByteBuf {
        writeString(byteBuf, _account)
        byteBuf.writeLong(_phoneNumber)

        return byteBuf
    }

}