package l2s.gameserver.network.l2.s2c

import l2s.commons.network.PacketWriter
import l2s.gameserver.model.actor.instances.player.ShortCut
import l2s.gameserver.network.l2.OutgoingExPackets

/**
 * @author Java-man
 */
class SExActivateAutoShortcut private constructor(
    private val slotId: Int,
    private val action: Int,
    private val value: Int
) : IClientOutgoingPacket {

    override fun write(packetWriter: PacketWriter): Boolean {
        OutgoingExPackets.EX_ACTIVATE_AUTO_SHORTCUT.writeId(packetWriter)

        packetWriter.writeC(slotId)
        packetWriter.writeC(action)
        packetWriter.writeC(value)

        return true
    }

    companion object {

        fun enable(shortcut: ShortCut): SExActivateAutoShortcut {
            return when {
                shortcut.type == ShortCut.ShortCutType.ITEM ->
                    SExActivateAutoShortcut(shortcut.slot + 8, 1, 1)
                shortcut.type == ShortCut.ShortCutType.SKILL ->
                    SExActivateAutoShortcut(shortcut.index, 0, 1)
                else ->
                    error("Can't find type.")
            }
        }

        fun disable(shortcut: ShortCut): SExActivateAutoShortcut {
            return when {
                shortcut.type == ShortCut.ShortCutType.ITEM ->
                    SExActivateAutoShortcut(shortcut.slot + 8, 1, 0)
                shortcut.type == ShortCut.ShortCutType.SKILL ->
                    SExActivateAutoShortcut(shortcut.index, 0, 0)
                else ->
                    error("Can't find type.")
            }
        }

    }

}