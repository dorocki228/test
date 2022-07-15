package l2s.gameserver.network.authcomm.as2gs

import io.netty.buffer.ByteBuf
import l2s.gameserver.network.authcomm.AuthServerClientService
import l2s.gameserver.network.authcomm.ReceivablePacket
import l2s.gameserver.network.authcomm.vertx.AuthServerConnection
import l2s.gameserver.network.l2.components.CustomMessage
import l2s.gameserver.network.l2.s2c.ExShowScreenMessage
import l2s.gameserver.network.l2.s2c.ExShowScreenMessage.ScreenMessageAlign

/**
 * @Author: Death
 * @Date: 8/2/2007
 * @Time: 14:39:46
 */
class ChangePasswordResponse(client: AuthServerConnection, byteBuf: ByteBuf) : ReceivablePacket(client, byteBuf) {

    private val _account: String = readString(byteBuf)
    private val _changed = byteBuf.readInt() == 1

    override fun runImpl(client: AuthServerConnection) {
        val client = AuthServerClientService.getAuthedClient(_account) ?: return
        val activeChar = client.activeChar ?: return
        if (_changed) activeChar.sendPacket(
            ExShowScreenMessage(
                CustomMessage(
                    "scripts.commands.user.password.ResultTrue"
                ).toString(activeChar),
                3000,
                ScreenMessageAlign.BOTTOM_CENTER,
                true
            )
        ) else activeChar.sendPacket(
            ExShowScreenMessage(
                CustomMessage(
                    "scripts.commands.user.password.ResultFalse"
                ).toString(activeChar),
                3000,
                ScreenMessageAlign.BOTTOM_CENTER,
                true
            )
        )
    }
}