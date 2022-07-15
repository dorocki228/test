package l2s.gameserver.network.authcomm.gs2as

import io.netty.buffer.ByteBuf
import l2s.gameserver.network.authcomm.SendablePacket

class ChangeAllowedHwid(private val account: String, private val hwid: String) : SendablePacket() {

    override val opCode: Byte = 0x09

    override fun writeImpl(byteBuf: ByteBuf): ByteBuf {
        writeString(byteBuf, account)
        writeString(byteBuf, hwid)

        return byteBuf
    }

}