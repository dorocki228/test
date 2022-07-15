package l2s.gameserver.features.foursepulchers

import com.google.common.flogger.LazyArgs.lazy

import com.google.common.flogger.FluentLogger
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import l2s.commons.time.cron.SchedulingPattern
import l2s.gameserver.ThreadPoolManager
import l2s.gameserver.features.foursepulchers.instances.SepulcherNpcInstance
import l2s.gameserver.geometry.Location
import l2s.gameserver.instancemanager.ReflectionManager
import l2s.gameserver.instancemanager.SpawnManager
import l2s.gameserver.listener.actor.OnDeathListener
import l2s.gameserver.model.*
import l2s.gameserver.model.actor.listener.CharListenerList
import l2s.gameserver.network.l2.components.NpcString
import l2s.gameserver.scripts.annotation.OnScriptInit
import l2s.gameserver.utils.ChatUtils
import l2s.gameserver.utils.ItemFunctions
import l2s.gameserver.utils.ReflectionUtils
import l2s.gameserver.utils.TimeUtils

/**
 * @author Bonux (reworked)
 * @author Java-mam (reworked)
 */
class FourSepulchersManager {

    private class ProcessEventTask : Runnable {
        override fun run() {
            when {
                STATE.compareAndSet(
                    NONE_STATE,
                    ENTRY_STATE
                ) -> {
                    val changeWarmUpTime = startTime + TimeUnit.MINUTES.toMillis(3)
                    ThreadPoolManager.getInstance().execute(
                        ManagerSay(
                            0
                        )
                    )
                    eventProgressTask = ThreadPoolManager.getInstance()
                        .schedule(ProcessEventTask(), changeWarmUpTime - System.currentTimeMillis())
                }
                STATE.compareAndSet(
                    ENTRY_STATE,
                    WARM_UP_STATE
                ) -> {
                    val changeAttackTime = startTime + TimeUnit.MINUTES.toMillis(5)
                    eventProgressTask = ThreadPoolManager.getInstance()
                        .schedule(ProcessEventTask(), changeAttackTime - System.currentTimeMillis())
                }
                STATE.compareAndSet(
                    WARM_UP_STATE,
                    BATTLE_STATE
                ) -> {
                    halls.values.forEach {
                        it.spawnMysteriousBox(it.managerNpcId)
                    }

                    var durationMin = 5

                    var nextSayTime = startTime + TimeUnit.MINUTES.toMillis(10)
                    while (System.currentTimeMillis() > nextSayTime) {
                        durationMin += 5
                        nextSayTime += TimeUnit.MINUTES.toMillis(5)
                    }
                    managerSayTask = ThreadPoolManager.getInstance()
                        .schedule(ManagerSay(durationMin), nextSayTime - System.currentTimeMillis())

                    val attackEndTime = startTime + TimeUnit.MINUTES.toMillis((EVENT_DURATION + 5).toLong())
                    eventProgressTask = ThreadPoolManager.getInstance()
                        .schedule(ProcessEventTask(), attackEndTime - System.currentTimeMillis())
                }
                STATE.compareAndSet(
                    BATTLE_STATE,
                    NONE_STATE
                ) -> {
                    cleanUp()

                    startTime = START_TIME_PATTERN.next(System.currentTimeMillis())

                    LOGGER.atInfo()
                        .log("FourSepulchersManager: Entry time: %s", lazy { TimeUtils.toSimpleFormat(startTime) })

                    val interval = startTime - System.currentTimeMillis()
                    eventProgressTask = ThreadPoolManager.getInstance().schedule(
                        ProcessEventTask(), interval)
                }
            }
        }
    }

    private class ManagerSay(private val min: Int) : Runnable {

        override fun run() {
            if (isAttackTime) {
                if (min + 5 < 50) {
                    managerSay(min) //byte because minute cannot be more than 59
                    managerSayTask =
                        ThreadPoolManager.getInstance().schedule(
                            ManagerSay(
                                min + 5
                            ), TimeUnit.MINUTES.toMillis(5))
                } else
                    managerSay(90)// attack time ending chat
                //sending a unique id :D
            } else if (isEntryTime)
                managerSay(0)
        }
    }

