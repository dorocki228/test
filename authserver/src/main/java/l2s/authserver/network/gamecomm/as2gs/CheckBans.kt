package l2s.authserver.network.gamecomm.as2gs

import io.netty.buffer.ByteBuf
import l2s.authserver.network.gamecomm.SendablePacket
import l2s.commons.ban.BanBindType
import l2s.commons.ban.BanInfo

/**
 * @author Bonux (Head Developer L2-scripts.com)
 * 10.04.2019
 * Developed for L2-Scripts.com
 */
class CheckBans(
    private val bindType: BanBindType,
    private val bans: Map<String, BanInfo>
) : SendablePacket() {

    override val opCode: Byte = 0x07

    override fun writeImpl(byteBuf: ByteBuf): ByteBuf {
        byteBuf.writeByte(bindType.ordinal)

        byteBuf.writeShort(bans.size)
        for ((key, value) in bans) {
            writeString(byteBuf, key)
            byteBuf.writeInt(value.endTime)
            writeString(byteBuf, value.reason)
        }

        return byteBuf
    }

}