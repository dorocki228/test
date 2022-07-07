package l2s.gameserver.handler.admincommands.impl

import l2s.gameserver.handler.admincommands.IAdminCommandHandler
import l2s.gameserver.model.Player
import l2s.gameserver.network.l2.s2c.anticheat.AAScreenStringPacket
import java.time.Duration

/**
 * @author Java-man
 * @since 15.05.2019
 */
object AdminScreenString : IAdminCommandHandler {

    private enum class Commands {
        admin_ssa,
        admin_ssr
    }

    override fun getAdminCommandEnum(): Array<Enum<*>> {
        return Commands.values() as Array<Enum<*>>
    }

    override fun useAdminCommand(
        comm: Enum<*>,
        wordList: Array<String>,
        fullString: String,
        activeChar: Player
    ): Boolean {
        if (comm == Commands.admin_ssa) {
            val id = wordList[1].toInt()
            val text = wordList[2]
            val screenPosX = wordList[3].toInt()
            val screenPosY = wordList[4].toInt()
            val offsetX = wordList[5].toInt()
            val offsetY = wordList[6].toInt()
            val color = AAScreenStringPacket.Color(wordList[7].toInt(), wordList[8].toInt(), wordList[9].toInt())
            val fontSize = wordList[10].toInt()
            val showTime = Duration.ofMillis(wordList[11].toLong())

            val packet = AAScreenStringPacket.addOrEdit(
                id, text, screenPosX, screenPosY, offsetX, offsetY, color, fontSize, showTime
            )
            activeChar.sendPacket(packet)

            return true
        } else if (comm == Commands.admin_ssr) {
            val id = wordList[1].toInt()
            val packet = AAScreenStringPacket.remove(id)
            activeChar.sendPacket(packet)
        }

        return false
    }

}