package l2s.gameserver.features.foursepulchers.instances

import l2s.commons.collections.MultiValueSet
import l2s.gameserver.ThreadPoolManager
import l2s.gameserver.instancemanager.ReflectionManager
import l2s.gameserver.model.Party
import l2s.gameserver.model.Player
import l2s.gameserver.model.instances.DoorInstance
import l2s.gameserver.model.instances.NpcInstance
import l2s.gameserver.model.items.ItemInstance
import l2s.gameserver.templates.npc.NpcTemplate
import l2s.gameserver.utils.ItemFunctions
import l2s.gameserver.features.foursepulchers.FourSepulchersHall
import l2s.gameserver.features.foursepulchers.FourSepulchersManager

import java.util.concurrent.Future

// TODO: Жертва должна бегать за открывателем сундука и просить о помощи.
open class SepulcherNpcInstance(objectId: Int, template: NpcTemplate, set: MultiValueSet<String>) :
    NpcInstance(objectId, template, set) {

    protected var _closeTask: Future<*>? = null
    protected var _spawnMonsterTask: Future<*>? = null

    override fun showChatWindow(player: Player, `val`: Int, firstTalk: Boolean, vararg replace: Any) {
        if (isDead) {
            player.sendActionFailed()
            return
        }

        if (npcId in 31468..31487) {
            doDie(player)
            if (_spawnMonsterTask != null)
                _spawnMonsterTask!!.cancel(false)
            _spawnMonsterTask = ThreadPoolManager.getInstance().schedule(SpawnMonster(this), 3500)
            return
        } else if (npcId in 31455..31467) {
            val party = player.party
            if (!hasPartyAKey(player) && (party != null && party.isLeader(player) || player.isGM)) {
                ItemFunctions.addItem(
                    player,
                    FourSepulchersManager.CHAPEL_KEY,
                    1/*, "Give items on talk with npc SepulcherNpcInstance"*/
                )
                doDie(player)
            }
            return
        }

        super.showChatWindow(player, `val`, firstTalk, *replace)
    }

    override fun getHtmlDir(filename: String, player: Player): String? {
        return HTML_FILE_PATH
    }

    override fun onBypassFeedback(player: Player, command: String) {
        if (command.equals("open_gate", ignoreCase = true)) {
            var hallsKey = player.inventory.getItemByItemId(FourSepulchersManager.CHAPEL_KEY)
            if (hallsKey == null)
                showChatWindow(player, HTML_FILE_PATH + "Gatekeeper-no.htm", false)
            else if (FourSepulchersManager.isAttackTime) {
                val param = (getParameter("four_sepulchers_hall")
                    ?: throw IllegalArgumentException("Can't find hall for npc $npcId"))
                val hall = param as FourSepulchersHall

                when (npcId) {
                    31929, 31934, 31939, 31944 -> if (!hall.isShadowAlive())
                        hall.spawnShadow()
                }

                // Moved here from switch-default
                openNextDoor()

                val party = player.party
                if (party != null) {
                    for (mem in party.partyMembers) {
                        hallsKey = mem.inventory.getItemByItemId(FourSepulchersManager.CHAPEL_KEY)
                        if (hallsKey != null)
                            ItemFunctions.deleteItem(mem, FourSepulchersManager.CHAPEL_KEY, hallsKey.count)
                    }
                } else if (hallsKey != null)
                    ItemFunctions.deleteItem(player, FourSepulchersManager.CHAPEL_KEY, hallsKey.count)
            }
        } else if (command.equals("exit", ignoreCase = true)) {
            FourSepulchersManager.exitPlayer(player)
        } else
            super.onBypassFeedback(player, command)
    }

    private fun openNextDoor() {
        val doorId = getParameter("door_id", 0)
        if (doorId > 0) {
            val door = ReflectionManager.MAIN.getDoor(doorId)
            if (door != null) {
                door.openMe()

                if (_closeTask != null)
                    _closeTask!!.cancel(false)
                _closeTask = ThreadPoolManager.getInstance().schedule(CloseNextDoor(door), 10000)
            }
        }
    }

    private inner class CloseNextDoor(private val door: DoorInstance) : Runnable {

        private var state = 0

        override fun run() {
            if (state == 0) {
                door.closeMe()
                state++
                _closeTask = ThreadPoolManager.getInstance().schedule(this, 10000)
            } else if (state == 1) {
                val hall = FourSepulchersManager.findHallByDoorId(door.doorId)
                hall.spawnMysteriousBox(npcId)
                _closeTask = null
            }
        }
    }

    private fun hasPartyAKey(player: Player): Boolean {
        val party = player.party
        if (party != null) {
            for (m in party.partyMembers)
                if (ItemFunctions.getItemCount(m, FourSepulchersManager.CHAPEL_KEY) > 0)
                    return true
        } else if (player.isGM) {
            if (ItemFunctions.getItemCount(player, FourSepulchersManager.CHAPEL_KEY) > 0)
                return true
        }
        return false
    }

    companion object {
        protected const val HTML_FILE_PATH = "four_sepulchers/"

        private class SpawnMonster(private val npc: SepulcherNpcInstance) : Runnable {

            override fun run() {
                val param = (npc.getParameter("four_sepulchers_hall")
                        ?: throw IllegalArgumentException("Can't find hall for npc ${npc.npcId}"))
                val hall = param as FourSepulchersHall
                hall.spawnMonster(npc.npcId)
            }

        }
    }
}