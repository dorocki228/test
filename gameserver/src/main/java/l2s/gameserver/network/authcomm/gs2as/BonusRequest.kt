package l2s.gameserver.network.authcomm.gs2as

import io.netty.buffer.ByteBuf
import l2s.gameserver.network.authcomm.SendablePacket

class BonusRequest(private val account: String, private val bonus: Int, private val bonusExpire: Int) :
    SendablePacket() {

    override val opCode: Byte = 0x10

    override fun writeImpl(byteBuf: ByteBuf): ByteBuf {
        writeString(byteBuf, account)
        byteBuf.writeInt(bonus)
        byteBuf.writeInt(bonusExpire)

        return byteBuf
    }

}