package l2s.gameserver.network.l2.s2c

import l2s.commons.network.PacketWriter
import l2s.gameserver.model.Player
import l2s.gameserver.network.l2.OutgoingExPackets

/**
 * @author Java-man
 *
 * TODO
 */
class ExBloodyCoinCount(player: Player) : IClientOutgoingPacket {

    override fun write(packetWriter: PacketWriter): Boolean {
        OutgoingExPackets.EX_BLOODY_COIN_COUNT.writeId(packetWriter)

        packetWriter.writeD(0x00)
        packetWriter.writeD(0x00)

        return true
    }

}