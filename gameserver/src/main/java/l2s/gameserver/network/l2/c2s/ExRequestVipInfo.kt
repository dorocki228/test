package l2s.gameserver.network.l2.c2s

import l2s.gameserver.network.l2.s2c.ExVipInfo

/**
 * @author Bonux
 */
class ExRequestVipInfo : IClientIncomingPacket {

    override fun readImpl(
        client: l2s.gameserver.network.l2.GameClient,
        packet: l2s.commons.network.PacketReader
    ): Boolean {
        return true
    }

    override fun run(client: l2s.gameserver.network.l2.GameClient) {
        val activeChar = client.activeChar ?: return

        activeChar.sendPacket(ExVipInfo(activeChar));
    }

}