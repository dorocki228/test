package l2s.gameserver.model.instances

import l2s.commons.collections.MultiValueSet
import l2s.commons.geometry.Circle
import l2s.gameserver.ThreadPoolManager
import l2s.gameserver.entity.ArtifactEntity
import l2s.gameserver.listener.actor.OnDeathListener
import l2s.gameserver.listener.actor.OnReviveListener
import l2s.gameserver.listener.zone.OnZoneEnterLeaveListener
import l2s.gameserver.model.*
import l2s.gameserver.network.l2.components.CustomMessage
import l2s.gameserver.templates.StatsSet
import l2s.gameserver.templates.ZoneTemplate
import l2s.gameserver.templates.item.ItemTemplate
import l2s.gameserver.templates.npc.NpcTemplate
import l2s.gameserver.time.counter.TimeCounter
import l2s.gameserver.time.counter.TimeElapsedAction
import java.time.Duration
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

class ArtifactInstance(objectId: Int, template: NpcTemplate, set: MultiValueSet<String>) :
    NpcInstance(objectId, template, set) {

    private var checkDefendZone: ScheduledFuture<*>? = null
    lateinit var entity: ArtifactEntity

    private var defendZone: AtomicReference<Zone> = AtomicReference(null)
    private var defendZoneListener: OnZoneEnterLeaveListenerImpl? = null

    init {
        isHasChatWindow = false
    }

    override fun isAutoAttackable(attacker: Creature): Boolean {
        return false
    }

    override fun isAttackable(attacker: Creature): Boolean {
        return false
    }

    override fun onDeath(killer: Creature) {
        entity.stopCommunityBoardEntry()

        stopDefend()
    }

    fun startDefend() {
        val tempZone = createDefendZone()
        if (defendZone.compareAndSet(null, tempZone)) {
            val delay = Duration.ofSeconds(30)
//            TimeCounter.start(this, "DEFEND_ZONE_ARTIFACT", TimeElapsedActionImpl(this, "DEFEND_ZONE_ARTIFACT"), delay, true)

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
        set.set("name", "artifact defend zone")
        set.set("type", Zone.ZoneType.defendArtifact)
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

//            TimeCounter.stop(this, "DEFEND_ZONE_ARTIFACT")
            stopCheckDefendZone()
            entity.endProtect = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(30)
        }
    }

    private fun stopCheckDefendZone(){
        checkDefendZone?.cancel(false)
        checkDefendZone = null
    }

    companion object {

        class CheckDefendZone(private val npc: ArtifactInstance) : Runnable {
            override fun run() {
                npc.stopDefend()
            }
        }

        class OnZoneEnterLeaveListenerImpl(private val npc: ArtifactInstance, defendZone: Zone) : OnZoneEnterLeaveListener {

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

//                TimeCounter.addPlayer(npc, "DEFEND_ZONE_ARTIFACT", creature.player)

                creature.addListener(listener)

                creature.player.unsetVar("artifact_defend_value")
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

//                TimeCounter.removePlayer(npc, "DEFEND_ZONE_ARTIFACT", creature.player)
            }

        }

        class PlayerListenerImpl(private val npc: ArtifactInstance, private val defendZone: Zone) : OnDeathListener, OnReviveListener {

            override fun onDeath(victim: Creature?, killer: Creature?) {
                if (victim == null) {
                    return
                }

                if (!victim.isPlayer) {
                    return
                }

//                TimeCounter.removePlayer(npc, "DEFEND_ZONE_ARTIFACT", victim.player)
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

//                TimeCounter.addPlayer(npc, "DEFEND_ZONE_ARTIFACT", creature.player)
            }

        }

        class TimeElapsedActionImpl(owner: Any, counter: Any) : TimeElapsedAction(owner, counter) {

            override fun action(player: OfflinePlayer) {
                val defendTimeCounter = player.getVarInt("artifact_defend_value_counter", 0)
                if (player.getVarInt("artifact_defend_value", 0) >= 5 && defendTimeCounter >= 6) {
                    //player.unsetVar("artifact_defend_value")
                    player.unsetVar("artifact_defend_value_counter")

                    player.addItem(ItemTemplate.ITEM_ID_ADENA, 50, description = "ArtifactDefend")
                    player.sendMessage(CustomMessage("l2s.gameserver.model.instances.ArtifactInstance.defense.reward"))
                } else {
                    player.setVar("artifact_defend_value_counter", defendTimeCounter + 1)
                }
            }

        }

    }

}
