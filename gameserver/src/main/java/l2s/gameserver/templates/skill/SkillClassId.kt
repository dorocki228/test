package l2s.gameserver.templates.skill

import l2s.commons.network.PacketWriter

/**
 * @author Java-man
 * @since 04.08.2019
 */
data class SkillClassId(private val value: Int) {

    fun write(packetWriter: PacketWriter) {
        packetWriter.writeD(value)
    }

}