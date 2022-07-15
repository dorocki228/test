package l2s.gameserver.network.l2.s2c

import l2s.commons.network.PacketWriter
import l2s.gameserver.model.AutoplaySettings
import l2s.gameserver.network.l2.OutgoingExPackets

/**
 * @author Java-man
 */
class ExAutoplaySetting(
    private val autoplaySettings: AutoplaySettings
) : IClientOutgoingPacket {

    override fun write(packetWriter: PacketWriter): Boolean {
        OutgoingExPackets.EX_AUTOPLAY_SETTING.writeId(packetWriter)

        autoplaySettings.write(packetWriter)

        return true
    }

}