    companion object {
        private val LOGGER = FluentLogger.forEnclosingClass()

        val zones = arrayOf(
            "[four_sepulchers_1]",
            "[four_sepulchers_2]",
            "[four_sepulchers_3]",
            "[four_sepulchers_4]"
        ).map {
            ReflectionUtils.getZone(it)
        }

        val MANAGERS_SPAWN_GROUP = "4_sepul_manager"

        val ENTRANCE_PASS = 91406
        val CHAPEL_KEY = 7260

        private val EXIT_LOC = Location(178296, -85256, -7218)

        private val START_TIME_PATTERN = SchedulingPattern("55 * * * *")
        private val EVENT_DURATION = 50 // In minutes

        private val NONE_STATE = 0
        private val ENTRY_STATE = 1
        private val WARM_UP_STATE = 2
        private val BATTLE_STATE = 3

        private val STATE = AtomicInteger(NONE_STATE)

        private var eventProgressTask: ScheduledFuture<*>? = null
        private var managerSayTask: ScheduledFuture<*>? = null

        private var startTime = 0L

        val halls = mapOf(
            1 to FourSepulchersHall.Companion.ConquerorsSepulcher(),
            2 to FourSepulchersHall.Companion.SepulcherOfRulers(),
            3 to FourSepulchersHall.Companion.GreatSagesSepulcher(),
            4 to FourSepulchersHall.Companion.JudgesSepulcher()
        )

        fun init() {
            CharListenerList.addGlobal(DeathListenerImpl())

            SpawnManager.getInstance().spawn(MANAGERS_SPAWN_GROUP, true)
            halls.values.forEach {
                it.spawnGateKeepers()
            }

            timeSelector()
        }

        // phase select on server launch
        private fun timeSelector() {
            startTime = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(365)

            val currentTime = System.currentTimeMillis()
            var entryTimeEnd: Long
            var warmUpTimeEnd: Long
            var attackTimeEnd: Long
            do {
                startTime = START_TIME_PATTERN.next(startTime)

                entryTimeEnd = startTime + TimeUnit.MINUTES.toMillis(3)
                warmUpTimeEnd = entryTimeEnd + TimeUnit.MINUTES.toMillis(2)
                attackTimeEnd = warmUpTimeEnd + TimeUnit.MINUTES.toMillis(EVENT_DURATION.toLong())
            } while (entryTimeEnd < currentTime && warmUpTimeEnd < currentTime && attackTimeEnd < currentTime)

            // if current time >= time of entry beginning and if current time < time of entry beginning + time of entry end
            if (currentTime >= startTime && currentTime < entryTimeEnd)
            // entry time check
            {
                cleanUp()
                eventProgressTask = ThreadPoolManager.getInstance().schedule(
                    ProcessEventTask(), 0)
                LOGGER.atInfo().log("FourSepulchersManager: Beginning in Entry time")
            } else if (currentTime >= entryTimeEnd && currentTime < warmUpTimeEnd)
            // warmup time check
            {
                cleanUp()
                STATE.set(ENTRY_STATE)
                eventProgressTask = ThreadPoolManager.getInstance().schedule(
                    ProcessEventTask(), 0)
                LOGGER.atInfo().log("FourSepulchersManager: Beginning in WarmUp time")
            } else if (currentTime >= warmUpTimeEnd && currentTime < attackTimeEnd)
            // attack time check
            {
                cleanUp()
                STATE.set(WARM_UP_STATE)
                eventProgressTask = ThreadPoolManager.getInstance().schedule(
                    ProcessEventTask(), 0)
                LOGGER.atInfo().log("FourSepulchersManager: Beginning in Attack time")
            } else
            // else cooldown time and without cleanup because it's already implemented
            {
                STATE.set(BATTLE_STATE)
                eventProgressTask = ThreadPoolManager.getInstance().schedule(
                    ProcessEventTask(), 0)
                LOGGER.atInfo().log("FourSepulchersManager: Beginning in Cooldown time")
            }
        }

        private fun cleanUp() {
            for (player in playersInside)
                player.teleToClosestTown()

            halls.values.forEach {
                it.deleteAllMobs()

                it.closeAllDoors()

                it.inUse.set(false)
            }

            val eventProgressTaskTemp =
                eventProgressTask
            if (eventProgressTaskTemp != null) {
                eventProgressTaskTemp.cancel(false)
                eventProgressTask = null
            }

            val managerSayTaskTemp =
                managerSayTask
            if (managerSayTaskTemp != null) {
                managerSayTaskTemp.cancel(false)
                managerSayTask = null
            }
        }

        val isEntryTime: Boolean
            get() = STATE.get() == ENTRY_STATE || STATE.get() == WARM_UP_STATE

        val isAttackTime: Boolean
            get() = STATE.get() == BATTLE_STATE

        fun findHallByDoorId(id: Int): FourSepulchersHall {
            val hall = halls.values.find { it.haveDoor(id) }
            return requireNotNull(hall) { "Can't find hall by door $id." }
        }

        fun onEntry(player: Player, loc: Location) {
            player.teleToLocation(Location.findPointToStay(player, loc, 0, 80))
            ItemFunctions.deleteItem(player,
                ENTRANCE_PASS, 1)
            ItemFunctions.deleteItemsEverywhere(player,
                CHAPEL_KEY
            )
        }

        fun checkAnnihilated(player: Player) {
            if (isPlayersAnnihilated) {
                ThreadPoolManager.getInstance().schedule({
                    val party = player.party
                    if (party != null) {
                        for (member in party.partyMembers) {
                            if (member.isDead)
                                exitPlayer(member)
                        }
                    } else
                        exitPlayer(player)
                }, 5000)
            }
        }

        fun exitPlayer(player: Player) {
            player.teleToLocation(
                Location.findPointToStay(EXIT_LOC, 100, ReflectionManager.MAIN.geoIndex),
                ReflectionManager.MAIN
            )
        }

        private fun minuteSelect(min: Int): Int {
            return when (min % 5) {
                0 -> min
                1 -> min - 1
                2 -> min - 2
                3 -> min + 2
                else -> min + 1
            }
        }

        fun managerSay(min: Int) {
            var min = min
            // for attack phase, sending message every 5 minutes
            if (isAttackTime) {
                if (min < 5)
                // do not shout when < 5 minutes
                    return

                min = minuteSelect(min)

                for (spawner in SpawnManager.getInstance().getSpawners(MANAGERS_SPAWN_GROUP)) {
                    for (npc in spawner.allSpawned) {
                        // hall not used right now, so its manager will not tell you anything :)
                        // if you don't need this - delete next two lines.
                        val inUse = halls[npc.npcId - 31920]?.inUse
                        if (inUse == null || !inUse.get())
                            continue

                        if (npc is SepulcherNpcInstance) {
                            if (min == 90)
                                ChatUtils.shout(npc, NpcString.GAME_OVER)
                            else
                                ChatUtils.shout(npc, NpcString.MINUTES_HAVE_PASSED, min.toString())
                        }
                    }
                }
            } else if (isEntryTime) {
                for (spawner in SpawnManager.getInstance().getSpawners(MANAGERS_SPAWN_GROUP)) {
                    for (npc in spawner.allSpawned) {
                        if (npc is SepulcherNpcInstance) {
                            ChatUtils.shout(npc, NpcString.YOU_MAY_NOW_ENTER_THE_SEPULCHER)
                            ChatUtils.shout(
                                npc,
                                NpcString.IF_YOU_PLACE_YOUR_HAND_ON_THE_STONE_STATUE_IN_FRONT_OF_EACH_SEPULCHER_YOU_WILL_BE_ABLE_TO_ENTER
                            )
                        }
                    }
                }
            }
        }

        private val isPlayersAnnihilated: Boolean
            get() {
                return playersInside.all { it.isDead }
            }

        private val playersInside: List<Player>
            get() {
                return zones.flatMap { it?.insidePlayers ?: emptyList() }
            }

        private fun checkIfInZone(cha: Creature): Boolean {
            return zones.any { it?.checkIfInZone(cha) ?: false }
        }

        class DeathListenerImpl : OnDeathListener {

            override fun onDeath(self: Creature, killer: Creature) {
                if (self.isPlayer && self.z >= -7250 && self.z <= -6841 && checkIfInZone(self))
                    checkAnnihilated(self as Player)
            }

        }
    }
}