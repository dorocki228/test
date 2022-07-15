package l2s.gameserver.network.authcomm.gs2as

import io.netty.buffer.ByteBuf
import l2s.gameserver.network.authcomm.SendablePacket

/**
 * @Author: Death
 * @Date: 8/2/2007
 * @Time: 14:35:35
 */

class ChangePassword(
    private val _account: String,
    private val _oldPass: String,
    private val _newPass: String,
    private val _hwid: String
) : SendablePacket() {

    override val opCode: Byte = 0x08

    override fun writeImpl(byteBuf: ByteBuf): ByteBuf {
        writeString(byteBuf, _account)
        writeString(byteBuf, _oldPass)
        writeString(byteBuf, _newPass)
        writeString(byteBuf, _hwid)

        return byteBuf
    }

}