package l2s.gameserver.features.foursepulchers.instances

import java.util.concurrent.Future

import l2s.commons.collections.MultiValueSet
import l2s.commons.util.Rnd

import l2s.gameserver.ThreadPoolManager
import l2s.gameserver.model.Creature
import l2s.gameserver.model.Player
import l2s.gameserver.model.Skill
import l2s.gameserver.model.instances.MonsterInstance
import l2s.gameserver.network.l2.components.ChatType
import l2s.gameserver.network.l2.components.NpcString
import l2s.gameserver.data.xml.holder.SkillHolder
import l2s.gameserver.network.l2.s2c.NpcSay
import l2s.gameserver.templates.npc.NpcTemplate
import l2s.gameserver.utils.ItemFunctions
import l2s.gameserver.features.foursepulchers.FourSepulchersHall

class SepulcherMonsterInstance(objectId: Int, template: NpcTemplate, set: MultiValueSet<String>) :
    MonsterInstance(objectId, template, set) {
    private var _victimShout: Future<*>? = null
    private var _victimSpawnKeyBoxTask: Future<*>? = null
    private var _changeImmortalTask: Future<*>? = null
    private var _onDeadEventTask: Future<*>? = null

    override fun onSpawn() {
        when (npcId) {
            18150, 18151, 18152, 18153, 18154, 18155, 18156, 18157 -> {
                if (_victimSpawnKeyBoxTask != null)
                    _victimSpawnKeyBoxTask!!.cancel(false)
                _victimSpawnKeyBoxTask = ThreadPoolManager.getInstance().schedule(VictimSpawnKeyBox(this), 300000)
                if (_victimShout != null)
                    _victimShout!!.cancel(false)
                _victimShout = ThreadPoolManager.getInstance().schedule(VictimShout(this), 5000)
            }
            18196, 18197, 18198, 18199, 18200, 18201, 18202, 18203, 18204, 18205, 18206, 18207, 18208, 18209, 18210, 18211 -> {
            }
            18231, 18232, 18233, 18234, 18235, 18236, 18237, 18238, 18239, 18240, 18241, 18242, 18243 -> {
                if (_changeImmortalTask != null)
                    _changeImmortalTask!!.cancel(false)
                _changeImmortalTask = ThreadPoolManager.getInstance().schedule(ChangeImmortal(this), 1600)
            }
            18256 -> {
            }
        }
        super.onSpawn()
    }

    override fun onDeath(killer: Creature) {
        super.onDeath(killer)

        when (npcId) {
            18120, 18121, 18122, 18123, 18124, 18125, 18126, 18127, 18128, 18129, 18130, 18131, 18149, 18158, 18159, 18160, 18161, 18162, 18163, 18164, 18165, 18183, 18184, 18212, 18213, 18214, 18215, 18216, 18217, 18218, 18219 -> {
                if (_onDeadEventTask != null)
                    _onDeadEventTask!!.cancel(false)
                _onDeadEventTask = ThreadPoolManager.getInstance().schedule(OnDeadEvent(this), 3500)
            }

            18150, 18151, 18152, 18153, 18154, 18155, 18156, 18157 -> {
                if (_victimSpawnKeyBoxTask != null) {
                    _victimSpawnKeyBoxTask!!.cancel(false)
                    _victimSpawnKeyBoxTask = null
                }
                if (_victimShout != null) {
                    _victimShout!!.cancel(false)
                    _victimShout = null
                }
                if (_onDeadEventTask != null)
                    _onDeadEventTask!!.cancel(false)
                _onDeadEventTask = ThreadPoolManager.getInstance().schedule(OnDeadEvent(this), 3500)
            }

            18141, 18142, 18143, 18144, 18145, 18146, 18147, 18148 -> {
                val param = (getParameter("four_sepulchers_hall")
                    ?: throw IllegalArgumentException("Can't find hall for npc $npcId"))
                val hall = param as FourSepulchersHall
                val parentNpcId = getParameter("parent_npc_id", -1)
                require(parentNpcId != -1) { "Can't find parent npc for npc $npcId" }

                if (hall.isViscountMobsAnnihilated(parentNpcId) && !hasPartyAKey(killer.player!!)) {
                    if (_onDeadEventTask != null)
                        _onDeadEventTask!!.cancel(false)
                    _onDeadEventTask = ThreadPoolManager.getInstance().schedule(OnDeadEvent(this), 3500)
                }
            }
            18220, 18221, 18222, 18223, 18224, 18225, 18226, 18227, 18228, 18229, 18230, 18231, 18232, 18233, 18234, 18235, 18236, 18237, 18238, 18239, 18240 -> {
                val param = (getParameter("four_sepulchers_hall")
                    ?: throw IllegalArgumentException("Can't find hall for npc $npcId"))
                val hall = param as FourSepulchersHall
                val parentNpcId = getParameter("parent_npc_id", -1)
                require(parentNpcId != -1) { "Can't find parent npc for npc $npcId" }

                if (hall.isDukeMobsAnnihilated(parentNpcId)) {
                    if (_onDeadEventTask != null)
                        _onDeadEventTask!!.cancel(false)
                    _onDeadEventTask = ThreadPoolManager.getInstance().schedule(OnDeadEvent(this), 3500)
                }
            }
            18256 -> randomReward(killer)
        }
    }

    override fun hasRandomWalk(): Boolean {
        return if (npcId in 18231..18243) false else super.hasRandomWalk()
    }

    private inner class VictimShout(private val _activeChar: SepulcherMonsterInstance) : Runnable {

        override fun run() {
            if (_activeChar.isDead)
                return

            if (!_activeChar.isVisible)
                return

            broadcastPacket(NpcSay(this@SepulcherMonsterInstance, ChatType.NPC_ALL, NpcString.HELP_ME))
        }
    }

    private inner class VictimSpawnKeyBox(private val _activeChar: SepulcherMonsterInstance) : Runnable {

        override fun run() {
            if (_activeChar.isDead)
                return

            if (!_activeChar.isVisible)
                return

            val param = (getParameter("four_sepulchers_hall")
                ?: throw IllegalArgumentException("Can't find hall for npc $npcId"))
            val hall = param as FourSepulchersHall

            hall.spawnKeyBox(_activeChar)
            broadcastPacket(NpcSay(this@SepulcherMonsterInstance, ChatType.NPC_ALL, NpcString.THANK_YOU_FOR_SAVING_ME))
            if (_victimShout != null) {
                _victimShout!!.cancel(false)
                _victimShout = null
            }
        }
    }

    private inner class OnDeadEvent(private val npc: SepulcherMonsterInstance) : Runnable {

        override fun run() {
            val param = (getParameter("four_sepulchers_hall")
                ?: throw IllegalArgumentException("Can't find hall for npc $npcId"))
            val hall = param as FourSepulchersHall

            when (npc.npcId) {
                18120, 18121, 18122, 18123, 18124, 18125, 18126, 18127, 18128, 18129, 18130, 18131, 18149, 18158, 18159, 18160, 18161, 18162, 18163, 18164, 18165, 18183, 18184, 18212, 18213, 18214, 18215, 18216, 18217, 18218, 18219 -> hall.spawnKeyBox(
                    npc
                )

                18150, 18151, 18152, 18153, 18154, 18155, 18156, 18157 -> hall.spawnExecutionerOfHalisha(npc)

                18141, 18142, 18143, 18144, 18145, 18146, 18147, 18148 -> {
                    val parentNpcId = getParameter("parent_npc_id", -1)
                    require(parentNpcId != -1) { "Can't find parent npc for npc $npcId" }
                    hall.spawnMonster(parentNpcId)
                }
                18220, 18221, 18222, 18223, 18224, 18225, 18226, 18227, 18228, 18229, 18230, 18231, 18232, 18233, 18234, 18235, 18236, 18237, 18238, 18239, 18240 -> {
                    val parentNpcId = getParameter("parent_npc_id", -1)
                    require(parentNpcId != -1) { "Can't find parent npc for npc $npcId" }
                    hall.spawnArchonOfHalisha(parentNpcId)
                }
            }
        }
    }

    private inner class ChangeImmortal(private val activeChar: SepulcherMonsterInstance) : Runnable {

        override fun run() {
            // Invulnerable by petrification
            val fp = SkillHolder.getInstance().getSkill(4616, 1) ?: return
            fp.getEffects(activeChar, activeChar)
        }
    }

    private fun hasPartyAKey(player: Player): Boolean {
        if (player.party == null)
            return false

        for (m in player.party.partyMembers)
            if (ItemFunctions.getItemCount(m, HALLS_KEY) > 0)
                return true
        return false
    }

    private fun randomReward(killer: Creature) {
        val id = npcId
        if (id == 18256) {
            when {
                Rnd.chance(20) -> dropItem(killer.player, 57, 10000)
                Rnd.chance(20) -> dropItem(killer.player, 57, 100000)
                Rnd.chance(20) -> dropItem(killer.player, 57, 200000)
                Rnd.chance(20) -> dropItem(killer.player, 57, 1000000)
                Rnd.chance(10) -> dropItem(killer.player, 2133, 1)
                Rnd.chance(15) -> dropItem(killer.player, 91397, 2)
                Rnd.chance(15) -> dropItem(killer.player, 91398, 1)
                Rnd.chance(20) -> dropItem(killer.player, 91393, 1)
                Rnd.chance(20) -> dropItem(killer.player, 91394, 1)
                Rnd.chance(20) -> dropItem(killer.player, 91577, 1)
                Rnd.chance(7) -> dropItem(killer.player, 49786, 1)
                Rnd.chance(7) -> dropItem(killer.player, 49785, 1)
                else -> dropItem(killer.player, Rnd.get(6688, 6714), 1)
            }
        }
    }

    override fun canChampion(): Boolean {
        return false
    }

    companion object {
        private const val HALLS_KEY = 7260
    }
}