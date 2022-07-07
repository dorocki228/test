package l2s.gameserver.entity

import gve.util.GveMessageUtil
import l2s.gameserver.ThreadPoolManager
import l2s.gameserver.dao.ArtifactDAO
import l2s.gameserver.instancemanager.ReflectionManager
import l2s.gameserver.instancemanager.SpawnManager
import l2s.gameserver.model.GameObjectsStorage
import l2s.gameserver.model.Player
import l2s.gameserver.model.SimpleSpawner
import l2s.gameserver.model.World
import l2s.gameserver.model.base.Fraction
import l2s.gameserver.model.bbs.ArtifactTeleportationCommunityBoardEntry
import l2s.gameserver.model.instances.ArtifactInstance
import l2s.gameserver.model.items.ItemInstance
import l2s.gameserver.network.l2.components.CustomMessage
import l2s.gameserver.network.l2.s2c.ExShowScreenMessage
import l2s.gameserver.security.HwidUtils
import l2s.gameserver.service.ConfrontationService
import l2s.gameserver.service.MoraleBoostService
import l2s.gameserver.templates.artifact.ArtifactTemplate
import l2s.gameserver.utils.ItemFunctions
import l2s.gameserver.utils.Language
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Future
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

/**
 * TODO rework
 */
class ArtifactEntity(val template: ArtifactTemplate, fraction: Fraction) {

    var fraction: Fraction = fraction
        private set
    var artifact: ArtifactInstance? = null
    var endProtect: Long = 0
    private var timeForNotification: Long = 0
    private val lastCastMap = ConcurrentHashMap<Fraction, Long>()
    private var future: ScheduledFuture<*>? = null

    private var teleportationTask: Future<*>? = null

    val nameForCommunityBoard: String = CustomMessage(template.stringName).toString(Language.ENGLISH) + " [Def]"
    private var communityBoardEntry = lazy { ArtifactTeleportationCommunityBoardEntry(this) }

    fun spawn() {
        val simpleSpawner = SimpleSpawner(template.npc)
        simpleSpawner.reflection = ReflectionManager.MAIN
        simpleSpawner.loc = template.location
        val temp = ArtifactInstance::class.java.cast(simpleSpawner.doSpawn(true))
        temp.entity = this
        temp.fraction = fraction
        if (fraction !== Fraction.NONE)
            temp.title = fraction.toString()
        temp.setParameter(PARAM_ENTITY, this)
        temp.broadcastCharInfo()
        artifact = temp
        val groups = template.spawnGroups[fraction]
        if (groups != null && groups.isNotEmpty())
            groups.forEach { g -> SpawnManager.getInstance().spawn(g) }
    }

    fun deSpawn() {
        if (artifact != null)
            artifact!!.deleteMe()
        artifact = null
        val groups = template.spawnGroups[fraction]
        if (groups != null && groups.isNotEmpty())
            groups.forEach { g -> SpawnManager.getInstance().despawn(g) }
    }

    @Synchronized
    fun changeFaction(caster: Player?) {
        if (caster == null)
            return

        val newFraction = caster.fraction
        if (endProtect >= System.currentTimeMillis() || newFraction === fraction)
            return

        stopCommunityBoardEntry()
        teleportationTask?.cancel(true)

        artifact?.stopDefend()

        lastCastMap.clear()
        GveMessageUtil.updateProtectMessage(fraction)
        endProtect = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(template.protectTime.toLong())
        val oldFraction = fraction
        val rewardRadius = template.params.getInteger("rewardRadius", 0)
        if (rewardRadius > 0)
            HwidUtils.filterSameHwids(World.getAroundPlayers(artifact, rewardRadius, 600)).stream()
                .filter { p -> p.fraction === caster.fraction }
                .forEach { p ->
                    template.rewardItems.forEach { i ->
                        val count = if (i.id == 57 && fraction == Fraction.NONE) 50 else i.count
                        ItemFunctions.addItem(p, i.id, count)

                        p.listeners.onArtifactCapture(artifact)
                    }
                }

        GameObjectsStorage.getPlayers()
            .forEach { p ->
                if (p.fraction === oldFraction) {
                    template.skillEntryList.forEach { s -> p.removeSkill(s.id, false) }
                } else if (p.fraction === newFraction) {
                    template.skillEntryList.forEach { s -> p.addSkill(s, false) }
                }

                val message = CustomMessage("artifact.s2")
                    .addString(newFraction.toString())
                    .addString(CustomMessage(template.stringName).toString(p))
                p.sendMessage(message)
            }
        MoraleBoostService.getInstance().artifactCapture(artifact, caster)
        ConfrontationService.getInstance().artifactCapture(artifact, caster)

        deSpawn()
        fraction = newFraction
        spawn()
        store()
    }

    fun store() {
        ArtifactDAO.getInstance().store(this)
    }

    fun notifyCast(player: Player?) {
        if (player == null)
            return

        if (endProtect >= System.currentTimeMillis() || player.fraction === fraction)
            return

        artifact?.startDefend()
        startCommunityBoardEntry()

        val currentTimeMillis = System.currentTimeMillis()
        lastCastMap[player.fraction] = currentTimeMillis
        if (timeForNotification > currentTimeMillis)
            return
        synchronized(this) {
            if (timeForNotification > currentTimeMillis)
                return
            GveMessageUtil.updateProtectMessage(fraction)
            timeForNotification = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(1)
            GameObjectsStorage.getPlayers().forEach { p ->
                val message = CustomMessage("artifact.s1").addString(player.fraction.toString())
                    .addString(CustomMessage(template.stringName).toString(p)).toString(p)
                p.sendPacket(
                    ExShowScreenMessage(
                        message,
                        10000,
                        ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER,
                        true
                    )
                )
            }
            stopProtectTask()
            future = ThreadPoolManager.getInstance().schedule(
                { GveMessageUtil.updateProtectMessage(fraction) },
                TimeUnit.SECONDS.toMillis((1 * 60 + 20).toLong())
            )
        }
    }

    fun notifyAbortCast(player: Player?) {
        if (player == null)
            return

        teleportationTask?.cancel(true)

        val anotherCaster = artifact?.getAroundCharacters(200, 300)
            ?.any { it.castingSkill != null && it.castingSkill.id == 246 }
            ?: false

        val task = StopTeleportationTask(this)
        if (!anotherCaster) {
            teleportationTask = ThreadPoolManager.getInstance().schedule(task, 1, TimeUnit.MINUTES)
        }
    }

    private fun stopProtectTask() {
        if (future != null)
            future!!.cancel(false)
        future = null
    }

    fun getLastCastFromFaction(fraction: Fraction): Long {
        return lastCastMap.getOrDefault(fraction, 0L)
    }

    fun getName(player: Player?): String {
        return if (player == null) "No Correct" else CustomMessage(template.stringName).toString(player)
    }

    fun startCommunityBoardEntry() {
        if (teleportationTask == null || teleportationTask!!.isDone || teleportationTask?.cancel(true) == true) {
            communityBoardEntry.value.register()
        }
    }

    fun stopCommunityBoardEntry() {
        communityBoardEntry.value.unregister()
    }

    companion object {
        const val PARAM_ENTITY = "entity_artifact"

        class StopTeleportationTask(private val artifactEntity: ArtifactEntity): Runnable {

            override fun run() {
                artifactEntity.stopCommunityBoardEntry()
            }

        }
    }

}
