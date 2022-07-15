package l2s.gameserver.network.l2.s2c

import l2s.commons.network.PacketWriter
import l2s.gameserver.network.l2.OutgoingExPackets

class ExAutoplayDoMacro(private val macroIndex: Int) : IClientOutgoingPacket {

    override fun write(packetWriter: PacketWriter): Boolean {
        OutgoingExPackets.EX_AUTOPLAY_DO_MACRO.writeId(packetWriter)

        packetWriter.writeD(macroIndex)

        return true
    }

    companion object {

        val AUTOPLAY_MACRO = ExAutoplayDoMacro(276)

    }

}