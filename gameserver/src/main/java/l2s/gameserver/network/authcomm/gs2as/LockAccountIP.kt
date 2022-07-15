package l2s.gameserver.network.authcomm.gs2as

import io.netty.buffer.ByteBuf
import l2s.gameserver.network.authcomm.SendablePacket

/**
 * @Author: SYS -> changed by Iqman 02.02.2012 12:12
 * @Date: 10/4/2008
 */
class LockAccountIP(private val _account: String, private val _IP: String, private val _time: Int) :
    SendablePacket() {

    override val opCode: Byte = 0x0b

    override fun writeImpl(byteBuf: ByteBuf): ByteBuf {
        writeString(byteBuf, _account)
        writeString(byteBuf, _IP)
        byteBuf.writeInt(_time)

        return byteBuf
    }

}