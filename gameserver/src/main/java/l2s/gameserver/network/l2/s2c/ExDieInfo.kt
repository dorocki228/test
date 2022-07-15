package l2s.gameserver.network.l2.s2c

import l2s.commons.network.PacketWriter
import l2s.gameserver.model.DamageInfo
import l2s.gameserver.model.LostItems
import l2s.gameserver.model.items.ItemInstance
import l2s.gameserver.network.l2.OutgoingExPackets

class ExDieInfo(
    private val lostItems: LostItems,
    private val damageInfo: DamageInfo
) : IClientOutgoingPacket {

    override fun write(packetWriter: PacketWriter): Boolean {
        OutgoingExPackets.EX_DIE_INFO.writeId(packetWriter)

        lostItems.write(packetWriter, false)

        damageInfo.write(packetWriter)

        return true
    }

}