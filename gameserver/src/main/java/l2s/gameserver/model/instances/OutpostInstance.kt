package l2s.gameserver.model.instances

import gve.util.GveMessageUtil
import gve.zones.model.GveOutpost
import l2s.commons.collections.MultiValueSet
import l2s.commons.geometry.Circle
import l2s.commons.util.Rnd
import l2s.gameserver.Announcements
import l2s.gameserver.ThreadPoolManager
import l2s.gameserver.listener.actor.OnDeathListener
import l2s.gameserver.listener.actor.OnReviveListener
import l2s.gameserver.listener.zone.OnZoneEnterLeaveListener
import l2s.gameserver.model.*
import l2s.gameserver.model.base.Experience
import l2s.gameserver.model.reward.RewardList
import l2s.gameserver.network.l2.components.CustomMessage
import l2s.gameserver.network.l2.s2c.AutoAttackStartPacket
import l2s.gameserver.network.l2.s2c.anticheat.AAScreenStringPacketPresets
import l2s.gameserver.service.BroadcastService
import l2s.gameserver.service.ConfrontationService
import l2s.gameserver.service.MoraleBoostService
import l2s.gameserver.taskmanager.LazyPrecisionTaskManager
import l2s.gameserver.templates.StatsSet
import l2s.gameserver.templates.ZoneTemplate
import l2s.gameserver.templates.item.ItemTemplate
import l2s.gameserver.templates.npc.NpcTemplate
import l2s.gameserver.time.counter.TimeCounter
import l2s.gameserver.time.counter.TimeElapsedAction
import l2s.gameserver.utils.ItemFunctions
import l2s.gameserver.utils.Language
import java.time.Duration
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

