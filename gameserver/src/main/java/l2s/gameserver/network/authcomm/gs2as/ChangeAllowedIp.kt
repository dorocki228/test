package l2s.gameserver.network.authcomm.gs2as

import io.netty.buffer.ByteBuf
import l2s.gameserver.network.authcomm.SendablePacket

class ChangeAllowedIp(private val account: String, private val ip: String) : SendablePacket() {

    override val opCode: Byte = 0x07

    override fun writeImpl(byteBuf: ByteBuf): ByteBuf {
        writeString(byteBuf, account)
        writeString(byteBuf, ip)

        return byteBuf
    }

}