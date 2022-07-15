package l2s.authserver.network.gamecomm.as2gs

import io.netty.buffer.ByteBuf
import l2s.authserver.network.gamecomm.SendablePacket

/**
 * @reworked by Bonux
 */
class LoginServerFail(private val reason: String, private val restartConnection: Boolean) : SendablePacket() {

    override val opCode: Byte = 0x01

    override fun writeImpl(byteBuf: ByteBuf): ByteBuf {
        byteBuf.writeByte(0x00) // Reason ID

        writeString(byteBuf, reason)
        byteBuf.writeByte(if (restartConnection) 0x01 else 0x00)

        return byteBuf
    }

}