class OutpostInstance(objectId: Int, template: NpcTemplate, set: MultiValueSet<String>) :
        TeleporterInstance(objectId, template, set) {

    private var checkDefendZone: ScheduledFuture<*>? = null
    private lateinit var outpost: GveOutpost

    private val hpAnnounce = AtomicBoolean(false)

    private var defendZone: AtomicReference<Zone> = AtomicReference(null)
    private var defendZoneListener: OnZoneEnterLeaveListenerImpl? = null

    override fun onSpawn() {
        super.onSpawn()

        outpost = GveOutpost.valueOf(spawn.group)
    }

    override fun onDeath(killer: Creature) {
        stopDefend()

        hpAnnounce.set(false)
        if (killer.isPlayable) {
            MoraleBoostService.getInstance().outpostDestroy(this, killer.playable)
            ConfrontationService.getInstance().outpostDestroy(this, killer.playable)
        }
        super.onDeath(killer)

        val message =
                outpost.getName(Language.ENGLISH) + ' '.toString() + fraction + " outpost was destroyed! Recovery time - 5 minutes!"
        Announcements.announceToAll(message)
    }

    override fun onReduceCurrentHp(
            damage: Double,
            attacker: Creature,
            skill: Skill,
            awake: Boolean,
            standUp: Boolean,
            directHp: Boolean,
            isDot: Boolean
    ) {
        val hpPercents = currentHpPercents

        if (hpPercents <= 95) {
            startDefend()
        }

        if (hpPercents <= 40) {
            if (hpAnnounce.compareAndSet(false, true)) {
                val text = (outpost.getName(Language.ENGLISH) + " [" + fraction
                        + "] outpost have less then 40% HP! Need help!")
                val announce = AAScreenStringPacketPresets.ANNOUNCE.addOrUpdate(text)
                BroadcastService.getInstance().sendToAll(announce)
            }
        }

        super.onReduceCurrentHp(damage, attacker, skill, awake, standUp, directHp, isDot)
    }

    fun startDefend() {
        val tempZone = createDefendZone()
        if (defendZone.compareAndSet(null, tempZone)) {
            val delay = Duration.ofSeconds(30)
//            TimeCounter.start(this, "DEFEND_ZONE", TimeElapsedActionImpl(this, "DEFEND_ZONE"), delay, true)

            defendZoneListener = OnZoneEnterLeaveListenerImpl(this, tempZone)
            tempZone.addListener(defendZoneListener)
            tempZone.isActive = true

            ThreadPoolManager.getInstance().schedule(CheckDefendZone(this), 15, TimeUnit.MINUTES)
        }
    }

    fun stopDefend() {
        destroyDefendZone()
    }

    private fun createDefendZone(): Zone {
        val c = Circle(loc, 5000)
        c.zmax = World.MAP_MAX_Z
        c.zmin = World.MAP_MIN_Z

        val set = StatsSet()
        set.set("name", "outpost defend zone")
        set.set("type", Zone.ZoneType.defend)
        set.set("territory", Territory().add(c))

        val zone = Zone(ZoneTemplate(set))
        zone.reflection = reflection

        return zone
    }

    private fun destroyDefendZone() {
        val temp = defendZone.getAndSet(null)
        if (temp != null) {
            temp.isActive = false
            temp.removeListener(defendZoneListener)
//            TimeCounter.stop(this, "DEFEND_ZONE")
            stopCheckDefendZone()
        }
    }

    override fun startAttackStanceTask0() {
        if (isInCombat) {
            _stanceEndTime = System.currentTimeMillis() + STANCE_TIME
            return
        }

        _stanceEndTime = System.currentTimeMillis() + STANCE_TIME
        broadcastPacket(AutoAttackStartPacket(getObjectId()))
        val task = _stanceTask
        task?.cancel(false)
        if (_stanceTaskRunnable == null)
            _stanceTaskRunnable = AttackStanceTask()
        _stanceTask = LazyPrecisionTaskManager.getInstance().scheduleAtFixedRate(_stanceTaskRunnable, 1000L, 1000L)

        GveMessageUtil.updateProtectMessage(fraction)

        val message = outpost.getName(Language.ENGLISH) + " [" + fraction + "] outpost was attacked! Need help!"
        Announcements.announceToAll(message)
        val attackedAnnounce = AAScreenStringPacketPresets.ANNOUNCE.addOrUpdate(message)
        BroadcastService.getInstance().sendToAll(attackedAnnounce)
    }

    override fun stopAttackStanceTask() {
        super.stopAttackStanceTask()
        GveMessageUtil.updateProtectMessage(fraction)
    }

    override fun isAttackable(attacker: Creature): Boolean {
        return isAutoAttackable(attacker)
    }

    override fun isAutoAttackable(attacker: Creature): Boolean {
        return fraction.canAttack(attacker.fraction)
    }

    override fun showChatWindow(player: Player, `val`: Int, firstTalk: Boolean, vararg replace: Any) {
        if (!fraction.canAttack(player.fraction))
            super.showChatWindow(player, `val`, firstTalk, *replace)
    }

    override fun isPeaceNpc(): Boolean {
        return false
    }

    override fun isThrowAndKnockImmune(): Boolean {
        return true
    }

    override fun isOutpost(): Boolean {
        return true
    }

    override fun rollRewards(list: RewardList, lastAttacker: Creature, topDamager: Creature) {
        val activePlayer = topDamager.player ?: return

        val penaltyMod = Experience.penaltyModifier(calculateLevelDiffForDrop(topDamager.level).toLong())

        val rewardItems = list.roll(activePlayer, penaltyMod, this)

        val dropPlayers = aggroList.playableMap.entries
                .filter { entry -> entry.value.damage >= maxHp * 0.02 }
                .map { it.key.player }
                .toSet()
        rewardItems.forEach { drop -> dropItemToTheGround(dropPlayers, drop.itemId, drop.count) }

        aggroList.playableMap.entries
                .filter { entry -> entry.value.damage >= 1 }
                .map { it.key.player }
                .distinct()
                .forEach { player ->
                    ItemFunctions.addItem(player, 57, 50)
                    if (Rnd.chance(20))
                        ItemFunctions.addItem(player, 75002, 1)
                }
    }

    fun stopCheckDefendZone(){
        checkDefendZone?.cancel(false)
        checkDefendZone = null
    }

    fun stopAndStartCheckDefendZone(delay : Long, timeUnit : TimeUnit) {
        stopCheckDefendZone()
        checkDefendZone = ThreadPoolManager.getInstance().schedule(CheckDefendZone(this), delay, timeUnit)
    }

    companion object {

        // TODO: move to npc xml
        const val FIRE_FLAG = 40031
        const val WATER_FLAG = 40038

        private val STANCE_TIME = TimeUnit.MINUTES.toMillis(5)

        class CheckDefendZone(private val npc: OutpostInstance) : Runnable {

            override fun run() {
                if (npc.currentHpPercents <= 95 && !npc.isDead) {
                    val zone = npc.defendZone.get()
                    if(zone != null) {
                        zone.isActive = false
                        zone.isActive = true
                    }
                    npc.stopAndStartCheckDefendZone(15, TimeUnit.MINUTES)
                } else {
                    npc.stopDefend()
                }
            }

        }

        class OnZoneEnterLeaveListenerImpl(private val npc: OutpostInstance, defendZone: Zone) :
                OnZoneEnterLeaveListener {

            private val listener: PlayerListenerImpl = PlayerListenerImpl(npc, defendZone)

            override fun onZoneEnter(zone: Zone?, creature: Creature?) {
                if (zone == null || creature == null) {
                    return
                }

                if (!creature.isPlayer) {
                    return
                }

                if (creature.player.fraction != npc.fraction) {
                    return
                }

//                TimeCounter.addPlayer(npc, "DEFEND_ZONE", creature.player)

                creature.addListener(listener)

                creature.player.unsetVar("defend_value")
            }

            override fun onZoneLeave(zone: Zone?, creature: Creature?) {
                if (zone == null || creature == null) {
                    return
                }

                if (!creature.isPlayer) {
                    return
                }

                if (creature.player.fraction != npc.fraction) {
                    return
                }

                creature.removeListener(listener)

//                TimeCounter.removePlayer(npc, "DEFEND_ZONE", creature.player)
            }

        }

        class PlayerListenerImpl(private val npc: OutpostInstance, private val defendZone: Zone) : OnDeathListener,
                OnReviveListener {

            override fun onDeath(victim: Creature?, killer: Creature?) {
                if (victim == null) {
                    return
                }

                if (!victim.isPlayer) {
                    return
                }

//                TimeCounter.removePlayer(npc, "DEFEND_ZONE", victim.player)
            }

            override fun onRevive(creature: Creature?) {
                if (creature == null) {
                    return
                }

                if (!creature.isPlayer) {
                    return
                }

                if (!creature.isInZone(defendZone)) {
                    return
                }

//                TimeCounter.addPlayer(npc, "DEFEND_ZONE", creature.player)
            }

        }

        class TimeElapsedActionImpl(owner: Any, counter: Any) : TimeElapsedAction(owner, counter) {

            override fun action(player: OfflinePlayer) {
                val defendTimeCounter = player.getVarInt("defend_value_counter", 0)
                if (player.getVarInt("defend_value", 0) >= 5 && defendTimeCounter >= 6) {
                    //player.unsetVar("defend_value")
                    player.unsetVar("defend_value_counter")

                    player.addItem(ItemTemplate.ITEM_ID_ADENA, 50, description = "OutpostDefend")
                    player.sendMessage(CustomMessage("l2s.gameserver.model.instances.OutpostInstance.defense.reward"))
                } else {
                    player.setVar("defend_value_counter", defendTimeCounter + 1)
                }
            }

        }

    }

}
