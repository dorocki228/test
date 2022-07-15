package l2s.gameserver.model

import l2s.commons.network.PacketWriter

/**
 * @author Java-man
 * @since 17.08.2019
 */
data class PledgeName(private val value: String) {

    fun write(packetWriter: PacketWriter) {
        packetWriter.writeS(value) // pledge name
    }

}