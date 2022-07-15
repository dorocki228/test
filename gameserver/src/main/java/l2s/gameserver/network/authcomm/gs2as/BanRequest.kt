package l2s.gameserver.network.authcomm.gs2as

import io.netty.buffer.ByteBuf
import l2s.commons.ban.BanBindType
import l2s.gameserver.network.authcomm.SendablePacket

/**
 * @author Bonux (Head Developer L2-scripts.com)
 * 10.04.2019
 * Developed for L2-Scripts.com
 */
class BanRequest(
    private val bindType: BanBindType,
    private val bindValue: String,
    private val endTime: Int,
    private val reason: String
) : SendablePacket() {

    override val opCode: Byte = 0x13

    override fun writeImpl(byteBuf: ByteBuf): ByteBuf {
        byteBuf.writeByte(bindType.ordinal)
        writeString(byteBuf, bindValue)
        byteBuf.writeInt(endTime)
        writeString(byteBuf, reason)

        return byteBuf
    }

}