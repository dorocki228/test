package l2s.gameserver.network.l2.c2s

import l2s.commons.network.PacketReader
import l2s.gameserver.model.Player
import l2s.gameserver.model.actor.instances.player.ShortCut
import l2s.gameserver.model.actor.instances.player.ShortCutList
import l2s.gameserver.network.l2.GameClient

/**
 * @author Java-man
 */
class CExActivateAutoShortcut : IClientIncomingPacket {

    private var slotId: Short = 0
    private var action: Short = 0
    private var value: Short = 0 // enable = 1 / disable = 0

    override fun readImpl(
        client: GameClient,
        packet: PacketReader
    ): Boolean {
        slotId = packet.readC()
        action = packet.readC()
        value = packet.readC()
        return true
    }

    override fun run(client: GameClient) {
        val player = client.activeChar ?: return
        val actionType = ActionType.findById(action.toInt())
        when {
            actionType != null ->
                actionType.execute(value.toInt(), slotId.toInt(), player)
            else ->
                player.sendActionFailed()
        }
    }

    companion object {

        private enum class ActionType(private val id: Int) {
            MANIPULATE_SKILL_AUTO_SHORTCUT(0x00) {
                override fun execute(value: Int, slotId: Int, player: Player) {
                    when (value) {
                        0 -> {
                            val slot = slotId % 12
                            val page = slotId / 12
                            val shortCut = player.getShortCut(slot, page) ?: return
                            player.disableAutoShortcut(player, shortCut)
                        }
                        1 -> {
                            val slot = slotId % 12
                            val page = slotId / 12
                            val shortCut = player.getShortCut(slot, page) ?: return
                            player.enableAutoShortcut(player, shortCut)
                        }
                        else ->
                            player.sendActionFailed()
                    }
                }
            },
            MANIPULATE_ITEM_AUTO_SHORTCUT(0x01) {
                override fun execute(value: Int, slotId: Int, player: Player) {
                    when (value) {
                        0 -> {
                            val shortCut = player.getShortCut(slotId - 8, ShortCut.PAGE_AUTO_USABLE_ITEMS) ?: return
                            player.disableAutoShortcut(player, shortCut)
                        }
                        1 -> {
                            val shortCut = player.getShortCut(slotId - 8, ShortCut.PAGE_AUTO_USABLE_ITEMS) ?: return
                            player.enableAutoShortcut(player, shortCut)
                        }
                        else ->
                            player.sendActionFailed()
                    }
                }
            },
            MANIPULATE_ALL_ITEM_AUTO_SHORTCUTS(0xFF) {
                override fun execute(value: Int, slotId: Int, player: Player) {
                    when (value) {
                        0 -> player.stopAutoShortcuts(ShortCutList.AutoShortCutType.ITEMS)
                        1 -> player.startAutoShortcuts(ShortCutList.AutoShortCutType.ITEMS)
                        else ->
                            player.sendActionFailed()
                    }
                }
            };

            abstract fun execute(value: Int, slotId: Int, player: Player)

            companion object {

                private val values = ActionType.values()

                fun findById(actionId: Int): ActionType? {
                    return values.find { it.id == actionId }
                }

            }

        }

    }

}