package l2s.gameserver.network.l2.c2s

import l2s.commons.network.PacketReader
import l2s.gameserver.network.l2.GameClient

class RequestOpenWndWithoutNPC : IClientIncomingPacket {

    override fun readImpl(
        client: GameClient,
        packet: PacketReader
    ): Boolean {
        return true
    }

    override fun run(client: GameClient) {
        val player = client.activeChar ?: return

        // TODO add handler
    }

}