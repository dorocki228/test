package l2s.gameserver.features.foursepulchers

import com.google.common.collect.HashMultimap
import com.google.common.flogger.FluentLogger
import l2s.commons.util.Rnd
import l2s.gameserver.features.foursepulchers.instances.SepulcherMonsterInstance
import l2s.gameserver.features.foursepulchers.instances.SepulcherRaidInstance
import l2s.gameserver.geometry.Location
import l2s.gameserver.instancemanager.ReflectionManager
import l2s.gameserver.instancemanager.SpawnManager
import l2s.gameserver.model.Player
import l2s.gameserver.model.instances.NpcInstance
import l2s.gameserver.utils.NpcUtils

import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

/**
 * @author ???
 * @author Java-mam (reworked)
 */
abstract class FourSepulchersHall(
        val managerNpcId: Int,
        private val gateKeeperSpawnGroup: String,
        private val startHallSpawn: Location,
        private val shadowId: Int,
        doorIds: IntArray
) {

    private val LOGGER = FluentLogger.forEnclosingClass()

    private val SHADOW_SPAWN_GROUP = "4_sepul_shadow_%d_%d"

    val inUse = AtomicBoolean()

    private val doors = doorIds.map {
        val door = ReflectionManager.MAIN.getDoor(it)
        requireNotNull(door) { "Can't find door with id $it" }
    }

    private val DUKE_MOB_GROUPS = HashMap<Int, String>()
    private val VISCOUNT_MOB_GROUPS = HashMap<Int, String>()

    private val SPAWNED_NPCS: MutableList<NpcInstance> = ArrayList()
    private val SPAWNED_MOBS = HashMultimap.create<Int, NpcInstance>()
    private val SPAWNED_GROUPS = HashSet<String>()

    private val KEY_BOX_NPC: Map<Int, Int>
    private val VICTIM: Map<Int, Int>

    init {
        KEY_BOX_NPC = mapOf(
                18120 to 31455,
                18121 to 31455,
                18122 to 31455,
                18123 to 31455,
                18124 to 31456,
                18125 to 31456,
                18126 to 31456,
                18127 to 31456,
                18128 to 31457,
                18129 to 31457,
                18130 to 31457,
                18131 to 31457,
                18149 to 31458,
                18150 to 31459,
                18151 to 31459,
                18152 to 31459,
                18153 to 31459,
                18154 to 31460,
                18155 to 31460,
                18156 to 31460,
                18157 to 31460,
                18158 to 31461,
                18159 to 31461,
                18160 to 31461,
                18161 to 31461,
                18162 to 31462,
                18163 to 31462,
                18164 to 31462,
                18165 to 31462,
                18183 to 31463,
                18184 to 31464,
                18212 to 31465,
                18213 to 31465,
                18214 to 31465,
                18215 to 31465,
                18216 to 31466,
                18217 to 31466,
                18218 to 31466,
                18219 to 31466
        )

        VICTIM = mapOf(
                18150 to 18158,
                18151 to 18159,
                18152 to 18160,
                18153 to 18161,
                18154 to 18162,
                18155 to 18163,
                18156 to 18164,
                18157 to 18165
        )
    }

    fun haveDoor(id: Int): Boolean {
        return doors.any { it.doorId == id }
    }

    fun closeAllDoors() {
        for (door in doors) {
            door.closeMe()
        }
    }

    fun deleteAllMobs() {
        for (group in SPAWNED_GROUPS)
            SpawnManager.getInstance().despawn(group)

        SPAWNED_GROUPS.clear()

        for (mob in SPAWNED_NPCS)
            mob.deleteMe()

        SPAWNED_NPCS.clear()

        for (mob in SPAWNED_MOBS.values())
            mob.deleteMe()

        SPAWNED_MOBS.clear()
    }

    fun spawnGateKeepers() {
        val spawners = SpawnManager.getInstance().spawn(gateKeeperSpawnGroup, true)
        spawners
                .flatMap { it.allSpawned }
                .forEach {
                    it.setParameter("four_sepulchers_hall", this)
                    //it.setParameter("parent_npc_id", managerNpcId)
                }
    }

    fun spawnShadow() {
        if (!FourSepulchersManager.isAttackTime)
            return

        val groupName = String.format(SHADOW_SPAWN_GROUP, shadowId, Rnd.get(1, 4))
        val spawners = SpawnManager.getInstance().spawn(groupName, false)
        spawners
                .flatMap { spawner -> spawner.allSpawned }
                .filter { npc -> npc is SepulcherRaidInstance }
                .forEach { npc ->
                    npc.setParameter("four_sepulchers_hall", this)
                    npc.setParameter("parent_npc_id", shadowId)
                }
        SPAWNED_GROUPS.add(groupName)
    }

    fun spawnEmperorsGraveNpc(npc: NpcInstance, parentNpcId: Int) {
        if (!FourSepulchersManager.isAttackTime)
            return

        val ghost = NpcUtils.spawnSingle(31452, npc.loc, npc.reflection)
        ghost.setParameter("four_sepulchers_hall", this)
        SPAWNED_NPCS.add(ghost)

        val group = String.format("4_sepul_emperor_npc_%d", parentNpcId)
        if (SPAWNED_GROUPS.add(group)) {
            val spawners = SpawnManager.getInstance().spawn(group, false)
            spawners
                    .flatMap { spawner -> spawner.allSpawned }
                    .forEach { npc ->
                        npc.setParameter("four_sepulchers_hall", this)
                        //npc.setParameter("parent_npc_id", parentNpcId)
                    }
        }
    }

    fun spawnArchonOfHalisha(parentNpcId: Int) {
        if (!FourSepulchersManager.isAttackTime)
            return

        val group = String.format("4_sepul_duke_final_monst_%d", parentNpcId)
        if (!SPAWNED_GROUPS.add(group))
            return

        val spawners = SpawnManager.getInstance().spawn(group, false)
        spawners
                .flatMap { spawner -> spawner.allSpawned }
                .filter { npc -> npc is SepulcherMonsterInstance }
                .forEach { npc ->
                    npc.setParameter("four_sepulchers_hall", this)
                    npc.setParameter("parent_npc_id", parentNpcId)
                }
    }

    fun spawnExecutionerOfHalisha(npc: NpcInstance) {
        if (!FourSepulchersManager.isAttackTime)
            return

        val victimId = VICTIM[npc.npcId] ?: return
        val victim = NpcUtils.spawnSingle(victimId, npc.loc)
        victim.setParameter("four_sepulchers_hall", this)
        SPAWNED_NPCS.add(victim)
    }

    fun spawnKeyBox(npc: NpcInstance) {
        if (!FourSepulchersManager.isAttackTime)
            return

        val keyBoxId = KEY_BOX_NPC[npc.npcId]
        requireNotNull(keyBoxId) { "Can't find key box for npc ${npc.npcId}" }

        val box = NpcUtils.spawnSingle(keyBoxId, npc.loc)
        box.setParameter("four_sepulchers_hall", this)
        SPAWNED_NPCS.add(box)
    }

    fun spawnMonster(parentNpcId: Int) {
        if (!FourSepulchersManager.isAttackTime)
            return

        var spawnedGroup: String? = VISCOUNT_MOB_GROUPS.remove(parentNpcId)
        if (spawnedGroup != null) {
            if (SPAWNED_GROUPS.remove(spawnedGroup))
                SpawnManager.getInstance().despawn(spawnedGroup)
        }

        spawnedGroup = DUKE_MOB_GROUPS.remove(parentNpcId)
        if (spawnedGroup != null) {
            if (SPAWNED_GROUPS.remove(spawnedGroup))
                SpawnManager.getInstance().despawn(spawnedGroup)
        }

        val spawnedMobs = SPAWNED_MOBS.removeAll(parentNpcId)
        for (spawnedMob in spawnedMobs) {
            spawnedMob.deleteMe()
        }

        val physGroup = String.format("4_sepul_phys_monst_%d", parentNpcId)
        val magicGroup = String.format("4_sepul_magic_monst_%d", parentNpcId)

        val group = if (Rnd.get(2) == 0) physGroup else magicGroup
        if (!SPAWNED_GROUPS.add(group))
            return

        val spawners = SpawnManager.getInstance().spawn(group, false)
        val spawnedMonsters = spawners.flatMap { it.allSpawned }

        for (npc in spawnedMonsters) {
            npc.setParameter("four_sepulchers_hall", this)
            npc.setParameter("parent_npc_id", parentNpcId)
        }

        if (spawnedMonsters.isNotEmpty()) {
            when (parentNpcId) {
                31469, 31474, 31479, 31484 ->
                    if (Rnd.get(2) == 0) {
                        val npc = Rnd.get(spawnedMonsters) ?: return

                        val beetle = NpcUtils.spawnSingle(18149, npc.spawnedLoc, npc.reflection)
                        beetle.setParameter("four_sepulchers_hall", this)
                        beetle.setParameter("parent_npc_id", parentNpcId)

                        npc.deleteMe()

                        SPAWNED_MOBS.put(parentNpcId, beetle)
                    }
            }
        }

        when (parentNpcId) {
            31469, 31474, 31479, 31484 -> VISCOUNT_MOB_GROUPS[parentNpcId] = group
            31472, 31477, 31482, 31487 -> DUKE_MOB_GROUPS[parentNpcId] = group
        }
    }

    fun spawnMysteriousBox(parentNpcId: Int) {
        if (!FourSepulchersManager.isAttackTime)
            return

        val group = String.format("4_sepul_myst_box_%d", parentNpcId)
        if (SPAWNED_GROUPS.add(group)) {
            val spawns = SpawnManager.getInstance().spawn(group, false)
            spawns
                    .flatMap { it.allSpawned }
                    .forEach {
                        it.setParameter("four_sepulchers_hall", this)
                        it.setParameter("parent_npc_id", parentNpcId)
                    }
        }
    }

    @Synchronized
    fun isDukeMobsAnnihilated(parentNpcId: Int): Boolean {
        val group = DUKE_MOB_GROUPS[parentNpcId] ?: return true

        val spawners = SpawnManager.getInstance().getSpawners(group)
        return spawners
                .flatMap { spawner -> spawner.allSpawned }
                .all { it.isDead }
    }

    @Synchronized
    fun isViscountMobsAnnihilated(parentNpcId: Int): Boolean {
        val group = VISCOUNT_MOB_GROUPS[parentNpcId] ?: return true

        val spawners = SpawnManager.getInstance().getSpawners(group)
        return spawners
                .flatMap { spawner -> spawner.allSpawned }
                .all { it.isDead } && SPAWNED_MOBS.values().all { it.isDead }
    }

    fun isShadowAlive(): Boolean {
        for (order in 1..4) {
            val spawn = String.format(SHADOW_SPAWN_GROUP, shadowId, order)
            val spawners = SpawnManager.getInstance().getSpawners(spawn)
            if (spawners
                            .flatMap { spawner -> spawner.allSpawned }
                            .any { npc -> !npc.isDead }
            ) {
                return true
            }
        }
        return false
    }

    fun entry(player: Player, unstable: Boolean) {
        val party = player.party
        when {
            party != null ->
                for (member in party.partyMembers)
                    FourSepulchersManager.onEntry(
                            member,
                            startHallSpawn
                    )
            player.isGM ->
                FourSepulchersManager.onEntry(player, startHallSpawn)
            else -> return
        }

        inUse.set(true)
    }

    companion object {

        class ConquerorsSepulcher : FourSepulchersHall(
                31921,
                "ConquerorsSepulcher_4_sepul_gatekeeper",
                startHallSpawn,
                31929,
                doors
        ) {

            companion object {
                val startHallSpawn = Location(181632, -85587, -7218)

                val doors: IntArray = intArrayOf(
                        25150012, 25150013, 25150014, 25150015, 25150016
                )
            }
        }

        class SepulcherOfRulers : FourSepulchersHall(
                31922,
                "SepulcherOfRulers_4_sepul_gatekeeper",
                startHallSpawn,
                31934,
                doors
        ) {

            companion object {
                val startHallSpawn = Location(179963, -88978, -7218)

                val doors: IntArray = intArrayOf(
                        25150002, 25150003, 25150004, 25150005, 25150006
                )
            }

        }

        class GreatSagesSepulcher : FourSepulchersHall(
                31923,
                "GreatSagesSepulcher_4_sepul_gatekeeper",
                startHallSpawn,
                31939,
                doors
        ) {

            companion object {
                val startHallSpawn = Location(173217, -86132, -7218)

                val doors: IntArray = intArrayOf(
                        25150032, 25150033, 25150034, 25150035, 25150036
                )
            }

        }

        class JudgesSepulcher : FourSepulchersHall(
                31924,
                "JudgesSepulcher_4_sepul_gatekeeper",
                startHallSpawn,
                31944,
                doors
        ) {

            companion object {
                val startHallSpawn = Location(175608, -82296, -7218)

                val doors: IntArray = intArrayOf(
                        25150022, 25150023, 25150024, 25150025, 25150026
                )
            }

        }
    }

}